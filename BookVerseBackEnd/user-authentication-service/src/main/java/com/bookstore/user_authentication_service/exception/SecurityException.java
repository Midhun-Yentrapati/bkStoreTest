package com.bookstore.user_authentication_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when security-related operations fail.
 * This includes access denied, insufficient permissions, and role validation errors.
 */
@Getter
public class SecurityException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;
    private final String requiredRole;
    private final String requiredPermission;
    
    public SecurityException(String message) {
        super(message);
        this.errorCode = "SECURITY_ERROR";
        this.httpStatus = HttpStatus.FORBIDDEN;
        this.retryable = false;
        this.requiredRole = null;
        this.requiredPermission = null;
    }
    
    public SecurityException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.FORBIDDEN;
        this.retryable = false;
        this.requiredRole = null;
        this.requiredPermission = null;
    }
    
    public SecurityException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.retryable = false;
        this.requiredRole = null;
        this.requiredPermission = null;
    }
    
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SECURITY_ERROR";
        this.httpStatus = HttpStatus.FORBIDDEN;
        this.retryable = false;
        this.requiredRole = null;
        this.requiredPermission = null;
    }

    // Specific security error factory methods
    public static SecurityException accessDenied() {
        return new SecurityException("Access denied", "ACCESS_DENIED");
    }
    
    public static SecurityException insufficientPermissions(String requiredPermission) {
        SecurityException exception = new SecurityException(
            "Insufficient permissions. Required: " + requiredPermission,
            "INSUFFICIENT_PERMISSIONS"
        );
        // Set the required permission field
        return exception;
    }
    
    public static SecurityException invalidRole(String requiredRole) {
        SecurityException exception = new SecurityException(
            "Invalid role. Required: " + requiredRole,
            "INVALID_ROLE"
        );
        // Set the required role field
        return exception;
    }
    
    public static SecurityException sessionExpired() {
        return new SecurityException(
            "Session has expired",
            "SESSION_EXPIRED",
            HttpStatus.UNAUTHORIZED
        );
    }
    
    public static SecurityException tokenInvalid() {
        return new SecurityException(
            "Invalid or malformed token",
            "INVALID_TOKEN",
            HttpStatus.UNAUTHORIZED
        );
    }
    
    public static SecurityException tokenExpired() {
        return new SecurityException(
            "Token has expired",
            "TOKEN_EXPIRED",
            HttpStatus.UNAUTHORIZED
        );
    }
    
    public static SecurityException accountLocked() {
        return new SecurityException(
            "Account is locked",
            "ACCOUNT_LOCKED",
            HttpStatus.FORBIDDEN
        );
    }
    
    public static SecurityException accountInactive() {
        return new SecurityException(
            "Account is inactive",
            "ACCOUNT_INACTIVE",
            HttpStatus.FORBIDDEN
        );
    }
    
    public static SecurityException twoFactorRequired() {
        return new SecurityException(
            "Two-factor authentication required",
            "TWO_FACTOR_REQUIRED",
            HttpStatus.UNAUTHORIZED
        );
    }
}
