package com.AIExpense.ExpenseTracker.report.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        ExpenseCategory category,
        BigDecimal totalSpent,           // actual spending
        BigDecimal budgetLimit,          // from Budget entity
        BigDecimal remainingAmount,      // budgetLimit - totalSpent
        double utilizationPercentage,    // (totalSpent/budgetLimit) * 100
        int totalTransactions,           // number of expenses in category
        String status) {
}
