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
public class MonthlySummaryResponse {

    private Long userId;
    private Integer month;
    private Integer year;
    private BigDecimal totalSpent;
    private int totalTransactions;
    private BigDecimal averagePerDay;
    private BigDecimal highestExpense;
}
