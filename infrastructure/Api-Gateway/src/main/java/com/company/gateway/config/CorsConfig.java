package com.company.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Global CORS WebFilter for API Gateway.
 * Runs BEFORE all other filters (including JwtAuthFilter) to ensure
 * CORS headers are always present on every response.
 */
@Configuration
public class CorsConfig {

    private static final java.util.Set<String> ALLOWED_ORIGINS = java.util.Set.of(
        "http://localhost:5173",   // Frontend - User
        "http://localhost:5174",   // Frontend - Seller
        "http://localhost:5175",   // Frontend - Admin
        "http://localhost:3000"    // Dev fallback
    );
    private static final String ALLOWED_METHODS = "GET, POST, PUT, PATCH, DELETE, OPTIONS";
    private static final String ALLOWED_HEADERS = "*";
    private static final String MAX_AGE = "3600";

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            String origin = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ORIGIN);

            if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);

                // Handle preflight OPTIONS request immediately
                if (CorsUtils.isPreFlightRequest(exchange.getRequest())) {
                    exchange.getResponse().setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }

            return chain.filter(exchange);
        };
    }
}
