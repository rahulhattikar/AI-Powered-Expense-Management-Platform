package com.AIExpense.ExpenseTracker.report.dto;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
        int month,
        int year,
        String monthName,
        BigDecimal totalSpent,
        int totalTransactions,
        BigDecimal changeFromLastMonth,    // difference from previous month
        double changePercentage
) {
}
