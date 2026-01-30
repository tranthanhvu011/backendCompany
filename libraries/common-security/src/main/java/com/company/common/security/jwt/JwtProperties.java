package com.company.common.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration properties.
 * Configure via application.yaml:
 * 
 * jwt:
 *   secret: your-256-bit-secret
 *   expiration: 86400000  # 24 hours
 *   refresh-expiration: 604800000  # 7 days
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT secret key (minimum 256 bits for HS256)
     */
    private String secret = "default-secret-key-please-change-in-production-environment";

    /**
     * Access token expiration time in milliseconds (default: 24 hours)
     */
    private long expiration = 86400000;

    /**
     * Refresh token expiration time in milliseconds (default: 7 days)
     */
    private long refreshExpiration = 604800000;

    /**
     * Token issuer
     */
    private String issuer = "company-microservices";

    /**
     * Token prefix (e.g., "Bearer ")
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Header name for JWT token
     */
    private String headerName = "Authorization";
}
