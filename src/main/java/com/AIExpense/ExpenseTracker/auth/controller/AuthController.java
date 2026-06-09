package com.AIExpense.ExpenseTracker.auth.controller;

import com.AIExpense.ExpenseTracker.auth.dto.AuthResponse;
import com.AIExpense.ExpenseTracker.auth.dto.LoginRequest;
import com.AIExpense.ExpenseTracker.auth.dto.RegisterRequest;
import com.AIExpense.ExpenseTracker.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Call authService.register()
        // Return token in response
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Call authService.login()
        // Return token in response

        return ResponseEntity.ok(authService.login(request));
    }
}