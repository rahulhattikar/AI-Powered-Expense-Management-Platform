package com.AIExpense.ExpenseTracker.util;


import com.AIExpense.ExpenseTracker.common.exception.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationUtils {

    private final JwtUtils jwtUtils;

    public Long getCurrentUserId() {
        try {

            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

            if (requestAttributes == null) {
                log.error("No request attributes found in context");
                throw new RuntimeException("No request context available");
            }

            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) requestAttributes;

            HttpServletRequest request = servletRequestAttributes.getRequest();

            if (request == null) {
                log.error("No HTTP request found in request attributes");
                throw new RuntimeException("No HTTP request available");
            }

            String authHeader = request.getHeader("Authorization");


            Long userId = validateAndExtractToken(authHeader);

            log.debug("Successfully extracted userId from token: {}", userId);
            return userId;

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error extracting userId from context: {}",
                    e.getMessage(), e);
            throw new RuntimeException("Failed to extract user ID from request", e);
        }
    }

    public Long validateAndExtractToken(String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank()) {
                log.warn("Missing Authorization header");
                throw new InvalidTokenException("Missing Authorization header");
            }

            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format");
                throw new InvalidTokenException("Authorization header must start with 'Bearer '");
            }

            String token = authHeader.substring(7);

            if (token.isBlank()) {
                log.warn("Empty bearer token");
                throw new InvalidTokenException("Bearer token is empty");
            }

            if (!jwtUtils.validateToken(token)) {
                log.warn("Invalid or expired JWT token");
                throw new InvalidTokenException("Invalid or expired JWT token");
            }


            Long userId = jwtUtils.extractUserIdFromToken(token);

            if (userId == null) {
                log.warn("userId claim not found in token");
                throw new InvalidTokenException("userId claim not found in token");
            }

            log.debug("Token validation successful, userId: {}", userId);
            return userId;

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage(), e);
            throw new InvalidTokenException("Failed to validate token");
        }
    }

    public <T> T extractClaimFromCurrentRequest(String claimName) {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

            if (requestAttributes == null) {
                throw new RuntimeException("No request context available");
            }

            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) requestAttributes;

            HttpServletRequest request = servletRequestAttributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            validateAndExtractToken(authHeader);

            String token = authHeader.substring(7);

            Object claimValue = jwtUtils.extractClaim(token, claimName);

            log.debug("Extracted claim '{}' from token", claimName);
            return (T) claimValue;

        } catch (Exception e) {
            log.error("Error extracting claim '{}': {}", claimName, e.getMessage(), e);
            return null;
        }
    }

}
