package com.AIExpense.ExpenseTracker.security;

import com.AIExpense.ExpenseTracker.util.AuthenticationUtils;
import com.AIExpense.ExpenseTracker.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final AuthenticationUtils authenticationUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, java.io.IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String jwt = extractJwt(authHeader);

            if (jwt == null) {
                log.debug("No JWT token found in Authorization header for request: {}",
                        request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }


            if (!jwtUtils.validateToken(jwt)) {
                log.warn("Invalid or expired JWT token for request: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtUtils.extractUserIdFromToken(jwt);
            if (userId == null) {
                log.warn("Could not extract userId from JWT token");
                filterChain.doFilter(request, response);
                return;
            }

            String email = (String) jwtUtils.extractClaim(jwt, "email");

            log.debug("JWT token validated successfully for userId: {}, email: {}",
                    userId, email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            null
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );


            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authentication set for userId: {} on request: {}",
                    userId, request.getRequestURI());

        } catch (Exception e) {
            log.error("Error processing JWT authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }


    private String extractJwt(String authHeader) {
        if (!StringUtils.hasText(authHeader)) {
            return null;
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.debug("Authorization header does not start with 'Bearer '");
            return null;
        }
        
        return authHeader.substring(7);
    }
}
