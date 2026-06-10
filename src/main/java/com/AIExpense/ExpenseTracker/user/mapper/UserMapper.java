package com.AIExpense.ExpenseTracker.user.mapper;

import com.AIExpense.ExpenseTracker.user.dto.UserResponse;
import com.AIExpense.ExpenseTracker.user.entity.User;

public class UserMapper {
    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}

