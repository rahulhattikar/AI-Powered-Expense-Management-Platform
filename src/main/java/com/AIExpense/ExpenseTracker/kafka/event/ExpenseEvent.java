package com.AIExpense.ExpenseTracker.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        Long expenseId,
        Long userId,
        String userEmail,
        String description,
        BigDecimal amount,
        String category,
        BigDecimal monthlyCategoryTotal,
        LocalDate expenseDate,
        BigDecimal previousAmount,
        String previousCategory
) {}
