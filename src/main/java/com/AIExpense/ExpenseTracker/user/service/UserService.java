package com.AIExpense.ExpenseTracker.user.service;


import com.AIExpense.ExpenseTracker.user.dto.UserRequest;
import com.AIExpense.ExpenseTracker.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    UserResponse getUserByEmail(String email);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();
}
