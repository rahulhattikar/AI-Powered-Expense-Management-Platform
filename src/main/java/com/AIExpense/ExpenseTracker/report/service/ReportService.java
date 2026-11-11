package com.AIExpense.ExpenseTracker.report.service;

import com.AIExpense.ExpenseTracker.report.dto.MonthlySummaryResponse;
import com.AIExpense.ExpenseTracker.report.dto.BudgetRemainingResponse;
import com.AIExpense.ExpenseTracker.report.dto.CategorySummaryResponse;
import com.AIExpense.ExpenseTracker.report.dto.MonthlyTrendResponse;
import com.AIExpense.ExpenseTracker.report.dto.TopSpendingCategoryResponse;
import com.AIExpense.ExpenseTracker.entity.ExpenseCategory;


import java.util.List;

public interface ReportService {

    MonthlySummaryResponse getMonthlySummary(int month, int year);

    CategorySummaryResponse getCategorySummary(ExpenseCategory category, int month, int year);

    List<TopSpendingCategoryResponse> getTopSpendingCategories(int month, int year, int limit);

    List<BudgetRemainingResponse> getBudgetRemaining(int month, int year);

    List<MonthlyTrendResponse> getMonthlyTrend(int months);
}
