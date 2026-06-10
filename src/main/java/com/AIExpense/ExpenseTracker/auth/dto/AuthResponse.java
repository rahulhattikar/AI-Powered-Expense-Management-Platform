package com.AIExpense.ExpenseTracker.auth.dto;

public record AuthResponse(
        String token,
        String email,
        String name
) {
}
