package com.AIExpense.ExpenseTracker.dto;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ExpenseResponse(
        Long id,
        Long userId,
        BigDecimal amount,
        String description,
        String category,
        LocalDate expenseDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
