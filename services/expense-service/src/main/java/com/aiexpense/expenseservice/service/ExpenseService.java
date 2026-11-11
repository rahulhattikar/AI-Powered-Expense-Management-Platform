package com.aiexpense.expenseservice.service;

import com.aiexpense.expenseservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(Long userId, CreateExpenseRequest request);

    ExpenseResponse getExpenseById(Long userId, Long expenseId);

    PagedResult<ExpenseResponse> getAllExpenses(Long userId, Pageable pageable);

    Page<ExpenseResponse> filterExpenses(
            Long userId,
            String category,
            String startDate,
            String endDate,
            Pageable pageable
    );

    ExpenseResponse updateExpense(Long userId, Long expenseId, UpdateExpenseRequest request);
    
    void deleteExpense(Long userId, Long expenseId);

    MonthlySummaryResponse getMonthlySummary(Long userId, Integer month, Integer year);

    List<CategorySpendingResponse> getCategoryBreakdown(Long userId, Integer month, Integer year);

    List<ExpenseResponse> getExpensesByMonthAndCategory(
            Long userId,
            String category,
            Integer month,
            Integer year
    );

    List<MonthlyTrendResponse> getMonthlyTrend(Long userId, LocalDate startDate);

    BigDecimal getTotalSpendingByCategory(Long userId, String category);

}
