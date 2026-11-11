package com.aiexpense.expenseservice.event;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseEvent {

    private String eventId;
    private String eventType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Long expenseId;
    private Long userId;
    private String userEmail;
    private String description;
    private BigDecimal amount;
    private String category;
    private BigDecimal monthlyCategoryTotal;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    private BigDecimal previousAmount;
    private String previousCategory;

}
