package com.aiexpense.apigateway.config;

import com.aiexpense.apigateway.filter.JwtValidationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;


import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RouteConfiguration {

    private final JwtValidationFilter jwtValidationFilter;

    private static final String URI = "http://auth-service:8081";


    @Bean
    public RouterFunction<ServerResponse> authPublicRoute() {
        return route("auth-public")
                .route(
                        path("/api/v1/auth/register")
                                .or(path("/api/v1/auth/login"))
                                .or(path("/api/v1/auth/health")),
                        http()
                )
                .before(uri(URI))
                .build();
    }


    @Bean
    public RouterFunction<ServerResponse> authProtectedRoute() {
        return route("auth-protected")
                .route(
                        path("/api/v1/auth/me")
                                .or(path("/api/v1/auth/deactivate")),
                        http()
                )
                .before(uri(URI))
                .filter(jwtValidationFilter)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authInternalRoute() {
        return route("auth-internal")
                .route(path("/api/v1/auth/users/**"), http())
                .before(uri(URI))
                .build();
    }


    @Bean
    public RouterFunction<ServerResponse> appServiceRoute() {
        return route("app-service")
                .route(
                        path("/api/v1/expenses/**")
                                .or(path("/api/v1/budgets/**"))
                                .or(path("/api/v1/reports/**")),
                        http()
                )
                .before(uri("http://app:8080"))
                .filter(jwtValidationFilter)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> aiServiceRoute() {
        return route("ai-service")
                .route(path("/api/v1/ai/**"), http())
                .before(uri("http://ai-service:8082"))
                .filter(jwtValidationFilter)
                .build();
    }
}