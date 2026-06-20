package com.AIExpense.ExpenseTracker.budget.service;


import com.AIExpense.ExpenseTracker.budget.dto.BudgetRequest;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetResponse;
import com.AIExpense.ExpenseTracker.budget.dto.BudgetStatusResponse;
import com.AIExpense.ExpenseTracker.common.dto.PagedResponse;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.util.List;

public interface BudgetService {

    BudgetResponse createBudget(BudgetRequest budgetRequest);

    BudgetResponse updateBudget(Long id, BudgetRequest budgetRequest);

    void deleteBudget(Long id);

    BudgetResponse getBudgetById(Long id);

    List<BudgetResponse> getBudgetsByCategory(ExpenseCategory category);

    List<BudgetResponse> getBudgetsByMonthAndYear(int month, int year);

    List<BudgetResponse> getBudgetsByCategoryAndMonthAndYear(ExpenseCategory category,
                                                             int month,
                                                             int year);

    PagedResponse<BudgetResponse> getAllBudgetsPaged(int page, int size, String sortBy, String sortDirection);

    Boolean isBudgetExists(ExpenseCategory category,
                           int month,
                           int year);

    BudgetStatusResponse getBudgetStatus(ExpenseCategory category, int month, int year);


}
