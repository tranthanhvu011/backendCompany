package com.company.gateway.filter;

import com.company.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final JwtTokenProvider jwtTokenProvider;

    private final List<String> openEnpoints = List.of("/api/auth/", "/actuator/");
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. Kiểm tra endpoint có public không
        boolean isOpen = openEnpoints.stream().anyMatch(path::startsWith);
        if (isOpen) {
            return chain.filter(exchange);  // Cho qua
        }

        // 2. Lấy token từ header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 3. Validate token
        if (!jwtTokenProvider.validateToken(token)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 4. Cho qua
        return chain.filter(exchange);
    }
    @Override
    public int getOrder() {
        return -1;  //
    }
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
