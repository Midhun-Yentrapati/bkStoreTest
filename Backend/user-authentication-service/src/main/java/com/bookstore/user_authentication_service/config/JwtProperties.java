package com.bookstore.user_authentication_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties
 * Binds jwt.* properties from application.properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT secret key for signing tokens
     */
    private String secret = "mySecretKey123456789012345678901234567890";
    
    /**
     * Access token expiration time in milliseconds (default: 24 hours)
     */
    private long expiration = 86400000L;
    
    /**
     * Refresh token expiration time in milliseconds (default: 7 days)
     */
    private long refreshExpiration = 604800000L;
    
    /**
     * Access token expiration time in milliseconds
     * Alias for expiration property
     */
    private long accessTokenExpiration = 86400000L;
    
    /**
     * Refresh token expiration time in milliseconds
     * Alias for refreshExpiration property
     */
    private long refreshTokenExpiration = 604800000L;
}
