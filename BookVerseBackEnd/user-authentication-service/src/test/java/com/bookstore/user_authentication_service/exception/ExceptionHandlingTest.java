package com.bookstore.user_authentication_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all custom exceptions
 * Tests exception creation, messages, and HTTP status codes
 */
class ExceptionHandlingTest {

    // ========== AUTHENTICATION EXCEPTION TESTS ==========

    @Test
    void testAuthenticationException_InvalidCredentials() {
        AuthenticationException exception = AuthenticationException.invalidCredentials();
        
        assertNotNull(exception);
        assertEquals("Invalid username/email or password", exception.getMessage());
        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
    }

    @Test
    void testAuthenticationException_AccountLocked() {
        AuthenticationException exception = AuthenticationException.accountLocked();
        
        assertNotNull(exception);
        assertEquals("Account is locked due to multiple failed login attempts", exception.getMessage());
        assertEquals("ACCOUNT_LOCKED", exception.getErrorCode());
    }

    @Test
    void testAuthenticationException_AccountInactive() {
        AuthenticationException exception = AuthenticationException.accountInactive();
        
        assertNotNull(exception);
        assertEquals("Account is inactive", exception.getMessage());
        assertEquals("ACCOUNT_INACTIVE", exception.getErrorCode());
    }

    @Test
    void testAuthenticationException_TokenInvalid() {
        AuthenticationException exception = AuthenticationException.tokenInvalid();
        
        assertNotNull(exception);
        assertEquals("Invalid authentication token", exception.getMessage());
        assertEquals("TOKEN_INVALID", exception.getErrorCode());
    }

    @Test
    void testAuthenticationException_TokenExpired() {
        AuthenticationException exception = AuthenticationException.tokenExpired();
        
        assertNotNull(exception);
        assertEquals("Authentication token has expired", exception.getMessage());
        assertEquals("TOKEN_EXPIRED", exception.getErrorCode());
    }

    @Test
    void testAuthenticationException_CustomMessage() {
        String customMessage = "Custom authentication error";
        AuthenticationException exception = new AuthenticationException(customMessage);
        
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
        assertEquals("AUTH_ERROR", exception.getErrorCode()); // Default error code
    }

    @Test
    void testAuthenticationException_WithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        AuthenticationException exception = new AuthenticationException("Authentication failed", cause);
        
        assertNotNull(exception);
        assertEquals("Authentication failed", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("AUTH_ERROR", exception.getErrorCode()); // Default error code
    }

    // ========== VALIDATION EXCEPTION TESTS ==========

    @Test
    void testValidationException_UsernameAlreadyExists() {
        String username = "existinguser";
        ValidationException exception = ValidationException.usernameAlreadyExists(username);
        
        assertNotNull(exception);
        assertEquals("Username 'existinguser' is already taken", exception.getMessage());
        assertFalse(exception.hasFieldErrors()); // No field-specific errors for this type
    }

    @Test
    void testValidationException_EmailAlreadyExists() {
        String email = "existing@example.com";
        ValidationException exception = ValidationException.emailAlreadyExists(email);
        
        assertNotNull(exception);
        assertEquals("Email 'existing@example.com' is already registered", exception.getMessage());
        assertFalse(exception.hasFieldErrors()); // No field-specific errors for this type
    }

    @Test
    void testValidationException_InvalidEmailFormat() {
        String email = "invalid-email";
        ValidationException exception = ValidationException.invalidEmailFormat(email);
        
        assertNotNull(exception);
        assertEquals("Invalid email format: invalid-email", exception.getMessage());
        assertFalse(exception.hasFieldErrors()); // No field-specific errors for this type
    }

    @Test
    void testValidationException_WeakPassword() {
        ValidationException exception = ValidationException.weakPassword();
        
        assertNotNull(exception);
        assertEquals("Password does not meet security requirements", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("WEAK_PASSWORD", exception.getErrorCode());
        assertTrue(exception.isRetryable());
    }

    @Test
    void testValidationException_InvalidPhoneNumber() {
        String phoneNumber = "invalid-phone";
        ValidationException exception = ValidationException.invalidPhoneNumber(phoneNumber);
        
        assertNotNull(exception);
        assertEquals("Invalid phone number format: invalid-phone", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("INVALID_PHONE", exception.getErrorCode());
        assertTrue(exception.isRetryable());
    }

    @Test
    void testValidationException_CustomMessage() {
        String customMessage = "Custom validation error";
        ValidationException exception = new ValidationException(customMessage);
        
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertFalse(exception.isRetryable());
    }

    @Test
    void testValidationException_WithErrorCode() {
        String message = "Validation failed";
        String errorCode = "CUSTOM_ERROR";
        ValidationException exception = new ValidationException(message, errorCode);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    void testValidationException_WithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        ValidationException exception = new ValidationException("Validation failed", cause);
        
        assertNotNull(exception);
        assertEquals("Validation failed", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    // ========== RESOURCE NOT FOUND EXCEPTION TESTS ==========

    @Test
    void testResourceNotFoundException_UserNotFound() {
        String userId = "user-123";
        ResourceNotFoundException exception = ResourceNotFoundException.userNotFound(userId);
        
        assertNotNull(exception);
        assertEquals("User not found with ID: user-123", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        assertEquals("User", exception.getResourceType());
        assertEquals(userId, exception.getResourceId());
        assertFalse(exception.isRetryable());
    }

    @Test
    void testResourceNotFoundException_SessionNotFound() {
        String sessionId = "session-123";
        ResourceNotFoundException exception = ResourceNotFoundException.sessionNotFound(sessionId);
        
        assertNotNull(exception);
        assertEquals("Session not found with ID: session-123", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("SESSION_NOT_FOUND", exception.getErrorCode());
        assertEquals("Session", exception.getResourceType());
        assertEquals(sessionId, exception.getResourceId());
        assertFalse(exception.isRetryable());
    }

    @Test
    void testResourceNotFoundException_AddressNotFound() {
        String addressId = "address-123";
        ResourceNotFoundException exception = ResourceNotFoundException.addressNotFound(addressId);
        
        assertNotNull(exception);
        assertEquals("Address not found with ID: address-123", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("ADDRESS_NOT_FOUND", exception.getErrorCode());
        assertEquals("Address", exception.getResourceType());
        assertEquals(addressId, exception.getResourceId());
    }

    @Test
    void testResourceNotFoundException_CustomResource() {
        String resourceType = "CustomResource";
        String resourceId = "custom-123";
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);
        
        assertNotNull(exception);
        assertEquals("CustomResource not found with ID: custom-123", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
    }

    @Test
    void testResourceNotFoundException_WithCustomMessage() {
        String customMessage = "Custom resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(customMessage);
        
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
    }

    // ========== SECURITY EXCEPTION TESTS ==========

    @Test
    void testSecurityException_AccessDenied() {
        SecurityException exception = SecurityException.accessDenied();
        
        assertNotNull(exception);
        assertEquals("Access denied", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("ACCESS_DENIED", exception.getErrorCode());
        assertFalse(exception.isRetryable());
    }

    @Test
    void testSecurityException_InsufficientPermissions() {
        String requiredPermission = "USER_WRITE";
        SecurityException exception = SecurityException.insufficientPermissions(requiredPermission);
        
        assertNotNull(exception);
        assertEquals("Insufficient permissions. Required: USER_WRITE", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("INSUFFICIENT_PERMISSIONS", exception.getErrorCode());
        assertEquals(requiredPermission, exception.getRequiredPermission());
        assertFalse(exception.isRetryable());
    }

    @Test
    void testSecurityException_InvalidRole() {
        String requiredRole = "ADMIN";
        SecurityException exception = SecurityException.invalidRole(requiredRole);
        
        assertNotNull(exception);
        assertEquals("Invalid role. Required: ADMIN", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("INVALID_ROLE", exception.getErrorCode());
        assertEquals(requiredRole, exception.getRequiredRole());
    }

    @Test
    void testSecurityException_SessionExpired() {
        SecurityException exception = SecurityException.sessionExpired();
        
        assertNotNull(exception);
        assertEquals("Session has expired", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("SESSION_EXPIRED", exception.getErrorCode());
        assertTrue(exception.isRetryable()); // User can re-authenticate
    }

    @Test
    void testSecurityException_CustomMessage() {
        String customMessage = "Custom security error";
        SecurityException exception = new SecurityException(customMessage);
        
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("SECURITY_ERROR", exception.getErrorCode());
    }

    // ========== SERVICE EXCEPTION TESTS ==========

    @Test
    void testServiceException_EmailServiceUnavailable() {
        ServiceException exception = ServiceException.emailServiceUnavailable();
        
        assertNotNull(exception);
        assertEquals("Email service is currently unavailable", exception.getMessage());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getHttpStatus());
        assertEquals("EMAIL_SERVICE_UNAVAILABLE", exception.getErrorCode());
        assertTrue(exception.isRetryable());
    }

    @Test
    void testServiceException_DatabaseConnectionError() {
        ServiceException exception = ServiceException.databaseConnectionError();
        
        assertNotNull(exception);
        assertEquals("Database connection error", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("DATABASE_ERROR", exception.getErrorCode());
        assertTrue(exception.isRetryable());
    }

    @Test
    void testServiceException_ExternalServiceError() {
        String serviceName = "PaymentService";
        ServiceException exception = ServiceException.externalServiceError(serviceName);
        
        assertNotNull(exception);
        assertEquals("External service error: PaymentService", exception.getMessage());
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getHttpStatus());
        assertEquals("EXTERNAL_SERVICE_ERROR", exception.getErrorCode());
        assertEquals(serviceName, exception.getServiceName());
        assertTrue(exception.isRetryable());
    }

    @Test
    void testServiceException_CustomMessage() {
        String customMessage = "Custom service error";
        ServiceException exception = new ServiceException(customMessage);
        
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("SERVICE_ERROR", exception.getErrorCode());
    }

    @Test
    void testServiceException_WithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        ServiceException exception = new ServiceException("Service failed", cause);
        
        assertNotNull(exception);
        assertEquals("Service failed", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }

    // ========== RATE LIMIT EXCEPTION TESTS ==========

    @Test
    void testRateLimitException_TooManyRequests() {
        long retryAfterSeconds = 60;
        RateLimitException exception = RateLimitException.tooManyRequests(retryAfterSeconds);
        
        assertNotNull(exception);
        assertEquals("Too many requests. Please try again after 60 seconds", exception.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getHttpStatus());
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
        assertEquals(retryAfterSeconds, exception.getRetryAfterSeconds());
        assertTrue(exception.isRetryable());
    }

    @Test
    void testRateLimitException_LoginAttemptsExceeded() {
        int maxAttempts = 5;
        RateLimitException exception = RateLimitException.loginAttemptsExceeded(maxAttempts);
        
        assertNotNull(exception);
        assertEquals("Maximum login attempts (5) exceeded", exception.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getHttpStatus());
        assertEquals("LOGIN_ATTEMPTS_EXCEEDED", exception.getErrorCode());
        assertEquals(maxAttempts, exception.getMaxAttempts());
    }

    @Test
    void testRateLimitException_CustomMessage() {
        String customMessage = "Custom rate limit error";
        RateLimitException exception = new RateLimitException(customMessage);
        
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getHttpStatus());
        assertEquals("RATE_LIMIT_ERROR", exception.getErrorCode());
    }

    // ========== CONFIGURATION EXCEPTION TESTS ==========

    @Test
    void testConfigurationException_MissingProperty() {
        String propertyName = "jwt.secret";
        ConfigurationException exception = ConfigurationException.missingProperty(propertyName);
        
        assertNotNull(exception);
        assertEquals("Missing required configuration property: jwt.secret", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("MISSING_CONFIGURATION", exception.getErrorCode());
        assertEquals(propertyName, exception.getPropertyName());
        assertFalse(exception.isRetryable());
    }

    @Test
    void testConfigurationException_InvalidProperty() {
        String propertyName = "jwt.expiration";
        String propertyValue = "invalid-value";
        ConfigurationException exception = ConfigurationException.invalidProperty(propertyName, propertyValue);
        
        assertNotNull(exception);
        assertEquals("Invalid configuration property 'jwt.expiration': invalid-value", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("INVALID_CONFIGURATION", exception.getErrorCode());
        assertEquals(propertyName, exception.getPropertyName());
        assertEquals(propertyValue, exception.getPropertyValue());
    }

    @Test
    void testConfigurationException_CustomMessage() {
        String customMessage = "Custom configuration error";
        ConfigurationException exception = new ConfigurationException(customMessage);
        
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("CONFIGURATION_ERROR", exception.getErrorCode());
    }

    // ========== EXCEPTION HIERARCHY TESTS ==========

    @Test
    void testExceptionHierarchy() {
        // Test that all custom exceptions extend RuntimeException
        assertTrue(AuthenticationException.invalidCredentials() instanceof RuntimeException);
        assertTrue(ValidationException.usernameAlreadyExists("test") instanceof RuntimeException);
        assertTrue(ResourceNotFoundException.userNotFound("test") instanceof RuntimeException);
        assertTrue(SecurityException.accessDenied() instanceof RuntimeException);
        assertTrue(ServiceException.emailServiceUnavailable() instanceof RuntimeException);
        assertTrue(RateLimitException.tooManyRequests(60) instanceof RuntimeException);
        assertTrue(ConfigurationException.missingProperty("test") instanceof RuntimeException);
    }

    @Test
    void testExceptionSerialization() {
        // Test that exceptions can be serialized (important for distributed systems)
        AuthenticationException exception = AuthenticationException.invalidCredentials();
        
        assertNotNull(exception.getMessage());
        assertNotNull(exception.getHttpStatus());
        
        // Test that exception maintains its properties after creation
        assertEquals("Invalid username/email or password", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void testExceptionEquality() {
        // Test exception equality based on message and type
        AuthenticationException exception1 = AuthenticationException.invalidCredentials();
        AuthenticationException exception2 = AuthenticationException.invalidCredentials();
        
        assertEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals(exception1.getHttpStatus(), exception2.getHttpStatus());
        assertEquals(exception1.getClass(), exception2.getClass());
    }
}
