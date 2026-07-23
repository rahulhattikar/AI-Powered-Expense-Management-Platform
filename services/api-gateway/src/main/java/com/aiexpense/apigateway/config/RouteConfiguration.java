package com.aiexpense.apigateway.config;

import com.aiexpense.apigateway.filter.JwtValidationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;


import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class RouteConfiguration {

    private final JwtValidationFilter jwtValidationFilter;

    public RouteConfiguration(JwtValidationFilter jwtValidationFilter) {
        this.jwtValidationFilter = jwtValidationFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> authRoute() {
        return route("auth-route")
                .route(path("/api/v1/auth/**"), http())
                .before(uri("http://app:8080"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> expenseTrackerRoute() {
        return route("expense-tracker-route")
                .route(
                        path("/api/v1/users/**")
                                .or(path("/api/v1/expenses/**"))
                                .or(path("/api/v1/budgets/**"))
                                .or(path("/api/v1/reports/**")),
                        http())
                .before(uri("http://app:8080"))
                .filter(jwtValidationFilter)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> aiServiceRoute() {
        return route("ai-service-route")
                .route(path("/api/v1/ai/**"), http())
                .before(uri("http://ai-service:8082"))
                .filter(jwtValidationFilter)
                .build();
    }
}