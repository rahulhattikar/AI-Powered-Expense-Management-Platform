package com.AIExpense.ExpenseTracker.expense.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        String description,
        ExpenseCategory category,
        LocalDate expenseDate,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
