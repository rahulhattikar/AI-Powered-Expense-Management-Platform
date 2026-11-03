package com.aiexpense.expenseservice.dto;

import com.aiexpense.expenseservice.entity.Expense;
import com.aiexpense.expenseservice.entity.ExpenseCategory;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {


    public  ExpenseResponse toResponse(Expense expense) {
        if (expense == null) return null;

        return ExpenseResponse.builder()
                .id(expense.getId())
                .userId(expense.getUserId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .category(expense.getCategory().name())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }


    public Expense toEntity(CreateExpenseRequest request, Long userId) {
        if (request == null) return null;

        return Expense.builder()
                .userId(userId)
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(ExpenseCategory.valueOf(request.getCategory()))
                .expenseDate(request.getExpenseDate())
                .isDeleted(false)
                .build();
    }


    public Expense updateEntity(Expense existing, UpdateExpenseRequest request) {
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            existing.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            existing.setCategory(ExpenseCategory.valueOf(request.getCategory()));
        }
        if (request.getExpenseDate() != null) {
            existing.setExpenseDate(request.getExpenseDate());
        }
        return existing;
    }
}
