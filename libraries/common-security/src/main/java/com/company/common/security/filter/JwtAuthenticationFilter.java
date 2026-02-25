package com.company.common.security.filter;

import com.company.common.security.jwt.JwtProperties;
import com.company.common.security.jwt.JwtTokenProvider;
import com.company.common.security.model.UserPrincipal;
import com.company.common.security.service.UserBlacklistChecker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

/**
 * JWT Authentication Filter.
 * Intercepts requests and validates JWT tokens.
 * Also checks user blacklist for force-logged-out users (if a checker is available).
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    // Optional: only available in services that provide a UserBlacklistChecker bean
    @Autowired(required = false)
    private UserBlacklistChecker userBlacklistChecker;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, JwtProperties jwtProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.extractUsername(jwt);
                Long userId = jwtTokenProvider.extractUserId(jwt);

                // Check if user is blacklisted (admin disabled their account)
                if (userBlacklistChecker != null && userBlacklistChecker.isBlacklisted(userId)) {
                    log.warn("Blocked request from blacklisted user: {} ({})", username, userId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"message\":\"Tài khoản đã bị vô hiệu hóa\",\"code\":\"ACCOUNT_DISABLED\"}");
                    return;
                }

                Set<String> roles = jwtTokenProvider.extractRoles(jwt);

                UserPrincipal userPrincipal = UserPrincipal.fromToken(userId, username, roles);

                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal, 
                        null, 
                        userPrincipal.getAuthorities()
                    );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Set user info in request attributes for easy access
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeaderName());
        
        if (StringUtils.hasText(bearerToken) && 
            bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            return bearerToken.substring(jwtProperties.getTokenPrefix().length());
        }
        
        return null;
    }
}


