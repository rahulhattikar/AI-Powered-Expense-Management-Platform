package com.aiexpense.aiservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ExpenseAnalysisRequest(
        @Min(1) @Max(12) int month,
        @Min(2000) int year
) {}
