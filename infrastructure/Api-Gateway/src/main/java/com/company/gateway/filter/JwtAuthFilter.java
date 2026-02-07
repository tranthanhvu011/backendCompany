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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final JwtTokenProvider jwtTokenProvider;

    private final List<String> openEnpoints = List.of("/api/v1/auth/", "/actuator/");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = String.valueOf(exchange.getRequest().getMethod());

        log.info(">>> JwtAuthFilter: {} {}", method, path);

        // 0. Bypass preflight CORS requests (OPTIONS)
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            log.info(">>> BYPASS: OPTIONS request");
            return chain.filter(exchange);
        }

        // 1. Kiểm tra endpoint có public không
        boolean isOpen = openEnpoints.stream().anyMatch(path::startsWith);
        log.info(">>> isOpen={} for path={}", isOpen, path);

        if (isOpen) {
            log.info(">>> PASS: Public endpoint");
            return chain.filter(exchange);
        }

        // 2. Lấy token từ header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(">>> REJECT 401: No valid Authorization header for path={}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 3. Validate token
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn(">>> REJECT 401: Invalid token for path={}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 4. Cho qua
        log.info(">>> PASS: Valid token");
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
