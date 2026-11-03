package com.aiexpense.expenseservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyTrendResponse {

    private YearMonth month;
    private BigDecimal totalSpent;
    private BigDecimal previousMonthSpent;
    private BigDecimal percentageChange;

}
