package com.AIExpense.ExpenseTracker.util;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;


@Component
@Slf4j
public class JwtUtils {

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.jwt.expiration-ms}")
    private Long jwtExpirationMs;

    @Value("${spring.jwt.refresh-expiration-ms}")
    private Long jwtRefreshExpirationMs;


    public boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                log.warn("Token is null or empty");
                return false;
            }

            Jwts
                    .parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            log.debug("JWT token validation successful");
            return true;

        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token format: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected JWT validation error: {}", e.getMessage(), e);
            return false;
        }
    }

    public Long extractUserIdFromToken(String token) {
        try {
            Object userId = extractClaim(token, "userId");
            if (userId == null) {
                log.warn("userId claim not found in token");
                return null;
            }

            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            } else if (userId instanceof String) {
                return Long.parseLong((String) userId);
            }

            log.warn("userId claim has unexpected type: {}", userId.getClass().getSimpleName());
            return null;

        } catch (NumberFormatException e) {
            log.warn("Failed to parse userId as Long: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error extracting userId from token: {}", e.getMessage(), e);
            return null;
        }
    }

    public Object extractClaim(String token, String claimName) {
        try {
            if (token == null || token.isBlank()) {
                log.warn("Token is null or empty");
                return null;
            }
            if (claimName == null || claimName.isBlank()) {
                log.warn("Claim name is null or empty");
                return null;
            }

            Claims claims = Jwts
                    .parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object claim = claims.get(claimName);
            if (claim == null) {
                log.debug("Claim '{}' not found in token", claimName);
            } else {
                log.debug("Successfully extracted claim: {}", claimName);
            }

            return claim;

        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Invalid signature while extracting claim '{}': {}", claimName, e.getMessage());
            return null;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired while extracting claim '{}': {}", claimName, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error extracting claim '{}' from token: {}", claimName, e.getMessage(), e);
            return null;
        }

    }

    private SecretKey getSigningKey() {
        try {
            byte[] bytes = Base64.getDecoder().decode(jwtSecret);
            return Keys.hmacShaKeyFor(bytes);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 encoding for JWT secret: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT secret configuration", e);
        }
    }
}

