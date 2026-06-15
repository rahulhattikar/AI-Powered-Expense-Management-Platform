package com.AIExpense.ExpenseTracker.report.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;

public record BudgetRemainingResponse(
        ExpenseCategory category,
        BigDecimal budgetLimit,
        BigDecimal totalSpent,
        BigDecimal remainingAmount,
        double utilizationPercentage,
        String status
) {
}
