package com.aiexpense.expenseservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "expenses", indexes = {@Index(name = "idx_user_id", columnList = "user_id"), @Index(name = "idx_user_date", columnList = "user_id,expense_date"), @Index(name = "idx_category", columnList = "category")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;


    @PrePersist
    protected void onCreate() {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
    }

}
