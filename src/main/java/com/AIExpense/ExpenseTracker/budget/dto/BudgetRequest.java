package com.AIExpense.ExpenseTracker.budget.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;


public record BudgetRequest(
        @NotNull(message = "Category is required")
        ExpenseCategory category,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount should be greater than 0")
        BigDecimal monthlyLimit,
        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        int month,
        @NotNull(message = "Year is required")
        @Min(value = 2000, message = "Year must be valid")
        int year

) {
}
