package com.AIExpense.ExpenseTracker.auth.service;

import com.AIExpense.ExpenseTracker.auth.dto.AuthResponse;
import com.AIExpense.ExpenseTracker.auth.dto.LoginRequest;
import com.AIExpense.ExpenseTracker.auth.dto.RegisterRequest;
import com.AIExpense.ExpenseTracker.auth.jwt.JwtUtil;
import com.AIExpense.ExpenseTracker.user.entity.Role;
import com.AIExpense.ExpenseTracker.user.entity.User;
import com.AIExpense.ExpenseTracker.common.exception.EmailAlreadyExistsException;
import com.AIExpense.ExpenseTracker.common.exception.UserNotFoundException;
import com.AIExpense.ExpenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {

        userRepository.findByEmail(request.email())
                .ifPresent(user -> {
                    throw new EmailAlreadyExistsException("Email already exists: " + request.email());
                });
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(user.getEmail()), user.getName(), user.getEmail());

    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.email()));
       String token =  jwtUtil.generateToken(request.email());
        return new AuthResponse(token, user.getName(), request.email());

    }
}
