package com.AIExpense.ExpenseTracker.budget.mapper;

import com.AIExpense.ExpenseTracker.budget.dto.BudgetResponse;
import com.AIExpense.ExpenseTracker.budget.entity.Budget;

public class BudgetMapper {

    private BudgetMapper() {
    }
    

    public static BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory(),
                budget.getMonthlyLimit(),
                budget.getMonth(),
                budget.getYear(),
                budget.getUser().getId(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }


}
