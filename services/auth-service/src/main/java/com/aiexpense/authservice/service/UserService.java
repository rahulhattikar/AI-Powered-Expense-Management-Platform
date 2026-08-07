package com.aiexpense.authservice.service;

import com.aiexpense.authservice.dto.JwtResponse;
import com.aiexpense.authservice.dto.LoginRequest;
import com.aiexpense.authservice.dto.RegisterRequest;
import com.aiexpense.authservice.dto.UserDTO;
import com.aiexpense.authservice.entity.User;
import com.aiexpense.authservice.exception.InvalidCredentialsException;
import com.aiexpense.authservice.exception.UserAlreadyExistsException;
import com.aiexpense.authservice.exception.UserNotFoundException;
import com.aiexpense.authservice.repository.UserRepository;
import com.aiexpense.authservice.util.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;

    private final PasswordEncoder passwordEncoder;


    @Transactional
    public JwtResponse register(RegisterRequest request) {

        log.info("Creating user with username: {} and email: {} ", request.getUsername(),
                request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username '{}' already exists", request.getUsername());
            throw new UserAlreadyExistsException(
                    "Username '" + request.getUsername() + "' is already taken. Please choose another."
            );
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email '{}' already exists", request.getEmail());
            throw new UserAlreadyExistsException(
                    "Email '" + request.getEmail() + "' is already registered. Try login instead."
            );
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Registration failed: password confirmation mismatch");
            throw new InvalidCredentialsException("Passwords do not match.");
        }

        validatePasswordStrength(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName() != null ? request.getFirstName() : "")
                .lastName(request.getLastName() != null ? request.getLastName() : "")
                .isActive(true)
                .isEmailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: id={}, username={}, email={}",
                user.getId(), user.getUsername(), user.getEmail());

        String accessToken = jwtUtils.generateAccessToken(savedUser.getId(),
                user.getUsername(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(savedUser.getId(),
                user.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(UserDTO.fromEntity(savedUser))
                .build();


    }

    @Transactional
    public JwtResponse login(LoginRequest request) {

        log.info("Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: username '{}' not found", request.getUsername());
                    return new InvalidCredentialsException("Invalid username or password.");
                });


        if (!user.getIsActive()) {
            log.warn("Login failed: account for username '{}' is deactivated", request.getUsername());
            throw new InvalidCredentialsException("Your account has been deactivated. Contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for username '{}'", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        log.info("Login successful for username: {}", request.getUsername());

        String accessToken = jwtUtils.generateAccessToken(savedUser.getId(),
                user.getUsername(), user.getEmail());

        String refreshToken = jwtUtils.generateRefreshToken(savedUser.getId(),
                user.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(UserDTO.fromEntity(savedUser))
                .build();


    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {

        log.info("Getting user with id: {} ", userId);
        return userRepository.findById(userId)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> {
                    log.warn("User not found: id={}", userId);
                    return new UserNotFoundException("User with ID " + userId + " not found.");
                });
    }


    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new InvalidCredentialsException(
                    "Password must be at least 8 characters long."
            );
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidCredentialsException(
                    "Password must contain at least one uppercase letter (A-Z)."
            );
        }

        if (!password.matches(".*[a-z].*")) {
            throw new InvalidCredentialsException(
                    "Password must contain at least one lowercase letter (a-z)."
            );
        }

        if (!password.matches(".*[0-9].*")) {
            throw new InvalidCredentialsException(
                    "Password must contain at least one digit (0-9)."
            );
        }

        if (!password.matches(".*[!@#$%^&*()\\-_+=\\[\\]{};:'\",.<>?/\\\\|`~].*")) {
            throw new InvalidCredentialsException(
                    "Password must contain at least one special character (!@#$%^&* etc.)."
            );
        }
        log.debug("Password validation passed");
    }

    @Transactional
    public void deactivateAccount(Long userId) {
        log.info("deactivating user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setIsActive(false);
        userRepository.save(user);
        log.info("User account deactivated: id={}", userId);
    }
}
