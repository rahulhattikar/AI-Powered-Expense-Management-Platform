package com.AIExpense.ExpenseTracker.ai.dto;


import com.AIExpense.ExpenseTracker.report.dto.MonthlySummaryResponse;

import java.time.LocalDateTime;


public record ExpenseAnalysisResponse(
        String summary,
        MonthlySummaryResponse monthlySummaryResponse,
        LocalDateTime generatedAt
) {
}
