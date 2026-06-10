package com.AIExpense.ExpenseTracker.util;

import com.AIExpense.ExpenseTracker.common.exception.AuthenticationException;
import com.AIExpense.ExpenseTracker.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationUtils {

    public User getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();

        Authentication authentication = context.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new AuthenticationException("Authentication principal is null");
        }

        if (principal instanceof String) {
            throw new AuthenticationException("Anonymous user is not allowed");
        }

        if (!(principal instanceof User user)) {
            throw new AuthenticationException("Invalid authentication principal type");
        }

        return user;
    }
}
