package com.AIExpense.ExpenseTracker.service;


import com.AIExpense.ExpenseTracker.dto.UserRequest;
import com.AIExpense.ExpenseTracker.dto.UserResponse;
import com.AIExpense.ExpenseTracker.exception.EmailAlreadyExistsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    public UserResponse createUser(UserRequest userRequest);
    public UserResponse getUserByEmail(String email);
    public UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
}
