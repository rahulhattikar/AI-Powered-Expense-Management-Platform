package com.aiexpense.authservice.controller;

import com.aiexpense.authservice.dto.JwtResponse;
import com.aiexpense.authservice.dto.LoginRequest;
import com.aiexpense.authservice.dto.RegisterRequest;
import com.aiexpense.authservice.dto.UserDTO;
import com.aiexpense.authservice.exception.InvalidCredentialsException;
import com.aiexpense.authservice.exception.UserAlreadyExistsException;
import com.aiexpense.authservice.service.UserService;
import com.aiexpense.authservice.util.JwtUtils;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "User registration, login, and profile management")
public class AuthController {


    private final UserService userService;

    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates new user and returns JWT tokens")
    public ResponseEntity<JwtResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request for username: {}", request.getUsername());

        try {
            JwtResponse response = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserAlreadyExistsException | InvalidCredentialsException e) {
            throw e;
        }
    }


    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT tokens")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request for username: {}", request.getUsername());

        try {
            JwtResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException e) {
            throw e;
        }
    }


    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns authenticated user's profile")
    public ResponseEntity<UserDTO> getCurrentUser(
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {

        log.debug("GET /me request");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        String token = authHeader.substring(7);

        if (!jwtUtils.validateToken(token)) {
            log.warn("Invalid JWT token in /me request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        Long userId = jwtUtils.extractUserIdFromToken(token);
        if (userId == null) {
            log.warn("Failed to extract userId from token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        log.info("Fetching user profile for userId: {}", userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserById(userId));

    }


    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        log.info("Deactivate account request");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        Long userId = jwtUtils.extractUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to extract userId");
        }

        userService.deactivateAccount(userId);

        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
            put("message", "Account deactivated successfully.");
        }});
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "UP");
        map.put("service", "auth-service");
        map.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(map);
    }


}
