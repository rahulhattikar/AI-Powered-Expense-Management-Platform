package com.aiexpense.authservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponse {

    String accessToken;
    String refreshToken;
    String tokenType;
    Long expiresIn;
    UserDTO user;
}
