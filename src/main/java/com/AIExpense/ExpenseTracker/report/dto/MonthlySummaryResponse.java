package com.AIExpense.ExpenseTracker.report.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        int month,
        int year,
        BigDecimal totalSpent,          // total money spent
        int totalTransactions,          // number of expenses
        BigDecimal averagePerDay,       // totalSpent / days in month
        BigDecimal highestExpense,      // single largest expense
        String highestSpendingCategory, // which category spent most
        List<CategoryBreakdown> categoryBreakdown) // spending per category
{
}
