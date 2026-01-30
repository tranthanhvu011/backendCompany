package com.company.common.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties.
 * Configure in application.yaml:
 * 
 * jwt:
 *   secret: your-256-bit-secret-key
 *   expiration: 86400000        # 24 hours in milliseconds
 *   refresh-expiration: 604800000  # 7 days in milliseconds
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * Secret key for signing JWT tokens.
     * Must be at least 256 bits (32 characters) for HS256 algorithm.
     */
    private String secret = "default-secret-key-please-change-in-production-environment";
    
    /**
     * Access token expiration time in milliseconds.
     * Default: 24 hours
     */
    private long expiration = 86400000;
    
    /**
     * Refresh token expiration time in milliseconds.
     * Default: 7 days
     */
    private long refreshExpiration = 604800000;
    
    /**
     * Token prefix in Authorization header.
     * Default: "Bearer "
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * Header name for JWT token.
     * Default: "Authorization"
     */
    private String headerName = "Authorization";
}
