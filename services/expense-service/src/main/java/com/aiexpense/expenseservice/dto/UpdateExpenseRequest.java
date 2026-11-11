package com.aiexpense.expenseservice.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpenseRequest {

    @Nullable
    @Size(min = 1, max = 500)
    private String description;

    @Nullable
    @DecimalMin("0.01")
    private BigDecimal amount;

    @Nullable
    private String category;

    @Nullable
    @PastOrPresent
    private LocalDate expenseDate;
}
