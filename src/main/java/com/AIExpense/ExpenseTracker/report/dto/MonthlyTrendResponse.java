package com.AIExpense.ExpenseTracker.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyTrendResponse{
    int month;
    int year;
    String monthName;
    BigDecimal totalSpent;
    int totalTransactions;
    BigDecimal changeFromLastMonth;    // difference from previous month
    double changePercentage;
}
