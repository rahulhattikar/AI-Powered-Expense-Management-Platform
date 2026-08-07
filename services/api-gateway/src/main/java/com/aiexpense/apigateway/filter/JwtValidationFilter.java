package com.aiexpense.apigateway.filter;


import com.aiexpense.apigateway.exception.InvalidTokenException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;


@Component
@Slf4j
public class JwtValidationFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception{

        log.debug("JWT validation filter called for path: {}", request.path());

        try {
            List<String> authHeaders = request.headers().header("Authorization");

            if (authHeaders.isEmpty()) {
                log.warn("Missing Authorization header for path: {}", request.path());
                throw new InvalidTokenException("Missing Authorization header");
            }

            String authHeader = authHeaders.get(0);

            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Malformed Authorization header (not Bearer token) for path: {}", request.path());
                throw new InvalidTokenException("Authorization header must start with 'Bearer '");
            }

            String token = authHeader.substring(7);

            if (token.isBlank()) {
                log.warn("Empty bearer token for path: {}", request.path());
                throw new InvalidTokenException("Bearer token is empty");
            }

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            log.debug("JWT token validation successful for path: {}", request.path());

            return next.handle(request);

        } catch (JwtException e) {
            log.warn("JWT validation failed - invalid token for path: {}, error: {}",
                    request.path(), e.getMessage());
            throw new InvalidTokenException("Invalid or expired JWT token: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("JWT validation failed - malformed token for path: {}, error: {}",
                    request.path(), e.getMessage());
            throw new InvalidTokenException("Malformed JWT token: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error during JWT validation for path: {}",
                    request.path(), e);
            throw new InvalidTokenException("JWT validation error: " + e.getMessage());
        }
    }


    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


