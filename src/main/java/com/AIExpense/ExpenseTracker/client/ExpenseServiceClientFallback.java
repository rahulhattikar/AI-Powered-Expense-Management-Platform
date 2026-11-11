package com.AIExpense.ExpenseTracker.client;


import com.AIExpense.ExpenseTracker.dto.*;
import com.AIExpense.ExpenseTracker.report.dto.MonthlySummaryResponse;
import com.AIExpense.ExpenseTracker.report.dto.CategorySpendingResponse;
import com.AIExpense.ExpenseTracker.report.dto.MonthlyTrendResponse;
import com.AIExpense.ExpenseTracker.dto.ExpenseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ExpenseServiceClientFallback implements ExpenseServiceClient {

    @Override
    public ExpenseResponse getExpenseById(Long expenseId) {
        log.warn("Expense-Service unavailable, returning null for expense: {}", expenseId);
        return null;
    }

    @Override
    public CategoryTotalResponse getCategoryTotal(String category, String token) {
        log.warn("Expense-Service unavailable, returning 0 total for category: {}", category);

        return CategoryTotalResponse.builder()
                .category(category)
                .totalAmount(BigDecimal.ZERO)
                .build();
    }

    @Override
    public MonthlySummaryResponse getMonthlySummary(
            Integer month, Integer year, String token) {
        log.warn("Expense-Service unavailable, returning empty summary for {}/{}", month, year);

        return MonthlySummaryResponse.builder()
                .month(month)
                .year(year)
                .totalSpent(BigDecimal.ZERO)
                .highestExpense(BigDecimal.ZERO)
                .build();
    }

    @Override
    public List<CategorySpendingResponse> getCategoryBreakdown(
            Integer month, Integer year, String token) {
        log.warn("Expense-Service unavailable, returning empty breakdown for {}/{}", month, year);
        return new ArrayList<>();
    }

    @Override
    public List<ExpenseResponse> getExpensesByMonthAndCategory(
            String category, Integer month, Integer year, String token) {
        log.warn("Expense-Service unavailable, returning empty expenses for {}/{}/{}",
                category, month, year);
        return new ArrayList<>();
    }

    @Override
    public List<MonthlyTrendResponse> getMonthlyTrend(String startDate, String token) {
        log.warn("Expense-Service unavailable, returning empty trend");
        return new ArrayList<>();
    }
}
