package com.bookstore.user_authentication_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when rate limits are exceeded.
 * This includes too many requests, login attempts, and other rate-limited operations.
 */
@Getter
public class RateLimitException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;
    private final long retryAfterSeconds;
    private final int maxAttempts;
    private final String rateLimitType;
    
    public RateLimitException(String message) {
        super(message);
        this.errorCode = "RATE_LIMIT_ERROR";
        this.httpStatus = HttpStatus.TOO_MANY_REQUESTS;
        this.retryable = true;
        this.retryAfterSeconds = 60;
        this.maxAttempts = 0;
        this.rateLimitType = "GENERAL";
    }
    
    public RateLimitException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.TOO_MANY_REQUESTS;
        this.retryable = true;
        this.retryAfterSeconds = 60;
        this.maxAttempts = 0;
        this.rateLimitType = "GENERAL";
    }
    
    public RateLimitException(String message, String errorCode, long retryAfterSeconds) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.TOO_MANY_REQUESTS;
        this.retryable = true;
        this.retryAfterSeconds = retryAfterSeconds;
        this.maxAttempts = 0;
        this.rateLimitType = "GENERAL";
    }
    
    public RateLimitException(String message, String errorCode, int maxAttempts) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.TOO_MANY_REQUESTS;
        this.retryable = true;
        this.retryAfterSeconds = 0;
        this.maxAttempts = maxAttempts;
        this.rateLimitType = "ATTEMPTS";
    }
    
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RATE_LIMIT_ERROR";
        this.httpStatus = HttpStatus.TOO_MANY_REQUESTS;
        this.retryable = true;
        this.retryAfterSeconds = 60;
        this.maxAttempts = 0;
        this.rateLimitType = "GENERAL";
    }

    // Specific rate limit error factory methods
    public static RateLimitException tooManyRequests(long retryAfterSeconds) {
        return new RateLimitException(
            "Too many requests. Please try again after " + retryAfterSeconds + " seconds",
            "RATE_LIMIT_EXCEEDED",
            retryAfterSeconds
        );
    }
    
    public static RateLimitException loginAttemptsExceeded(int maxAttempts) {
        return new RateLimitException(
            "Maximum login attempts (" + maxAttempts + ") exceeded",
            "LOGIN_ATTEMPTS_EXCEEDED",
            maxAttempts
        );
    }
    
    public static RateLimitException apiCallsExceeded(long retryAfterSeconds) {
        return new RateLimitException(
            "API rate limit exceeded. Please try again after " + retryAfterSeconds + " seconds",
            "API_RATE_LIMIT_EXCEEDED",
            retryAfterSeconds
        );
    }
    
    public static RateLimitException registrationAttemptsExceeded(long retryAfterSeconds) {
        return new RateLimitException(
            "Too many registration attempts. Please try again after " + retryAfterSeconds + " seconds",
            "REGISTRATION_RATE_LIMIT_EXCEEDED",
            retryAfterSeconds
        );
    }
    
    public static RateLimitException passwordResetAttemptsExceeded(long retryAfterSeconds) {
        return new RateLimitException(
            "Too many password reset attempts. Please try again after " + retryAfterSeconds + " seconds",
            "PASSWORD_RESET_RATE_LIMIT_EXCEEDED",
            retryAfterSeconds
        );
    }
    
    public static RateLimitException emailVerificationAttemptsExceeded(long retryAfterSeconds) {
        return new RateLimitException(
            "Too many email verification attempts. Please try again after " + retryAfterSeconds + " seconds",
            "EMAIL_VERIFICATION_RATE_LIMIT_EXCEEDED",
            retryAfterSeconds
        );
    }
    
    public static RateLimitException ipBlocked(long retryAfterSeconds) {
        return new RateLimitException(
            "IP address temporarily blocked due to suspicious activity. Please try again after " + retryAfterSeconds + " seconds",
            "IP_BLOCKED",
            retryAfterSeconds
        );
    }
}
