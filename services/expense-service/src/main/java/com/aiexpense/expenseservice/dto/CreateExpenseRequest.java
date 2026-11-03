package com.aiexpense.expenseservice.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CreateExpenseRequest {

    @NotBlank(message = "Description is required")
    @Size(min = 1, max = 500, message = "Description must be 1-500 chars")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be > 0.01")
    @DecimalMax(value = "999999.99", message = "Amount too large")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(FOOD|TRANSPORT|ENTERTAINMENT|UTILITIES|HEALTHCARE" + "|EDUCATION|SHOPPING|PERSONAL_CARE|INSURANCE|OTHER)$", message = "Invalid category")
    private String category;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in future")
    private LocalDate expenseDate;

}
