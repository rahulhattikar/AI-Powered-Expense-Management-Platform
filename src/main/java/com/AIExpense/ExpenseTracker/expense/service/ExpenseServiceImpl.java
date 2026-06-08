package com.AIExpense.ExpenseTracker.expense.service;


import com.AIExpense.ExpenseTracker.common.exception.ExpenseNotFoundException;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseRequest;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.expense.entity.Expense;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.mapper.ExpenseMapper;
import com.AIExpense.ExpenseTracker.expense.repository.ExpenseRepository;
import com.AIExpense.ExpenseTracker.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    private static final String EXPENSE = "Expense not found with id: ";

    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        log.info("Creating expense for user");
        User user = getCurrentUser();

        Expense expense = Expense.builder()
                .amount(request.amount())
                .description(request.description())
                .category(request.category())
                .expenseDate(request.expenseDate())
                .user(user)
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense created with id: {}", savedExpense.getId());
        return ExpenseMapper.toResponse(savedExpense);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        log.info("Getting expense with id: {}", id);
        User user = getCurrentUser();
        return expenseRepository.findByIdAndUserId(id, user.getId())
                .map(ExpenseMapper::toResponse)
                .orElseThrow(() -> new ExpenseNotFoundException(EXPENSE + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        log.info("Getting all expenses for user");
        User user = getCurrentUser();
        return expenseRepository.findByUserId(user.getId())
                .stream().map(ExpenseMapper::toResponse)
                .toList();
    }

    @Override
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        log.info("Updating expense with id: {}", id);
        User user = getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ExpenseNotFoundException(EXPENSE + id));
        expense.setAmount(request.amount());
        expense.setDescription(request.description());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());
        return ExpenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    public void deleteExpense(Long expenseId) {
        log.info("Deleting expense with id: {}", expenseId);
        User user = getCurrentUser();
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, user.getId())
                .orElseThrow(() -> new ExpenseNotFoundException(EXPENSE + expenseId));
        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByCategory(ExpenseCategory category) {
        log.info("Finding expenses by category: {}", category);
        User user = getCurrentUser();
          return expenseRepository.findByCategoryAndUserId(category, user.getId())
                .stream()
                .map(ExpenseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByMonthAndYear(int month, int year) {
        User user = getCurrentUser();
        log.info("Finding expenses for user id: {} for month: {} and year: {}", user.getId(), month, year);
        return expenseRepository.findByUserIdAndMonthAndYear(user.getId(), month, year)
                .stream()
                .map(ExpenseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpendingByCategory(ExpenseCategory category) {
        User user = getCurrentUser();
        log.info("Getting total amount for user id: {} and category: {}", user.getId(), category);
        return expenseRepository.getTotalAmountByUserIdAndCategory(user.getId(), category);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpending() {
        User user = getCurrentUser();
        log.info("Getting total amount for user id: {}", user.getId());
        return expenseRepository.getTotalAmountByUserId(user.getId());
    }


    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication.getPrincipal() instanceof User user) {
            return user;
        } else {
            throw new RuntimeException("Invalid authentication principal");
        }
    }
}
