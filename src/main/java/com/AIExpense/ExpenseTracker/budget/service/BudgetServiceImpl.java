package com.AIExpense.ExpenseTracker.budget.service;


import com.AIExpense.ExpenseTracker.budget.dto.BudgetRequest;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetResponse;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetStatusResponse;
import com.AIExpense.ExpenseTracker.budget.entity.Budget;
import com.AIExpense.ExpenseTracker.budget.mapper.BudgetMapper;
import com.AIExpense.ExpenseTracker.budget.repository.BudgetRepository;
import com.AIExpense.ExpenseTracker.common.exception.BudgetNotFoundException;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.expense.service.ExpenseService;
import com.AIExpense.ExpenseTracker.user.entity.User;
import com.AIExpense.ExpenseTracker.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;

    private final ExpenseService expenseService;

    private final AuthenticationUtils authenticationUtils;

    @Override
    @Transactional
    public BudgetResponse createBudget(BudgetRequest budgetRequest) {
        log.info("Creating budget for category: {}", budgetRequest.category());
        User user = authenticationUtils.getCurrentUser();
        Budget budget = Budget.builder()
                .user(user)
                .category(budgetRequest.category())
                .monthlyLimit(budgetRequest.monthlyLimit())
                .month(budgetRequest.month())
                .year(budgetRequest.year())
                .build();
        return BudgetMapper.toResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest budgetRequest) {
        log.info("Updating budget with id {}", id);
        User user = authenticationUtils.getCurrentUser();
        Budget budget = findBudgetByIdAndUserId(id, user.getId());
        budget.setCategory(budgetRequest.category());
        budget.setMonthlyLimit(budgetRequest.monthlyLimit());
        budget.setMonth(budgetRequest.month());
        budget.setYear(budgetRequest.year());
        return BudgetMapper.toResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public void deleteBudget(Long id) {
        log.info("Deleting budget with id: {}", id);
        User user = authenticationUtils.getCurrentUser();
        Budget budget = findBudgetByIdAndUserId(id, user.getId());
        budgetRepository.delete(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        log.info("Getting Budget with id: {}", id);
        User user = authenticationUtils.getCurrentUser();
        Budget budget = findBudgetByIdAndUserId(id, user.getId());
        return BudgetMapper.toResponse(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByCategory(ExpenseCategory category) {
        log.info("get budget by category: {}", category);
        User user = authenticationUtils.getCurrentUser();
        List<Budget> budgets = budgetRepository.findByUserIdAndCategory(user.getId(), category);
        if (budgets.isEmpty()) {
            throw new BudgetNotFoundException(
                    "No budgets found for category: " + category);
        }

        return budgets.stream()
                .map(BudgetMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByMonthAndYear(int month, int year) {
        log.info("Getting budgets of month: {}", month);
        User user = authenticationUtils.getCurrentUser();
        List<Budget> budgets = budgetRepository
                .findAllByUserIdAndMonthAndYear(user.getId(), month, year);

        if (budgets.isEmpty()) {
            throw new BudgetNotFoundException(
                    "No budgets found for month: " + month + " year: " + year);
        }

        return budgets.stream()
                .map(BudgetMapper::toResponse)
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByCategoryAndMonthAndYear(ExpenseCategory category, int month, int year) {
        log.info("Getting budgets for category: {} month: {} year: {}", category, month, year);
        User user = authenticationUtils.getCurrentUser();
        List<Budget> budgets = budgetRepository
                .findByUserIdAndCategoryAndMonthAndYear(user.getId(), category, month, year);

        if (budgets.isEmpty()) {
            throw new BudgetNotFoundException(
                    "No budgets found for category: " + category
                            + " month: " + month
                            + " year: " + year);
        }

        return budgets.stream()
                .map(BudgetMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets() {
        log.info("Getting all budgets for current user");
        User user = authenticationUtils.getCurrentUser();
        List<Budget> budgets = budgetRepository.findAllByUserId(user.getId());

        if (budgets.isEmpty()) {
            throw new BudgetNotFoundException("No budgets found for current user");
        }

        return budgets.stream()
                .map(BudgetMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isBudgetExists(ExpenseCategory category, int month, int year) {
        log.info("Checking budget existence for category: {}", category);
        User user = authenticationUtils.getCurrentUser();
        return budgetRepository.existsByUserIdAndCategoryAndMonthAndYear(user.getId(),
                category, month, year);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetStatusResponse getBudgetStatus(ExpenseCategory category, int month, int year) {
        log.info("Getting budget status for category: {} month: {} year: {}", category, month, year);
        User user = authenticationUtils.getCurrentUser();
        Budget budget = budgetRepository
                .findActiveBudget(user.getId(), category, month, year)
                .orElseThrow(() -> new BudgetNotFoundException(
                        "No budget found for category: " + category
                                + " month: " + month
                                + " year: " + year));

        BigDecimal actualSpending = expenseService.getTotalSpendingByCategory(category);
        BigDecimal remainingAmount = budget.getMonthlyLimit().subtract(actualSpending);
        String status = actualSpending.compareTo(budget.getMonthlyLimit()) > 0
                ? "EXCEEDED"
                : "WITHIN_BUDGET";

        return new BudgetStatusResponse(
                category,
                budget.getMonthlyLimit(),
                actualSpending,
                remainingAmount,
                month,
                year,
                status
        );
    }


    private Budget findBudgetByIdAndUserId(Long id, Long userId) {
        return budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BudgetNotFoundException(
                        "Budget not found with id: " + id));
    }

}
