package com.AIExpense.ExpenseTracker.expense.dto;

import com.AIExpense.ExpenseTracker.expense.entity.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be Greater than zero")
        BigDecimal amount,
        String description,
        @NotNull(message = "Category is required")
        ExpenseCategory category,
        @NotNull(message = "Expense date is required")
        LocalDate expenseDate
) {
}
