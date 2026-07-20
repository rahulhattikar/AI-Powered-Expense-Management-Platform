package com.aiexpense.aiservice.dto;

import java.math.BigDecimal;

public record CategoryBreakdown(
        String category,
        BigDecimal totalSpent,
        double percentageOfTotal
) {
}
