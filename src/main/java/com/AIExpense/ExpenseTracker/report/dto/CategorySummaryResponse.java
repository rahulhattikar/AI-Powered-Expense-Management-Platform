package com.AIExpense.ExpenseTracker.report.dto;

import com.AIExpense.ExpenseTracker.entity.ExpenseCategory;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        ExpenseCategory category,
        BigDecimal totalSpent,
        BigDecimal budgetLimit,
        BigDecimal remainingAmount,
        double utilizationPercentage,
        int totalTransactions,
        String status) {
}
