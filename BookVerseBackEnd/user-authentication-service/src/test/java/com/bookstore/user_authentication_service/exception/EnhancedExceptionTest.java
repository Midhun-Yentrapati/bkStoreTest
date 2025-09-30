package com.bookstore.user_authentication_service.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for all custom exception classes
 * Provides detailed test coverage including constructors, methods, static factories, and edge cases
 */
@DisplayName("Enhanced Exception Classes Tests")
class EnhancedExceptionTest {

    @Nested
    @DisplayName("AddressNotFoundException Comprehensive Tests")
    class AddressNotFoundExceptionTest {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            String message = "Address not found with ID: 123";
            AddressNotFoundException exception = new AddressNotFoundException(message);
            
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
            assertTrue(exception instanceof RuntimeException);
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

        @Test
        @DisplayName("Should create exception using static factory methods")
        void shouldCreateExceptionUsingStaticFactoryMethods() {
            // Test addressNotFound factory method
            String addressId = "addr-123";
            AddressNotFoundException byId = AddressNotFoundException.addressNotFound(addressId);
            assertTrue(byId.getMessage().contains(addressId));
            assertTrue(byId.getMessage().contains("Address not found with ID"));
            
            // Test addressNotFoundForUser factory method
            String userId = "user-456";
            AddressNotFoundException byUserAndId = AddressNotFoundException.addressNotFoundForUser(addressId, userId);
            assertTrue(byUserAndId.getMessage().contains(addressId));
            assertTrue(byUserAndId.getMessage().contains(userId));
            assertTrue(byUserAndId.getMessage().contains("not found for user"));
        }

        @Test
        @DisplayName("Should handle null and empty parameters")
        void shouldHandleNullAndEmptyParameters() {
            // Test with null message
            AddressNotFoundException nullMessage = new AddressNotFoundException(null);
            assertNull(nullMessage.getMessage());
            
            // Test with empty message
            AddressNotFoundException emptyMessage = new AddressNotFoundException("");
            assertEquals("", emptyMessage.getMessage());
            
            // Test factory methods with null/empty parameters
            AddressNotFoundException nullId = AddressNotFoundException.addressNotFound(null);
            assertTrue(nullId.getMessage().contains("null"));
            
            AddressNotFoundException emptyId = AddressNotFoundException.addressNotFound("");
            assertTrue(emptyId.getMessage().contains("Address not found with ID"));
        }

        @Test
        @DisplayName("Should test exception inheritance and behavior")
        void shouldTestExceptionInheritanceAndBehavior() {
            AddressNotFoundException exception = new AddressNotFoundException("Test message");
            
            // Test inheritance
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
            
            // Test stack trace
            assertNotNull(exception.getStackTrace());
            assertTrue(exception.getStackTrace().length > 0);
            
            // Test toString
            String toString = exception.toString();
            assertNotNull(toString);
            assertTrue(toString.contains("AddressNotFoundException"));
            assertTrue(toString.contains("Test message"));
        }
    }

    @Nested
    @DisplayName("AuthenticationException Comprehensive Tests")
    class AuthenticationExceptionTest {

        @Test
        @DisplayName("Should create exception with all constructor variations")
        void shouldCreateExceptionWithAllConstructorVariations() {
            // Test message-only constructor
            String message = "Authentication failed";
            AuthenticationException messageOnly = new AuthenticationException(message);
            assertEquals(message, messageOnly.getMessage());
            assertEquals("AUTH_ERROR", messageOnly.getErrorCode());
            assertEquals(HttpStatus.UNAUTHORIZED, messageOnly.getHttpStatus());
            assertNull(messageOnly.getCause());

            // Test message and error code constructor
            String errorCode = "CUSTOM_AUTH_ERROR";
            AuthenticationException messageAndCode = new AuthenticationException(message, errorCode);
            assertEquals(message, messageAndCode.getMessage());
            assertEquals(errorCode, messageAndCode.getErrorCode());
            assertEquals(HttpStatus.UNAUTHORIZED, messageAndCode.getHttpStatus());

            // Test message and cause constructor
            Throwable cause = new RuntimeException("Token validation failed");
            AuthenticationException messageAndCause = new AuthenticationException(message, cause);
            assertEquals(message, messageAndCause.getMessage());
            assertEquals(cause, messageAndCause.getCause());
            assertEquals("AUTH_ERROR", messageAndCause.getErrorCode());

            // Test message, error code, and cause constructor
            AuthenticationException allParams = new AuthenticationException(message, errorCode, cause);
            assertEquals(message, allParams.getMessage());
            assertEquals(errorCode, allParams.getErrorCode());
            assertEquals(cause, allParams.getCause());
            assertEquals(HttpStatus.UNAUTHORIZED, allParams.getHttpStatus());
        }

        @Test
        @DisplayName("Should test all static factory methods")
        void shouldTestAllStaticFactoryMethods() {
            // Test invalidCredentials
            AuthenticationException invalidCreds = AuthenticationException.invalidCredentials();
            assertEquals("INVALID_CREDENTIALS", invalidCreds.getErrorCode());
            assertTrue(invalidCreds.getMessage().contains("Invalid username/email or password"));

            // Test accountLocked
            AuthenticationException locked = AuthenticationException.accountLocked();
            assertEquals("ACCOUNT_LOCKED", locked.getErrorCode());
            assertTrue(locked.getMessage().contains("locked"));

            // Test accountInactive
            AuthenticationException inactive = AuthenticationException.accountInactive();
            assertEquals("ACCOUNT_INACTIVE", inactive.getErrorCode());
            assertTrue(inactive.getMessage().contains("inactive"));

            // Test accountSuspended
            AuthenticationException suspended = AuthenticationException.accountSuspended();
            assertEquals("ACCOUNT_SUSPENDED", suspended.getErrorCode());
            assertTrue(suspended.getMessage().contains("suspended"));

            // Test accountDeleted
            AuthenticationException deleted = AuthenticationException.accountDeleted();
            assertEquals("ACCOUNT_DELETED", deleted.getErrorCode());
            assertTrue(deleted.getMessage().contains("deleted"));

            // Test emailNotVerified
            AuthenticationException emailNotVerified = AuthenticationException.emailNotVerified();
            assertEquals("EMAIL_NOT_VERIFIED", emailNotVerified.getErrorCode());
            assertTrue(emailNotVerified.getMessage().contains("not verified"));

            // Test tokenExpired
            AuthenticationException tokenExpired = AuthenticationException.tokenExpired();
            assertEquals("TOKEN_EXPIRED", tokenExpired.getErrorCode());
            assertTrue(tokenExpired.getMessage().contains("expired"));

            // Test tokenInvalid
            AuthenticationException tokenInvalid = AuthenticationException.tokenInvalid();
            assertEquals("TOKEN_INVALID", tokenInvalid.getErrorCode());
            assertTrue(tokenInvalid.getMessage().contains("Invalid"));

            // Test sessionExpired
            AuthenticationException sessionExpired = AuthenticationException.sessionExpired();
            assertEquals("SESSION_EXPIRED", sessionExpired.getErrorCode());
            assertTrue(sessionExpired.getMessage().contains("Session"));

            // Test insufficientPrivileges
            AuthenticationException insufficient = AuthenticationException.insufficientPrivileges();
            assertEquals("INSUFFICIENT_PRIVILEGES", insufficient.getErrorCode());
            assertTrue(insufficient.getMessage().contains("Insufficient"));

            // Test twoFactorRequired
            AuthenticationException twoFactor = AuthenticationException.twoFactorRequired();
            assertEquals("TWO_FACTOR_REQUIRED", twoFactor.getErrorCode());
            assertTrue(twoFactor.getMessage().contains("Two-factor"));
        }

        @Test
        @DisplayName("Should test getter methods and properties")
        void shouldTestGetterMethodsAndProperties() {
            String message = "Test authentication error";
            String errorCode = "TEST_ERROR";
            Throwable cause = new IllegalStateException("Test cause");
            
            AuthenticationException exception = new AuthenticationException(message, errorCode, cause);
            
            // Test all getters
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
            assertEquals(cause, exception.getCause());
            
            // Test that HttpStatus is always UNAUTHORIZED
            AuthenticationException defaultException = new AuthenticationException("Default");
            assertEquals(HttpStatus.UNAUTHORIZED, defaultException.getHttpStatus());
        }

        @Test
        @DisplayName("Should handle edge cases and null values")
        void shouldHandleEdgeCasesAndNullValues() {
            // Test with null message
            AuthenticationException nullMessage = new AuthenticationException(null);
            assertNull(nullMessage.getMessage());
            assertEquals("AUTH_ERROR", nullMessage.getErrorCode());

            // Test with null error code
            AuthenticationException nullCode = new AuthenticationException("Message", (String) null);
            assertNull(nullCode.getErrorCode());

            // Test with null cause
            AuthenticationException nullCause = new AuthenticationException("Message", (Throwable) null);
            assertNull(nullCause.getCause());

            // Test with empty strings
            AuthenticationException emptyMessage = new AuthenticationException("");
            assertEquals("", emptyMessage.getMessage());

            AuthenticationException emptyCode = new AuthenticationException("Message", "");
            assertEquals("", emptyCode.getErrorCode());
        }
    }

    @Nested
    @DisplayName("ValidationException Comprehensive Tests")
    class ValidationExceptionTest {

        @Test
        @DisplayName("Should create exception with all constructor variations")
        void shouldCreateExceptionWithAllConstructorVariations() {
            // Test message-only constructor
            String message = "Validation failed";
            ValidationException messageOnly = new ValidationException(message);
            assertEquals(message, messageOnly.getMessage());
            assertEquals("VALIDATION_ERROR", messageOnly.getErrorCode());
            assertEquals(HttpStatus.BAD_REQUEST, messageOnly.getHttpStatus());
            assertTrue(messageOnly.isRetryable());
            assertNull(messageOnly.getFieldErrors());
            assertFalse(messageOnly.hasFieldErrors());

            // Test message and error code constructor
            String errorCode = "CUSTOM_VALIDATION";
            ValidationException messageAndCode = new ValidationException(message, errorCode);
            assertEquals(errorCode, messageAndCode.getErrorCode());

            // Test message and field errors constructor
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("email", Arrays.asList("Email is required", "Invalid format"));
            fieldErrors.put("password", Collections.singletonList("Too short"));
            
            ValidationException messageAndFields = new ValidationException(message, fieldErrors);
            assertEquals(fieldErrors, messageAndFields.getFieldErrors());
            assertTrue(messageAndFields.hasFieldErrors());

            // Test message and cause constructor
            Throwable cause = new IllegalArgumentException("Invalid input");
            ValidationException messageAndCause = new ValidationException(message, cause);
            assertEquals(cause, messageAndCause.getCause());

            // Test message, error code, and cause constructor
            ValidationException allParams = new ValidationException(message, errorCode, cause);
            assertEquals(message, allParams.getMessage());
            assertEquals(errorCode, allParams.getErrorCode());
            assertEquals(cause, allParams.getCause());
        }

        @Test
        @DisplayName("Should test all static factory methods")
        void shouldTestAllStaticFactoryMethods() {
            // Test usernameAlreadyExists
            String username = "testuser";
            ValidationException usernameExists = ValidationException.usernameAlreadyExists(username);
            assertEquals("USERNAME_EXISTS", usernameExists.getErrorCode());
            assertTrue(usernameExists.getMessage().contains(username));

            // Test emailAlreadyExists
            String email = "test@example.com";
            ValidationException emailExists = ValidationException.emailAlreadyExists(email);
            assertEquals("EMAIL_EXISTS", emailExists.getErrorCode());
            assertTrue(emailExists.getMessage().contains(email));

            // Test employeeIdAlreadyExists
            String employeeId = "EMP001";
            ValidationException empIdExists = ValidationException.employeeIdAlreadyExists(employeeId);
            assertEquals("EMPLOYEE_ID_EXISTS", empIdExists.getErrorCode());
            assertTrue(empIdExists.getMessage().contains(employeeId));

            // Test passwordMismatch
            ValidationException passwordMismatch = ValidationException.passwordMismatch();
            assertEquals("PASSWORD_MISMATCH", passwordMismatch.getErrorCode());
            assertTrue(passwordMismatch.getMessage().contains("do not match"));

            // Test weakPassword
            ValidationException weakPassword = ValidationException.weakPassword();
            assertEquals("WEAK_PASSWORD", weakPassword.getErrorCode());
            assertTrue(weakPassword.getMessage().contains("security requirements"));

            // Test invalidEmailFormat
            ValidationException invalidEmail = ValidationException.invalidEmailFormat("invalid-email");
            assertEquals("INVALID_EMAIL", invalidEmail.getErrorCode());
            assertTrue(invalidEmail.getMessage().contains("invalid-email"));

            // Test invalidPhoneNumber
            ValidationException invalidPhone = ValidationException.invalidPhoneNumber("123");
            assertEquals("INVALID_PHONE", invalidPhone.getErrorCode());
            assertTrue(invalidPhone.getMessage().contains("123"));

            // Test invalidUserRole
            ValidationException invalidRole = ValidationException.invalidUserRole("INVALID_ROLE");
            assertEquals("INVALID_ROLE", invalidRole.getErrorCode());
            assertTrue(invalidRole.getMessage().contains("INVALID_ROLE"));

            // Test invalidDateOfBirth
            ValidationException invalidDob = ValidationException.invalidDateOfBirth();
            assertEquals("INVALID_DATE_OF_BIRTH", invalidDob.getErrorCode());
            assertTrue(invalidDob.getMessage().contains("past"));

            // Test missingRequiredField
            ValidationException missingField = ValidationException.missingRequiredField("username");
            assertEquals("MISSING_FIELD", missingField.getErrorCode());
            assertTrue(missingField.getMessage().contains("username"));
        }

        @Test
        @DisplayName("Should test field errors functionality")
        void shouldTestFieldErrorsFunctionality() {
            // Test with no field errors
            ValidationException noErrors = new ValidationException("No field errors");
            assertNull(noErrors.getFieldErrors());
            assertFalse(noErrors.hasFieldErrors());

            // Test with empty field errors map
            ValidationException emptyErrors = new ValidationException("Empty errors", new HashMap<>());
            assertNotNull(emptyErrors.getFieldErrors());
            assertFalse(emptyErrors.hasFieldErrors());

            // Test with field errors
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("field1", Arrays.asList("Error 1", "Error 2"));
            fieldErrors.put("field2", Collections.singletonList("Error 3"));
            
            ValidationException withErrors = new ValidationException("With errors", fieldErrors);
            assertEquals(fieldErrors, withErrors.getFieldErrors());
            assertTrue(withErrors.hasFieldErrors());
            assertEquals(2, withErrors.getFieldErrors().size());
            assertEquals(2, withErrors.getFieldErrors().get("field1").size());
            assertEquals(1, withErrors.getFieldErrors().get("field2").size());
        }

        @Test
        @DisplayName("Should test getter methods and properties")
        void shouldTestGetterMethodsAndProperties() {
            String message = "Test validation error";
            String errorCode = "TEST_VALIDATION";
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("testField", Collections.singletonList("Test error"));
            
            ValidationException exception = new ValidationException(message, fieldErrors);
            
            // Test all getters
            assertEquals(message, exception.getMessage());
            assertEquals("VALIDATION_ERROR", exception.getErrorCode()); // Default error code
            assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
            assertTrue(exception.isRetryable());
            assertEquals(fieldErrors, exception.getFieldErrors());
            assertTrue(exception.hasFieldErrors());
        }
    }
}
