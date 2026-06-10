package com.AIExpense.ExpenseTracker.budget.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;

public record BudgetStatusResponse(
        ExpenseCategory category,
        BigDecimal monthlyLimit,      // what user budgeted
        BigDecimal actualSpending,    // what user actually spent
        BigDecimal remainingAmount,   // monthlyLimit - actualSpending
        int month,
        int year,
        String status) {  // "WITHIN_BUDGET" or "EXCEEDED"
}
