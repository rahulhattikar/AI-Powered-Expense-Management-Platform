package com.aiexpense.expenseservice.service;


import com.aiexpense.expenseservice.dto.*;
import com.aiexpense.expenseservice.entity.Expense;
import com.aiexpense.expenseservice.entity.ExpenseCategory;
import com.aiexpense.expenseservice.event.ExpenseEvent;
import com.aiexpense.expenseservice.event.ExpenseEventProducer;
import com.aiexpense.expenseservice.exception.ExpenseNotFoundException;
import com.aiexpense.expenseservice.repository.ExpenseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {


    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseEventProducer eventProducer;


    @Override
    @CacheEvict(value = "userExpenses", key = "#userId")
    public ExpenseResponse createExpense(Long userId, CreateExpenseRequest request) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        Expense expense = expenseMapper.toEntity(request, userId);

        Expense saved = expenseRepository.save(expense);

        publishExpenseCreatedEvent(saved);

        log.info("Created expense: {} for user: {}", saved.getId(), userId);

        return expenseMapper.toResponse(saved);
    }


    @Override
    @Cacheable(value = "expense", key = "#expenseId", unless = "#result == null")
    public ExpenseResponse getExpenseById(Long userId, Long expenseId) {

        log.info("Fetching expense: {} for user: {}", expenseId, userId);

        Expense expense = expenseRepository.findByIdAndNotDeleted(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));


        if (!expense.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to expense");
        }

        return expenseMapper.toResponse(expense);
    }


    @Override
    @Cacheable(value = "userExpenses",
              key ="#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PagedResult<ExpenseResponse> getAllExpenses(Long userId, Pageable pageable) {

        log.debug("Fetching expenses for user: {}, page: {}", userId, pageable.getPageNumber());

        Page<Expense> page = expenseRepository
                .findByUserIdAndIsDeletedFalse(userId, pageable);

        List<ExpenseResponse> content = page.getContent().stream()
                .map(expenseMapper::toResponse)
                .toList();

        return new PagedResult<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }


    @Override
    public Page<ExpenseResponse> filterExpenses(
            Long userId,
            String category,
            String startDate,
            String endDate,
            Pageable pageable) {

        log.debug("Filtering expenses for user: {} with category: {}", userId, category);

        Page<Expense> expenses;

        if (category != null && !category.isBlank()) {
            ExpenseCategory cat = ExpenseCategory.valueOf(category.toUpperCase());

            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                expenses = expenseRepository.findWithMultipleFilters(
                        userId, cat, start, end, BigDecimal.ZERO, pageable
                );
            } else {
                expenses = expenseRepository.findByUserIdAndCategory(userId, cat, pageable);
            }
        } else if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            expenses = expenseRepository.findByDateRange(userId, start, end, pageable);
        } else {
            expenses = expenseRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
        }

        return expenses.map(expenseMapper::toResponse);
    }


    @Override
    @CacheEvict(value = {"expense", "userExpenses"}, allEntries = true)
    public ExpenseResponse updateExpense(Long userId, Long expenseId, UpdateExpenseRequest request) {

        Expense existing = expenseRepository.findByIdAndNotDeleted(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));


        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to expense");
        }


        BigDecimal oldAmount = existing.getAmount();
        String oldCategory = existing.getCategory().name();

        Expense updated = expenseMapper.updateEntity(existing, request);
        Expense saved = expenseRepository.save(updated);

        publishExpenseUpdatedEvent(saved, oldAmount, oldCategory);

        log.info("Updated expense: {} for user: {}", expenseId, userId);

        return expenseMapper.toResponse(saved);
    }


    @Override
    @CacheEvict(value = {"expense", "userExpenses"}, allEntries = true)
    public void deleteExpense(Long userId, Long expenseId) {

        Expense existing = expenseRepository.findByIdAndNotDeleted(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));

        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to expense");
        }

        existing.setIsDeleted(true);
        existing.setUpdatedAt(LocalDateTime.now());

        expenseRepository.save(existing);

        publishExpenseDeletedEvent(existing);

        log.info("Deleted expense: {} for user: {}", expenseId, userId);
    }

    @Override
    public MonthlySummaryResponse getMonthlySummary(Long userId, Integer month, Integer year) {

        log.info("Fetching monthly summary for user: {}, month: {}, year: {}",
                userId, month, year);

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be 1-12");
        }

        BigDecimal totalSpent = expenseRepository.getTotalSpentByMonth(userId, month, year);
        BigDecimal highest = expenseRepository.getHighestExpenseByMonth(userId, month, year);
        int totalTransactions = expenseRepository.countTransactionsByMonth(userId, month, year);
        BigDecimal averagePerDay = totalTransactions > 0 ? totalSpent
                .divide(BigDecimal.valueOf(totalTransactions), RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return MonthlySummaryResponse.builder()
                .userId(userId)
                .month(month)
                .year(year)
                .totalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO)
                .totalTransactions(totalTransactions)
                .averagePerDay(averagePerDay)
                .highestExpense(highest != null ? highest : BigDecimal.ZERO)
                .build();
    }

    @Override
    public List<CategorySpendingResponse> getCategoryBreakdown(Long userId, Integer month, Integer year) {

        log.info("Fetching category breakdown for user: {}, month: {}, year: {}",
                userId, month, year);

        List<Object[]> results = expenseRepository
                .getSpendingByCategoryForMonth(userId, month, year);


        return results.stream()
                .map(row -> CategorySpendingResponse.builder()
                        .category((String) row[0])
                        .amount((BigDecimal) row[1])
                        .count((Long) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseResponse> getExpensesByMonthAndCategory(
            Long userId,
            String category,
            Integer month,
            Integer year) {

        log.info("Fetching expenses By month and category for user: {}, category: {}, month: {}, year: {}",
                userId, category, month, year);

        ExpenseCategory cat = ExpenseCategory.valueOf(category.toUpperCase());
        List<Expense> expenses = expenseRepository
                .findByCategoryAndUserIdAndMonthAndYear(userId, cat, month, year);

        return expenses.stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyTrendResponse> getMonthlyTrend(Long userId, LocalDate startDate) {

        log.info("Fetching monthly trend for user: {}, starting from: {}", userId, startDate);

        List<Object[]> results = expenseRepository.getMonthlyTrend(userId, startDate);

        List<MonthlyTrendResponse> trend = new ArrayList<>();
        BigDecimal previousAmount = null;

        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            BigDecimal percentageChange = BigDecimal.ZERO;
            if (previousAmount != null) {
                percentageChange = amount.subtract(previousAmount)
                        .divide(previousAmount, 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
            }

            trend.add(MonthlyTrendResponse.builder()
                    .month(YearMonth.from(date))
                    .totalSpent(amount)
                    .previousMonthSpent(previousAmount)
                    .percentageChange(percentageChange)
                    .build());

            previousAmount = amount;
        }
        return trend;
    }

    @Override
    public BigDecimal getTotalSpendingByCategory(Long userId, String category) {

        log.info("Fetching total spending by category for user: {}, category: {}",
                userId, category);

        ExpenseCategory cat = ExpenseCategory.valueOf(category.toUpperCase());
        BigDecimal total = expenseRepository.getTotalSpendingByCategory(userId, cat);
        return total != null ? total : BigDecimal.ZERO;
    }


    private void publishExpenseCreatedEvent(Expense expense) {

        BigDecimal monthlyCategoryTotal = expenseRepository
                .getTotalByCategoryAndMonth(expense.getUserId(), expense.getCategory(),
                        expense.getExpenseDate().getMonthValue());

        ExpenseEvent event = ExpenseEvent.builder()
                .expenseId(expense.getId())
                .userId(expense.getUserId())
                .amount(expense.getAmount())
                .category(expense.getCategory().name())
                .monthlyCategoryTotal(monthlyCategoryTotal != null
                        ? monthlyCategoryTotal : BigDecimal.ZERO)
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .build();

        eventProducer.publishExpenseCreated(event);
    }

    private void publishExpenseUpdatedEvent(Expense expense, BigDecimal oldAmount,
                                            String oldCategory) {

        BigDecimal monthlyCategoryTotal = expenseRepository
                .getTotalByCategoryAndMonth(expense.getUserId(), expense.getCategory(),
                        expense.getExpenseDate().getMonthValue());

        ExpenseEvent event = ExpenseEvent.builder()
                .expenseId(expense.getId())
                .userId(expense.getUserId())
                .amount(expense.getAmount())
                .previousAmount(oldAmount)
                .category(expense.getCategory().name())
                .monthlyCategoryTotal(monthlyCategoryTotal != null ?
                        monthlyCategoryTotal : BigDecimal.ZERO)
                .previousCategory(oldCategory)
                .build();

        eventProducer.publishExpenseUpdated(event);
    }

    private void publishExpenseDeletedEvent(Expense expense) {

        BigDecimal monthlyCategoryTotal = expenseRepository
                .getTotalByCategoryAndMonth(expense.getUserId(), expense.getCategory(),
                        expense.getExpenseDate().getMonthValue());

        ExpenseEvent event = ExpenseEvent.builder()
                .expenseId(expense.getId())
                .userId(expense.getUserId())
                .amount(expense.getAmount())
                .category(expense.getCategory().name())
                .monthlyCategoryTotal(monthlyCategoryTotal != null ?
                        monthlyCategoryTotal : BigDecimal.ZERO)
                .build();

        eventProducer.publishExpenseDeleted(event);
    }
}
