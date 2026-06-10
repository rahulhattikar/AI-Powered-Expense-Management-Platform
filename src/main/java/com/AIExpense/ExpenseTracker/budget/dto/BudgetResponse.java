package com.AIExpense.ExpenseTracker.budget.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponse(
        Long id,
        ExpenseCategory category,
        BigDecimal monthlyLimit,
        int month,
        int year,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
