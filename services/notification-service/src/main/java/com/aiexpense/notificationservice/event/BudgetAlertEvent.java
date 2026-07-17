package com.aiexpense.notificationservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;



public record BudgetAlertEvent(
        Long userId,
        String userEmail,
        String category,
        BigDecimal budgetLimit,
        BigDecimal actualSpending,
        BigDecimal exceededBy,
        int month,
        int year,
        LocalDateTime occurredAt
) {}