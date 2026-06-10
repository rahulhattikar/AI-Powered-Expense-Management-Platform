package com.AIExpense.ExpenseTracker.auth.service;

import com.AIExpense.ExpenseTracker.auth.dto.AuthResponse;
import com.AIExpense.ExpenseTracker.auth.dto.LoginRequest;
import com.AIExpense.ExpenseTracker.auth.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
