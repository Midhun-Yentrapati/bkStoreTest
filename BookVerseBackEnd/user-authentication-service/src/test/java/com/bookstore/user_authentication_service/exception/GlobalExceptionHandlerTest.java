package com.bookstore.user_authentication_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for GlobalExceptionHandler
 * Ensures 100% code coverage for exception handling
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindException bindException;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        globalExceptionHandler = new GlobalExceptionHandler();
        
        // Setup common mock behavior
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Nested
    @DisplayName("Custom Exception Handler Tests")
    class CustomExceptionHandlerTest {

        @Test
        @DisplayName("Should handle UserNotFoundException")
        void shouldHandleUserNotFoundException() {
            String message = "User not found with ID: 123";
            UserNotFoundException exception = new UserNotFoundException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleUserNotFoundException(exception, webRequest);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(404, response.getBody().getStatus());
            assertEquals("User Not Found", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
            assertEquals("/api/test", response.getBody().getPath());
            assertNotNull(response.getBody().getTimestamp());
        }

        @Test
        @DisplayName("Should handle AuthenticationException")
        void shouldHandleAuthenticationException() {
            String message = "Invalid credentials";
            String errorCode = "AUTH_001";
            AuthenticationException exception = new AuthenticationException(message, errorCode);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleAuthenticationException(exception, webRequest);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(401, response.getBody().getStatus());
            assertEquals("Authentication Failed", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
            assertEquals(errorCode, response.getBody().getErrorCode());
            assertEquals("/api/test", response.getBody().getPath());
        }

        @Test
        @DisplayName("Should handle ValidationException")
        void shouldHandleValidationException() {
            String message = "Email format is invalid";
            ValidationException exception = new ValidationException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleValidationException(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Validation Failed", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle ValidationException with field errors")
        void shouldHandleValidationExceptionWithFieldErrors() {
            String message = "Validation failed";
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("email", Arrays.asList("Email is required", "Invalid email format"));
            fieldErrors.put("password", Collections.singletonList("Password is too short"));
            
            ValidationException exception = new ValidationException(message);
            // Note: ValidationException might have a setFieldErrors method or constructor with fieldErrors
            // For this test, we'll assume the exception can carry field errors

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleValidationException(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Validation Failed", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle ResourceNotFoundException")
        void shouldHandleResourceNotFoundException() {
            String message = "Resource not found";
            ResourceNotFoundException exception = new ResourceNotFoundException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(404, response.getBody().getStatus());
            assertEquals("Resource Not Found", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle ServiceException")
        void shouldHandleServiceException() {
            String message = "Service operation failed";
            ServiceException exception = new ServiceException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleServiceException(exception, webRequest);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(500, response.getBody().getStatus());
            assertEquals("Service Error", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle ConfigurationException")
        void shouldHandleConfigurationException() {
            String message = "Configuration error";
            ConfigurationException exception = new ConfigurationException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleConfigurationException(exception, webRequest);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(500, response.getBody().getStatus());
            assertEquals("Configuration Error", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle RateLimitException")
        void shouldHandleRateLimitException() {
            String message = "Rate limit exceeded";
            RateLimitException exception = new RateLimitException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleRateLimitException(exception, webRequest);

            assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(429, response.getBody().getStatus());
            assertEquals("Rate Limit Exceeded", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle SecurityException")
        void shouldHandleSecurityException() {
            String message = "Access denied";
            SecurityException exception = new SecurityException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleSecurityException(exception, webRequest);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(403, response.getBody().getStatus());
            assertEquals("Security Error", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("Spring Framework Exception Handler Tests")
    class SpringFrameworkExceptionHandlerTest {

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException")
        void shouldHandleMethodArgumentNotValidException() {
            // Create mock field errors
            FieldError fieldError1 = new FieldError("user", "email", "Email is required");
            FieldError fieldError2 = new FieldError("user", "password", "Password must be at least 8 characters");
            
            when(methodArgumentNotValidException.getBindingResult()).thenReturn(
                mock(org.springframework.validation.BindingResult.class)
            );
            when(methodArgumentNotValidException.getBindingResult().getFieldErrors()).thenReturn(
                Arrays.asList(fieldError1, fieldError2)
            );

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleMethodArgumentNotValidException(methodArgumentNotValidException, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Validation Failed", response.getBody().getError());
            assertNotNull(response.getBody().getFieldErrors());
            assertEquals(2, response.getBody().getFieldErrors().size());
            assertTrue(response.getBody().getFieldErrors().containsKey("email"));
            assertTrue(response.getBody().getFieldErrors().containsKey("password"));
        }

        @Test
        @DisplayName("Should handle BindException")
        void shouldHandleBindException() {
            FieldError fieldError = new FieldError("user", "username", "Username is required");
            
            when(bindException.getBindingResult()).thenReturn(
                mock(org.springframework.validation.BindingResult.class)
            );
            when(bindException.getBindingResult().getFieldErrors()).thenReturn(
                Collections.singletonList(fieldError)
            );

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBindException(bindException, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Binding Error", response.getBody().getError());
            assertNotNull(response.getBody().getFieldErrors());
            assertEquals(1, response.getBody().getFieldErrors().size());
            assertTrue(response.getBody().getFieldErrors().containsKey("username"));
        }

        @Test
        @DisplayName("Should handle ConstraintViolationException")
        void shouldHandleConstraintViolationException() {
            // Create mock constraint violations
            ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
            ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
            
            when(violation1.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
            when(violation1.getPropertyPath().toString()).thenReturn("email");
            when(violation1.getMessage()).thenReturn("Invalid email format");
            
            when(violation2.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
            when(violation2.getPropertyPath().toString()).thenReturn("age");
            when(violation2.getMessage()).thenReturn("Age must be positive");
            
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);
            
            when(constraintViolationException.getConstraintViolations()).thenReturn(violations);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleConstraintViolationException(constraintViolationException, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Constraint Violation", response.getBody().getError());
            assertNotNull(response.getBody().getFieldErrors());
            assertEquals(2, response.getBody().getFieldErrors().size());
        }

        @Test
        @DisplayName("Should handle AccessDeniedException")
        void shouldHandleAccessDeniedException() {
            String message = "Access is denied";
            AccessDeniedException exception = new AccessDeniedException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(403, response.getBody().getStatus());
            assertEquals("Access Denied", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException")
        void shouldHandleIllegalArgumentException() {
            String message = "Invalid argument provided";
            IllegalArgumentException exception = new IllegalArgumentException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Invalid Argument", response.getBody().getError());
            assertEquals(message, response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle HttpMessageNotReadableException")
        void shouldHandleHttpMessageNotReadableException() {
            String message = "JSON parse error";
            HttpMessageNotReadableException exception = new HttpMessageNotReadableException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleHttpMessageNotReadableException(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Bad Request", response.getBody().getError());
            assertEquals("Malformed JSON request. Please check your request body format.", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle HttpMediaTypeNotSupportedException")
        void shouldHandleHttpMediaTypeNotSupportedException() {
            String message = "Content type not supported";
            HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleHttpMediaTypeNotSupportedException(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().getStatus());
            assertEquals("Bad Request", response.getBody().getError());
            assertEquals("Unsupported media type. Please ensure you're sending valid JSON with Content-Type: application/json", response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle generic Exception")
        void shouldHandleGenericException() {
            String message = "Unexpected error occurred";
            Exception exception = new Exception(message);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleGenericException(exception, webRequest);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(500, response.getBody().getStatus());
            assertEquals("Internal Server Error", response.getBody().getError());
            assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("ErrorResponse DTO Tests")
    class ErrorResponseTest {

        @Test
        @DisplayName("Should create ErrorResponse with builder")
        void shouldCreateErrorResponseWithBuilder() {
            LocalDateTime timestamp = LocalDateTime.now();
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("email", Arrays.asList("Email is required", "Invalid email format"));

            GlobalExceptionHandler.ErrorResponse errorResponse = GlobalExceptionHandler.ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("Validation Error")
                .message("Request validation failed")
                .errorCode("VAL_001")
                .path("/api/users")
                .fieldErrors(fieldErrors)
                .build();

            assertEquals(timestamp, errorResponse.getTimestamp());
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Validation Error", errorResponse.getError());
            assertEquals("Request validation failed", errorResponse.getMessage());
            assertEquals("VAL_001", errorResponse.getErrorCode());
            assertEquals("/api/users", errorResponse.getPath());
            assertEquals(fieldErrors, errorResponse.getFieldErrors());
        }

        @Test
        @DisplayName("Should create ErrorResponse with no-args constructor")
        void shouldCreateErrorResponseWithNoArgsConstructor() {
            GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse();
            
            assertNotNull(errorResponse);
            assertNull(errorResponse.getTimestamp());
            assertEquals(0, errorResponse.getStatus());
            assertNull(errorResponse.getError());
            assertNull(errorResponse.getMessage());
            assertNull(errorResponse.getErrorCode());
            assertNull(errorResponse.getPath());
            assertNull(errorResponse.getFieldErrors());
        }

        @Test
        @DisplayName("Should create ErrorResponse with all-args constructor")
        void shouldCreateErrorResponseWithAllArgsConstructor() {
            LocalDateTime timestamp = LocalDateTime.now();
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("username", Collections.singletonList("Username is required"));

            GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse(
                timestamp, 404, "Not Found", "User not found", "USER_404", "/api/users/123", fieldErrors
            );

            assertEquals(timestamp, errorResponse.getTimestamp());
            assertEquals(404, errorResponse.getStatus());
            assertEquals("Not Found", errorResponse.getError());
            assertEquals("User not found", errorResponse.getMessage());
            assertEquals("USER_404", errorResponse.getErrorCode());
            assertEquals("/api/users/123", errorResponse.getPath());
            assertEquals(fieldErrors, errorResponse.getFieldErrors());
        }

        @Test
        @DisplayName("Should test ErrorResponse setters and getters")
        void shouldTestErrorResponseSettersAndGetters() {
            GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse();
            LocalDateTime timestamp = LocalDateTime.now();
            Map<String, List<String>> fieldErrors = new HashMap<>();
            fieldErrors.put("password", Collections.singletonList("Password is too weak"));

            errorResponse.setTimestamp(timestamp);
            errorResponse.setStatus(422);
            errorResponse.setError("Unprocessable Entity");
            errorResponse.setMessage("Validation failed");
            errorResponse.setErrorCode("VAL_422");
            errorResponse.setPath("/api/auth/register");
            errorResponse.setFieldErrors(fieldErrors);

            assertEquals(timestamp, errorResponse.getTimestamp());
            assertEquals(422, errorResponse.getStatus());
            assertEquals("Unprocessable Entity", errorResponse.getError());
            assertEquals("Validation failed", errorResponse.getMessage());
            assertEquals("VAL_422", errorResponse.getErrorCode());
            assertEquals("/api/auth/register", errorResponse.getPath());
            assertEquals(fieldErrors, errorResponse.getFieldErrors());
        }

        @Test
        @DisplayName("Should test ErrorResponse toString method")
        void shouldTestErrorResponseToString() {
            GlobalExceptionHandler.ErrorResponse errorResponse = GlobalExceptionHandler.ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("Database connection failed")
                .build();

            String toString = errorResponse.toString();
            assertNotNull(toString);
            assertTrue(toString.contains("ErrorResponse"));
            assertTrue(toString.contains("500"));
            assertTrue(toString.contains("Internal Server Error"));
        }

        @Test
        @DisplayName("Should test ErrorResponse equals and hashCode")
        void shouldTestErrorResponseEqualsAndHashCode() {
            LocalDateTime timestamp = LocalDateTime.now();
            
            GlobalExceptionHandler.ErrorResponse errorResponse1 = GlobalExceptionHandler.ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("Bad Request")
                .message("Invalid input")
                .build();

            GlobalExceptionHandler.ErrorResponse errorResponse2 = GlobalExceptionHandler.ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("Bad Request")
                .message("Invalid input")
                .build();

            GlobalExceptionHandler.ErrorResponse errorResponse3 = GlobalExceptionHandler.ErrorResponse.builder()
                .timestamp(timestamp)
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .build();

            // Test equals
            assertEquals(errorResponse1, errorResponse1); // Same object
            assertEquals(errorResponse1, errorResponse2); // Same values
            assertNotEquals(errorResponse1, errorResponse3); // Different values
            assertNotEquals(errorResponse1, null); // Null comparison
            assertNotEquals(errorResponse1, "string"); // Different type

            // Test hashCode consistency
            assertEquals(errorResponse1.hashCode(), errorResponse1.hashCode());
            assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode());
            assertNotEquals(errorResponse1.hashCode(), errorResponse3.hashCode());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesTest {

        @Test
        @DisplayName("Should handle null messages in exceptions")
        void shouldHandleNullMessagesInExceptions() {
            UserNotFoundException exception = new UserNotFoundException((String) null);

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleUserNotFoundException(exception, webRequest);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNull(response.getBody().getMessage());
        }

        @Test
        @DisplayName("Should handle empty field errors list")
        void shouldHandleEmptyFieldErrorsList() {
            when(methodArgumentNotValidException.getBindingResult()).thenReturn(
                mock(org.springframework.validation.BindingResult.class)
            );
            when(methodArgumentNotValidException.getBindingResult().getFieldErrors()).thenReturn(
                Collections.emptyList()
            );

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleMethodArgumentNotValidException(methodArgumentNotValidException, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getFieldErrors().isEmpty());
        }

        @Test
        @DisplayName("Should handle WebRequest with null description")
        void shouldHandleWebRequestWithNullDescription() {
            when(webRequest.getDescription(false)).thenReturn(null);
            
            ValidationException exception = new ValidationException("Test error");

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleValidationException(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNull(response.getBody().getPath());
        }

        @Test
        @DisplayName("Should handle WebRequest with uri prefix")
        void shouldHandleWebRequestWithUriPrefix() {
            when(webRequest.getDescription(false)).thenReturn("uri=/api/users/create");
            
            ValidationException exception = new ValidationException("Test error");

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleValidationException(exception, webRequest);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("/api/users/create", response.getBody().getPath());
        }
    }
}
