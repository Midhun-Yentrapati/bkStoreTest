package com.bookstore.user_authentication_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
    
    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AUTH_ERROR";
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
    
    public AuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }

    // Specific authentication error types
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid username/email or password", "INVALID_CREDENTIALS");
    }
    
    public static AuthenticationException accountLocked() {
        return new AuthenticationException("Account is locked due to multiple failed login attempts", "ACCOUNT_LOCKED");
    }
    
    public static AuthenticationException accountInactive() {
        return new AuthenticationException("Account is inactive", "ACCOUNT_INACTIVE");
    }
    
    public static AuthenticationException accountSuspended() {
        return new AuthenticationException("Account has been suspended", "ACCOUNT_SUSPENDED");
    }
    
    public static AuthenticationException accountDeleted() {
        return new AuthenticationException("Account has been deleted", "ACCOUNT_DELETED");
    }
    
    public static AuthenticationException emailNotVerified() {
        return new AuthenticationException("Email address is not verified", "EMAIL_NOT_VERIFIED");
    }
    
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("Authentication token has expired", "TOKEN_EXPIRED");
    }
    
    public static AuthenticationException tokenInvalid() {
        return new AuthenticationException("Invalid authentication token", "TOKEN_INVALID");
    }
    
    public static AuthenticationException sessionExpired() {
        return new AuthenticationException("Session has expired", "SESSION_EXPIRED");
    }
    
    public static AuthenticationException insufficientPrivileges() {
        return new AuthenticationException("Insufficient privileges to access this resource", "INSUFFICIENT_PRIVILEGES");
    }
    
    public static AuthenticationException twoFactorRequired() {
        return new AuthenticationException("Two-factor authentication is required", "TWO_FACTOR_REQUIRED");
    }
}
