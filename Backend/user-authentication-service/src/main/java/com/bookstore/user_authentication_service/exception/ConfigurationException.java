package com.bookstore.user_authentication_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when configuration-related errors occur.
 * This includes missing properties, invalid configurations, and setup issues.
 */
@Getter
public class ConfigurationException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;
    private final String propertyName;
    private final String propertyValue;
    private final String configurationSection;
    
    public ConfigurationException(String message) {
        super(message);
        this.errorCode = "CONFIGURATION_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = false;
        this.propertyName = null;
        this.propertyValue = null;
        this.configurationSection = null;
    }
    
    public ConfigurationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = false;
        this.propertyName = null;
        this.propertyValue = null;
        this.configurationSection = null;
    }
    
    public ConfigurationException(String message, String errorCode, String propertyName) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = false;
        this.propertyName = propertyName;
        this.propertyValue = null;
        this.configurationSection = null;
    }
    
    public ConfigurationException(String message, String errorCode, String propertyName, String propertyValue) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = false;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.configurationSection = null;
    }
    
    public ConfigurationException(String message, String errorCode, String propertyName, String propertyValue, String configurationSection) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = false;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.configurationSection = configurationSection;
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CONFIGURATION_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = false;
        this.propertyName = null;
        this.propertyValue = null;
        this.configurationSection = null;
    }

    // Specific configuration error factory methods
    public static ConfigurationException missingProperty(String propertyName) {
        return new ConfigurationException(
            "Missing required configuration property: " + propertyName,
            "MISSING_CONFIGURATION",
            propertyName
        );
    }
    
    public static ConfigurationException invalidProperty(String propertyName, String propertyValue) {
        return new ConfigurationException(
            "Invalid configuration property '" + propertyName + "': " + propertyValue,
            "INVALID_CONFIGURATION",
            propertyName,
            propertyValue
        );
    }
    
    public static ConfigurationException invalidPropertyValue(String propertyName, String value) {
        return new ConfigurationException(
            "Invalid value '" + value + "' for configuration property: " + propertyName,
            "INVALID_PROPERTY_VALUE",
            propertyName
        );
    }
    
    public static ConfigurationException missingConfigurationSection(String sectionName) {
        return new ConfigurationException(
            "Missing required configuration section: " + sectionName,
            "MISSING_CONFIGURATION_SECTION",
            null,
            null,
            sectionName
        );
    }
    
    public static ConfigurationException databaseConfigurationError(String details) {
        return new ConfigurationException(
            "Database configuration error: " + details,
            "DATABASE_CONFIGURATION_ERROR"
        );
    }
    
    public static ConfigurationException jwtConfigurationError(String details) {
        return new ConfigurationException(
            "JWT configuration error: " + details,
            "JWT_CONFIGURATION_ERROR"
        );
    }
    
    public static ConfigurationException securityConfigurationError(String details) {
        return new ConfigurationException(
            "Security configuration error: " + details,
            "SECURITY_CONFIGURATION_ERROR"
        );
    }
    
    public static ConfigurationException emailConfigurationError(String details) {
        return new ConfigurationException(
            "Email configuration error: " + details,
            "EMAIL_CONFIGURATION_ERROR"
        );
    }
    
    public static ConfigurationException corsConfigurationError(String details) {
        return new ConfigurationException(
            "CORS configuration error: " + details,
            "CORS_CONFIGURATION_ERROR"
        );
    }
    
    public static ConfigurationException serviceDiscoveryConfigurationError(String details) {
        return new ConfigurationException(
            "Service discovery configuration error: " + details,
            "SERVICE_DISCOVERY_CONFIGURATION_ERROR"
        );
    }
}
