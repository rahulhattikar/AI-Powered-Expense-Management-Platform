package com.AIExpense.ExpenseTracker.kafka.event;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetAlertEvent(
        Long userId,
        String userEmail,
        ExpenseCategory category,
        BigDecimal budgetLimit,
        BigDecimal actualSpending,
        BigDecimal exceededBy,
        int month,
        int year,
        LocalDateTime occurredAt
) {
}
