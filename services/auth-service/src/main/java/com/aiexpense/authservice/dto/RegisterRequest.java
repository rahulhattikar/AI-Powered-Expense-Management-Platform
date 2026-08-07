package com.aiexpense.authservice.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatch
@Builder
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
    String username;

    @Email(message = "invalid email format")
    @NotBlank(message = "email is required")
    @Size(max = 100, message = "email must be less than 100 characters")
    String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password;

    @NotBlank(message = "confirm password is required")
    String confirmPassword;

    String firstName;
    String lastName;
}
