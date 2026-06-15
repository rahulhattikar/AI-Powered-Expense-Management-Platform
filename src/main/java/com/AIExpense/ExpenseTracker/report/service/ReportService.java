package com.AIExpense.ExpenseTracker.report.service;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import com.AIExpense.ExpenseTracker.report.dto.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public interface ReportService {

    MonthlySummaryResponse getMonthlySummary(int month, int year);

    CategorySummaryResponse getCategorySummary(ExpenseCategory category, int month, int year);

    List<TopSpendingCategoryResponse> getTopSpendingCategories(int month, int year, int limit);

    List<BudgetRemainingResponse> getBudgetRemaining(int month, int year);

    List<MonthlyTrendResponse> getMonthlyTrend(int months);
}
