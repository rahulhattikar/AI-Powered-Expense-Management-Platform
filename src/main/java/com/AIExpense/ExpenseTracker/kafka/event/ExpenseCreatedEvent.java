package com.AIExpense.ExpenseTracker.kafka.event;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseCreatedEvent(
        Long expenseId,
        Long userId,
        BigDecimal amount,
        ExpenseCategory category,
        LocalDate expenseDate,
        LocalDateTime occurredAt) {
}
