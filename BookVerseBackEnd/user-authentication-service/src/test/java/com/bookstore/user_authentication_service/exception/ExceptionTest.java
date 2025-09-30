package com.bookstore.user_authentication_service.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for all custom exception classes
 * Ensures 100% code coverage for exception handling
 */
@DisplayName("Exception Classes Tests")
class ExceptionTest {

    @Nested
    @DisplayName("AddressNotFoundException Tests")
    class AddressNotFoundExceptionTest {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            String message = "Address not found with ID: 123";
            AddressNotFoundException exception = new AddressNotFoundException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Address lookup failed";
            Throwable cause = new RuntimeException("Database connection error");
            AddressNotFoundException exception = new AddressNotFoundException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("AuthenticationException Tests")
    class AuthenticationExceptionTest {

        @Test
        @DisplayName("Should create exception with message and error code")
        void shouldCreateExceptionWithMessageAndErrorCode() {
            String message = "Invalid credentials";
            String errorCode = "AUTH_001";
            AuthenticationException exception = new AuthenticationException(message, errorCode);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message, error code and cause")
        void shouldCreateExceptionWithMessageErrorCodeAndCause() {
            String message = "Authentication failed";
            String errorCode = "AUTH_002";
            Throwable cause = new RuntimeException("Token validation error");
            AuthenticationException exception = new AuthenticationException(message, errorCode, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message only")
        void shouldCreateExceptionWithMessageOnly() {
            String message = "User account locked";
            AuthenticationException exception = new AuthenticationException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getErrorCode());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Token expired";
            Throwable cause = new IllegalStateException("Token validation failed");
            AuthenticationException exception = new AuthenticationException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertNull(exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("UserNotFoundException Tests")
    class UserNotFoundExceptionTest {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            String message = "User not found with email: user@example.com";
            UserNotFoundException exception = new UserNotFoundException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "User lookup failed";
            Throwable cause = new RuntimeException("Database connection timeout");
            UserNotFoundException exception = new UserNotFoundException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should create exception using static factory methods")
        void shouldCreateExceptionUsingStaticFactoryMethods() {
            // Test static factory methods
            UserNotFoundException byId = UserNotFoundException.byId("123");
            assertTrue(byId.getMessage().contains("123"));
            
            UserNotFoundException byUsername = UserNotFoundException.byUsername("johndoe");
            assertTrue(byUsername.getMessage().contains("johndoe"));
            
            UserNotFoundException byEmail = UserNotFoundException.byEmail("john@example.com");
            assertTrue(byEmail.getMessage().contains("john@example.com"));
            
            UserNotFoundException byUsernameOrEmail = UserNotFoundException.byUsernameOrEmail("john");
            assertTrue(byUsernameOrEmail.getMessage().contains("john"));
            
            UserNotFoundException byEmployeeId = UserNotFoundException.byEmployeeId("EMP001");
            assertTrue(byEmployeeId.getMessage().contains("EMP001"));
        }
    }

    @Nested
    @DisplayName("ValidationException Tests")
    class ValidationExceptionTest {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            String message = "Validation failed: email format is invalid";
            ValidationException exception = new ValidationException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
            assertEquals("VALIDATION_ERROR", exception.getErrorCode());
            assertNull(exception.getFieldErrors());
        }

        @Test
        @DisplayName("Should create exception with message and error code")
        void shouldCreateExceptionWithMessageAndErrorCode() {
            String message = "Custom validation error";
            String errorCode = "CUSTOM_001";
            ValidationException exception = new ValidationException(message, errorCode);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertNull(exception.getFieldErrors());
        }

        @Test
        @DisplayName("Should create exception with message and field errors")
        void shouldCreateExceptionWithMessageAndFieldErrors() {
            String message = "Multiple validation errors";
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("email", Arrays.asList("Email is required", "Invalid email format"));
            fieldErrors.put("password", Collections.singletonList("Password is too short"));
            
            ValidationException exception = new ValidationException(message, fieldErrors);
            
            assertEquals(message, exception.getMessage());
            assertEquals(fieldErrors, exception.getFieldErrors());
            assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            String message = "Input validation error";
            Throwable cause = new RuntimeException("Regex pattern matching failed");
            ValidationException exception = new ValidationException(message, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should create exception with message, error code and cause")
        void shouldCreateExceptionWithMessageErrorCodeAndCause() {
            String message = "Complex validation error";
            String errorCode = "COMPLEX_001";
            Throwable cause = new IllegalArgumentException("Invalid input format");
            ValidationException exception = new ValidationException(message, errorCode, cause);
            
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("Should test ValidationException getter methods")
        void shouldTestValidationExceptionGetterMethods() {
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("username", Collections.singletonList("Username is required"));
            
            ValidationException exception = new ValidationException("Test message", fieldErrors);
            
            // Test all getter methods
            assertNotNull(exception.getFieldErrors());
            assertEquals("VALIDATION_ERROR", exception.getErrorCode());
            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertTrue(exception.isRetryable());
        }
    }

    @Nested
    @DisplayName("Simple Exception Tests")
    class SimpleExceptionTest {

        @Test
        @DisplayName("Should create ResourceNotFoundException")
        void shouldCreateResourceNotFoundException() {
            String message = "Resource not found";
            ResourceNotFoundException exception = new ResourceNotFoundException(message);
            assertEquals(message, exception.getMessage());
            
            Throwable cause = new RuntimeException("Database error");
            ResourceNotFoundException exceptionWithCause = new ResourceNotFoundException(message, cause);
            assertEquals(message, exceptionWithCause.getMessage());
            assertEquals(cause, exceptionWithCause.getCause());
        }

        @Test
        @DisplayName("Should create ServiceException")
        void shouldCreateServiceException() {
            String message = "Service operation failed";
            ServiceException exception = new ServiceException(message);
            assertEquals(message, exception.getMessage());
            
            Throwable cause = new RuntimeException("External service error");
            ServiceException exceptionWithCause = new ServiceException(message, cause);
            assertEquals(message, exceptionWithCause.getMessage());
            assertEquals(cause, exceptionWithCause.getCause());
        }

        @Test
        @DisplayName("Should create SecurityException")
        void shouldCreateSecurityException() {
            String message = "Access denied";
            SecurityException exception = new SecurityException(message);
            assertEquals(message, exception.getMessage());
            
            Throwable cause = new RuntimeException("Security validation failed");
            SecurityException exceptionWithCause = new SecurityException(message, cause);
            assertEquals(message, exceptionWithCause.getMessage());
            assertEquals(cause, exceptionWithCause.getCause());
        }

        @Test
        @DisplayName("Should create ConfigurationException")
        void shouldCreateConfigurationException() {
            String message = "Configuration error";
            ConfigurationException exception = new ConfigurationException(message);
            assertEquals(message, exception.getMessage());
            
            Throwable cause = new RuntimeException("Config file not found");
            ConfigurationException exceptionWithCause = new ConfigurationException(message, cause);
            assertEquals(message, exceptionWithCause.getMessage());
            assertEquals(cause, exceptionWithCause.getCause());
        }

        @Test
        @DisplayName("Should create RateLimitException")
        void shouldCreateRateLimitException() {
            String message = "Rate limit exceeded";
            RateLimitException exception = new RateLimitException(message);
            assertEquals(message, exception.getMessage());
            
            Throwable cause = new RuntimeException("Too many requests");
            RateLimitException exceptionWithCause = new RateLimitException(message, cause);
            assertEquals(message, exceptionWithCause.getMessage());
            assertEquals(cause, exceptionWithCause.getCause());
        }
    }

    @Nested
    @DisplayName("Exception Inheritance and Behavior Tests")
    class ExceptionInheritanceTest {

        @Test
        @DisplayName("Should verify exception inheritance hierarchy")
        void shouldVerifyExceptionInheritance() {
            // Test that all custom exceptions extend RuntimeException
            assertTrue(RuntimeException.class.isAssignableFrom(AddressNotFoundException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(AuthenticationException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(ConfigurationException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(RateLimitException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(ResourceNotFoundException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(SecurityException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(ServiceException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(UserNotFoundException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(ValidationException.class));
        }

        @Test
        @DisplayName("Should test exception stack trace")
        void shouldTestExceptionStackTrace() {
            try {
                throw new ValidationException("Test validation error");
            } catch (ValidationException e) {
                assertNotNull(e.getStackTrace());
                assertTrue(e.getStackTrace().length > 0);
                assertEquals("shouldTestExceptionStackTrace", e.getStackTrace()[0].getMethodName());
            }
        }

        @Test
        @DisplayName("Should test exception toString method")
        void shouldTestExceptionToString() {
            ValidationException exception = new ValidationException("Test message");
            String toString = exception.toString();
            assertNotNull(toString);
            assertTrue(toString.contains("ValidationException"));
            assertTrue(toString.contains("Test message"));
        }
    }
}
