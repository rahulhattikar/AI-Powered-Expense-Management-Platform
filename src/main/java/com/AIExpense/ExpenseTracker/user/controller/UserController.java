package com.AIExpense.ExpenseTracker.user.controller;

import com.AIExpense.ExpenseTracker.user.dto.UserRequest;
import com.AIExpense.ExpenseTracker.user.dto.UserResponse;
import com.AIExpense.ExpenseTracker.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users",description = "Endpoints for creating and retrieving user information")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    @Operation(
            summary = "Create a new user",
            description = "Creates a new user entry for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse user = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(
            summary = "Get user by id",
            description = "Fetches user details for a given user id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("REST request to get user by id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Get a user by email",
            description = "Fetches user details for a given user email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("REST request to get user by email: {}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(
            summary = "Get all users",
            description = "Fetches  all user entries"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users fetched successfully"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("REST request to get all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }


}
