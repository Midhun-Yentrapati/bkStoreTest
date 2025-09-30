package com.bookstore.user_authentication_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when service-level operations fail.
 * This includes external service failures, database errors, and infrastructure issues.
 */
@Getter
public class ServiceException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;
    private final String serviceName;
    
    public ServiceException(String message) {
        super(message);
        this.errorCode = "SERVICE_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = true;
        this.serviceName = null;
    }
    
    public ServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = true;
        this.serviceName = null;
    }
    
    public ServiceException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.retryable = true;
        this.serviceName = null;
    }
    
    public ServiceException(String message, String errorCode, HttpStatus httpStatus, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
        this.serviceName = null;
    }
    
    public ServiceException(String message, String errorCode, String serviceName) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = true;
        this.serviceName = serviceName;
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SERVICE_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = true;
        this.serviceName = null;
    }

    // Specific service error factory methods
    public static ServiceException emailServiceUnavailable() {
        return new ServiceException(
            "Email service is currently unavailable",
            "EMAIL_SERVICE_UNAVAILABLE",
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }
    
    public static ServiceException databaseConnectionError() {
        return new ServiceException(
            "Database connection error",
            "DATABASE_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    
    public static ServiceException externalServiceError(String serviceName) {
        return new ServiceException(
            "External service error: " + serviceName,
            "EXTERNAL_SERVICE_ERROR",
            serviceName
        ) {
            @Override
            public HttpStatus getHttpStatus() {
                return HttpStatus.BAD_GATEWAY;
            }
        };
    }
    
    public static ServiceException configurationError(String configName) {
        return new ServiceException(
            "Configuration error: " + configName,
            "CONFIGURATION_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR,
            false
        );
    }
    
    public static ServiceException networkTimeout(String operation) {
        return new ServiceException(
            "Network timeout during: " + operation,
            "NETWORK_TIMEOUT",
            HttpStatus.REQUEST_TIMEOUT
        );
    }
    
    public static ServiceException resourceExhausted(String resource) {
        return new ServiceException(
            "Resource exhausted: " + resource,
            "RESOURCE_EXHAUSTED",
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }
    
    public static ServiceException maintenanceMode() {
        return new ServiceException(
            "Service is currently under maintenance",
            "MAINTENANCE_MODE",
            HttpStatus.SERVICE_UNAVAILABLE,
            false
        );
    }
}
