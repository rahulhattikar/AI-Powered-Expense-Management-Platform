package com.AIExpense.ExpenseTracker.expense.mapper;

import com.AIExpense.ExpenseTracker.expense.dto.ExpenseResponse;
import com.AIExpense.ExpenseTracker.expense.dto.PagedResponse;
import com.AIExpense.ExpenseTracker.expense.entity.Expense;


public class ExpenseMapper {

    private ExpenseMapper() {
    }

    public static ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(expense.getId(),
                expense.getAmount(), expense.getDescription(),
                expense.getCategory(), expense.getExpenseDate(),
                expense.getUser().getId(), expense.getCreatedAt(),
                expense.getUpdatedAt());
    }


}
