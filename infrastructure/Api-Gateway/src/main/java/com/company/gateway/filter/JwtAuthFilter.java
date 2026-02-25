package com.company.gateway.filter;

import com.company.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final JwtTokenProvider jwtTokenProvider;

    private final List<String> openEnpoints = List.of(
            "/api/v1/auth/",
            "/api/v1/categories",
            "/api/v1/products",
            "/api/v1/orders/public/",
            "/uploads/",
            "/actuator/"
    );

    private boolean isPublicEndpoint(String path) {
        if (openEnpoints.stream().anyMatch(path::startsWith)) {
            return true;
        }
        // /api/v1/seller/{id}/public
        return path.matches("/api/v1/seller/\\d+/public");
    }

    private boolean isAdminEndpoint(String path) {
        return path.startsWith("/api/v1/admin/");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = String.valueOf(exchange.getRequest().getMethod());

        log.info(">>> JwtAuthFilter: {} {}", method, path);

        // 0. Bypass CORS preflight
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            log.info(">>> BYPASS: OPTIONS request");
            return chain.filter(exchange);
        }

        // 1. Allow public endpoints
        boolean isOpen = isPublicEndpoint(path);
        log.info(">>> isOpen={} for path={}", isOpen, path);

        if (isOpen) {
            log.info(">>> PASS: Public endpoint");
            return chain.filter(exchange);
        }

        // 2. Read bearer token
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(">>> REJECT 401: No valid Authorization header for path={}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 3. Validate token signature/expiry
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn(">>> REJECT 401: Invalid token for path={}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 4. Enforce role-path policy at gateway
        Set<String> roles = jwtTokenProvider.extractRoles(token);
        boolean isAdmin = roles.contains("ADMIN");
        boolean adminPath = isAdminEndpoint(path);

        if (adminPath && !isAdmin) {
            log.warn(">>> REJECT 403: Non-admin token for admin path={}, roles={}", path, roles);
            return onError(exchange, HttpStatus.FORBIDDEN);
        }

        if (!adminPath && isAdmin) {
            log.warn(">>> REJECT 403: Admin token blocked on non-admin path={}, roles={}", path, roles);
            return onError(exchange, HttpStatus.FORBIDDEN);
        }

        // 5. Pass through
        log.info(">>> PASS: Valid token and role policy matched");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
