package com.AIExpense.ExpenseTracker.expense.service;


import com.AIExpense.ExpenseTracker.common.exception.ExpenseNotFoundException;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseRequest;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.common.dto.PagedResponse;
import com.AIExpense.ExpenseTracker.expense.entity.Expense;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.mapper.ExpenseMapper;
import com.AIExpense.ExpenseTracker.expense.repository.ExpenseRepository;
import com.AIExpense.ExpenseTracker.kafka.event.ExpenseCreatedEvent;
import com.AIExpense.ExpenseTracker.kafka.producer.ExpenseEventProducer;
import com.AIExpense.ExpenseTracker.util.AuthenticationUtils;
import com.AIExpense.ExpenseTracker.util.CacheNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    private final AuthenticationUtils authenticationUtils;

    private final ExpenseEventProducer expenseEventProducer;

    private static final String EXPENSE = "Expense not found with id: ";

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("expenseDate", "amount", "category", "createdAt");

    @Override
    @Transactional
    @CacheEvict(value = {CacheNames.MONTHLY_SUMMARY, CacheNames.CATEGORY_SUMMARY, CacheNames.TOP_SPENDING_CATEGORIES,
            CacheNames.BUDGET_REMAINING, CacheNames.MONTHLY_TREND},allEntries = true)
    public ExpenseResponse createExpense(ExpenseRequest request) {
        log.info("Creating expense for user");

        Long userId = authenticationUtils.getCurrentUserId();
        String userEmail = authenticationUtils
                .extractClaimFromCurrentRequest("email");

        Expense expense = Expense.builder()
                .amount(request.amount())
                .description(request.description())
                .category(request.category())
                .expenseDate(request.expenseDate())
                .userId(userId)
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense created with id: {}", savedExpense.getId());

        expenseEventProducer.publishExpenseCreated(new ExpenseCreatedEvent(
                savedExpense.getId(),
                userId,
                userEmail,
                savedExpense.getAmount(),
                savedExpense.getCategory(),
                savedExpense.getExpenseDate(),
                LocalDateTime.now()
        ));

        return ExpenseMapper.toResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        log.info("Getting expense with id: {}", id);
        Long userId = authenticationUtils.getCurrentUserId();
        return expenseRepository.findByIdAndUserId(id, userId)
                .map(ExpenseMapper::toResponse)
                .orElseThrow(() -> new ExpenseNotFoundException(EXPENSE + id));
    }


    @Override
    @CacheEvict(value = {CacheNames.MONTHLY_SUMMARY, CacheNames.CATEGORY_SUMMARY, CacheNames.TOP_SPENDING_CATEGORIES,
            CacheNames.BUDGET_REMAINING, CacheNames.MONTHLY_TREND},allEntries = true)
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        log.info("Updating expense with id: {}", id);
        Long userId = authenticationUtils.getCurrentUserId();
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(EXPENSE + id));
        expense.setAmount(request.amount());
        expense.setDescription(request.description());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());
        return ExpenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    @CacheEvict(value = {CacheNames.MONTHLY_SUMMARY, CacheNames.CATEGORY_SUMMARY, CacheNames.TOP_SPENDING_CATEGORIES,
            CacheNames.BUDGET_REMAINING, CacheNames.MONTHLY_TREND},allEntries = true)
    public void deleteExpense(Long expenseId) {
        log.info("Deleting expense with id: {}", expenseId);
        Long userId = authenticationUtils.getCurrentUserId();
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(EXPENSE + expenseId));
        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByCategory(ExpenseCategory category) {
        log.info("Finding expenses by category: {}", category);
        Long userId = authenticationUtils.getCurrentUserId();
        return expenseRepository.findByCategoryAndUserId(category, userId)
                .stream()
                .map(ExpenseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByMonthAndYear(int month, int year) {
        Long userId = authenticationUtils.getCurrentUserId();
        log.info("Finding expenses for user id: {} for month: {} and year: {}", userId, month, year);
        return expenseRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream()
                .map(ExpenseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpendingByCategory(ExpenseCategory category) {
        Long userId = authenticationUtils.getCurrentUserId();
        log.info("Getting total amount for user id: {} and category: {}", userId, category);
        return expenseRepository.getTotalAmountByUserIdAndCategory(userId, category);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpending() {
        Long userId = authenticationUtils.getCurrentUserId();
        log.info("Getting total amount for user id: {}", userId);
        return expenseRepository.getTotalAmountByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> getAllExpensesPaged(int page, int size,
                                                              String sortBy,
                                                              String sortDirection) {


        log.info("Getting all  budgets page: {} size: {}", page, size);
        Long userId = authenticationUtils.getCurrentUserId();


        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }

        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page , size , sort);

        Page<Expense> expensePage = expenseRepository.findByUserId(userId, pageable);

        return new PagedResponse<>(
                expensePage.getContent().stream().map(
                        ExpenseMapper::toResponse
                ).toList(),
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements(),
                expensePage.getTotalPages(),
                expensePage.isLast()
        );

    }

}
