package com.AIExpense.ExpenseTracker.expense.service;


import com.AIExpense.ExpenseTracker.expense.dto.ExpenseRequest;
import com.AIExpense.ExpenseTracker.expense.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.common.dto.PagedResponse;
import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.util.List;


public interface ExpenseService {
    ExpenseResponse createExpense(ExpenseRequest request);

    ExpenseResponse getExpenseById(Long id);

    ExpenseResponse updateExpense(Long id, ExpenseRequest request);

    void deleteExpense(Long id);

    List<ExpenseResponse> getExpensesByCategory(ExpenseCategory category);

    List<ExpenseResponse> getExpensesByMonthAndYear(int month, int year);

    BigDecimal getTotalSpendingByCategory(ExpenseCategory category);

    BigDecimal getTotalSpending();

    PagedResponse<ExpenseResponse> getAllExpensesPaged(int page, int size,
                                                       String sortBy, String sortDirection);


}
