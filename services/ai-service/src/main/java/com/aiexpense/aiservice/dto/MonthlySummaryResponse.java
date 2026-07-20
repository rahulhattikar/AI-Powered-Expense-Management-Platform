package com.aiexpense.aiservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        int month,
        int year,
        BigDecimal totalSpent,
        int totalTransactions,
        BigDecimal averagePerDay,
        BigDecimal highestExpense,
        String highestSpendingCategory,
        List<CategoryBreakdown> categoryBreakdown
) {
}
