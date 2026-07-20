package com.aiexpense.aiservice.dto;

import java.time.LocalDateTime;

public record ExpenseAnalysisResponse(
        String aiSummary,
        MonthlySummaryResponse monthlySummary,
        LocalDateTime generatedAt
) {}
