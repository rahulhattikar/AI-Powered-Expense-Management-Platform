package com.AIExpense.ExpenseTracker.report.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;

public record CategoryBreakdown(
        ExpenseCategory category,
        BigDecimal totalSpent,
        double percentageOfTotal
) {
}
