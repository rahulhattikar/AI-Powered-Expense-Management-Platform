package com.AIExpense.ExpenseTracker.mapper;

import com.AIExpense.ExpenseTracker.dto.UserResponse;
import com.AIExpense.ExpenseTracker.entity.User;

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

