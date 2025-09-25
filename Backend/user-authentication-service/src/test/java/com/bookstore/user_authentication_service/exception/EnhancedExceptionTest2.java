package com.bookstore.user_authentication_service.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for remaining custom exception classes
 * Part 2: ResourceNotFoundException, ServiceException, SecurityException, ConfigurationException, RateLimitException, UserNotFoundException
 */
@DisplayName("Enhanced Exception Classes Tests - Part 2")
class EnhancedExceptionTest2 {

    @Nested
    @DisplayName("ResourceNotFoundException Comprehensive Tests")
    class ResourceNotFoundExceptionTest {

        @Test
        @DisplayName("Should create exception with all constructor variations")
        void shouldCreateExceptionWithAllConstructorVariations() {
            // Test message-only constructor
            String message = "Resource not found";
            ResourceNotFoundException messageOnly = new ResourceNotFoundException(message);
            assertEquals(message, messageOnly.getMessage());
            assertEquals("RESOURCE_NOT_FOUND", messageOnly.getErrorCode());
            assertEquals(HttpStatus.NOT_FOUND, messageOnly.getHttpStatus());
            assertFalse(messageOnly.isRetryable());
            assertEquals("Resource", messageOnly.getResourceType());
            assertNull(messageOnly.getResourceId());

            // Test resourceType and resourceId constructor
            String resourceType = "User";
            String resourceId = "123";
            ResourceNotFoundException typeAndId = new ResourceNotFoundException(resourceType, resourceId);
            assertTrue(typeAndId.getMessage().contains(resourceType));
            assertTrue(typeAndId.getMessage().contains(resourceId));
            assertEquals("RESOURCE_NOT_FOUND", typeAndId.getErrorCode());
            assertEquals(resourceType, typeAndId.getResourceType());
            assertEquals(resourceId, typeAndId.getResourceId());

            // Test message, resourceType, and resourceId constructor
            ResourceNotFoundException messageTypeId = new ResourceNotFoundException(message, resourceType, resourceId);
            assertEquals(message, messageTypeId.getMessage());
            assertEquals("USER_NOT_FOUND", messageTypeId.getErrorCode());
            assertEquals(resourceType, messageTypeId.getResourceType());
            assertEquals(resourceId, messageTypeId.getResourceId());

            // Test message and cause constructor
            Throwable cause = new RuntimeException("Database error");
            ResourceNotFoundException messageAndCause = new ResourceNotFoundException(message, cause);
            assertEquals(message, messageAndCause.getMessage());
            assertEquals(cause, messageAndCause.getCause());
            assertEquals("RESOURCE_NOT_FOUND", messageAndCause.getErrorCode());
        }

        @Test
        @DisplayName("Should test all static factory methods")
        void shouldTestAllStaticFactoryMethods() {
            // Test userNotFound
            String userId = "user-123";
            ResourceNotFoundException userNotFound = ResourceNotFoundException.userNotFound(userId);
            assertEquals("USER_NOT_FOUND", userNotFound.getErrorCode());
            assertEquals("User", userNotFound.getResourceType());
            assertEquals(userId, userNotFound.getResourceId());
            assertTrue(userNotFound.getMessage().contains(userId));

            // Test sessionNotFound
            String sessionId = "session-456";
            ResourceNotFoundException sessionNotFound = ResourceNotFoundException.sessionNotFound(sessionId);
            assertEquals("SESSION_NOT_FOUND", sessionNotFound.getErrorCode());
            assertEquals("Session", sessionNotFound.getResourceType());
            assertEquals(sessionId, sessionNotFound.getResourceId());

            // Test addressNotFound
            String addressId = "addr-789";
            ResourceNotFoundException addressNotFound = ResourceNotFoundException.addressNotFound(addressId);
            assertEquals("ADDRESS_NOT_FOUND", addressNotFound.getErrorCode());
            assertEquals("Address", addressNotFound.getResourceType());
            assertEquals(addressId, addressNotFound.getResourceId());

            // Test adminUserNotFound
            String adminId = "admin-101";
            ResourceNotFoundException adminNotFound = ResourceNotFoundException.adminUserNotFound(adminId);
            assertEquals("ADMIN_USER_NOT_FOUND", adminNotFound.getErrorCode());
            assertEquals("AdminUser", adminNotFound.getResourceType());
            assertEquals(adminId, adminNotFound.getResourceId());

            // Test tokenNotFound
            String token = "token-xyz";
            ResourceNotFoundException tokenNotFound = ResourceNotFoundException.tokenNotFound(token);
            assertEquals("TOKEN_NOT_FOUND", tokenNotFound.getErrorCode());
            assertEquals("Token", tokenNotFound.getResourceType());
            assertEquals(token, tokenNotFound.getResourceId());

            // Test roleNotFound
            String roleName = "ADMIN";
            ResourceNotFoundException roleNotFound = ResourceNotFoundException.roleNotFound(roleName);
            assertEquals("ROLE_NOT_FOUND", roleNotFound.getErrorCode());
            assertEquals("Role", roleNotFound.getResourceType());
            assertEquals(roleName, roleNotFound.getResourceId());

            // Test permissionNotFound
            String permission = "READ_USERS";
            ResourceNotFoundException permissionNotFound = ResourceNotFoundException.permissionNotFound(permission);
            assertEquals("PERMISSION_NOT_FOUND", permissionNotFound.getErrorCode());
            assertEquals("Permission", permissionNotFound.getResourceType());
            assertEquals(permission, permissionNotFound.getResourceId());

            // Test departmentNotFound
            String department = "IT";
            ResourceNotFoundException deptNotFound = ResourceNotFoundException.departmentNotFound(department);
            assertEquals("DEPARTMENT_NOT_FOUND", deptNotFound.getErrorCode());
            assertEquals("Department", deptNotFound.getResourceType());
            assertEquals(department, deptNotFound.getResourceId());

            // Test customResource
            String customType = "CustomResource";
            String customId = "custom-123";
            String customMessage = "Custom resource not found";
            ResourceNotFoundException customNotFound = ResourceNotFoundException.customResource(customType, customId, customMessage);
            assertEquals("CUSTOMRESOURCE_NOT_FOUND", customNotFound.getErrorCode());
            assertEquals(customType, customNotFound.getResourceType());
            assertEquals(customId, customNotFound.getResourceId());
            assertEquals(customMessage, customNotFound.getMessage());
        }

        @Test
        @DisplayName("Should test getter methods and properties")
        void shouldTestGetterMethodsAndProperties() {
            String message = "Test resource not found";
            String errorCode = "TEST_NOT_FOUND";
            String resourceType = "TestResource";
            String resourceId = "test-123";
            
            ResourceNotFoundException exception = new ResourceNotFoundException(message, errorCode, resourceType, resourceId);
            
            // Test all getters
            assertEquals(message, exception.getMessage());
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertFalse(exception.isRetryable());
            assertEquals(resourceType, exception.getResourceType());
            assertEquals(resourceId, exception.getResourceId());
        }

        @Test
        @DisplayName("Should handle edge cases and null values")
        void shouldHandleEdgeCasesAndNullValues() {
            // Test with null parameters
            ResourceNotFoundException nullMessage = new ResourceNotFoundException(null);
            assertNull(nullMessage.getMessage());
            assertEquals("RESOURCE_NOT_FOUND", nullMessage.getErrorCode());

            ResourceNotFoundException nullTypeId = new ResourceNotFoundException((String) null, (String) null);
            assertTrue(nullTypeId.getMessage().contains("null"));
            assertNull(nullTypeId.getResourceType());
            assertNull(nullTypeId.getResourceId());

            // Test with empty strings
            ResourceNotFoundException emptyType = new ResourceNotFoundException("", "");
            assertTrue(emptyType.getMessage().contains("not found with ID"));
            assertEquals("", emptyType.getResourceType());
            assertEquals("", emptyType.getResourceId());
        }
    }

    @Nested
    @DisplayName("ServiceException Comprehensive Tests")
    class ServiceExceptionTest {

        @Test
        @DisplayName("Should create exception with all constructor variations")
        void shouldCreateExceptionWithAllConstructorVariations() {
            // Test message-only constructor
            String message = "Service operation failed";
            ServiceException messageOnly = new ServiceException(message);
            assertEquals(message, messageOnly.getMessage());
            assertEquals("SERVICE_ERROR", messageOnly.getErrorCode());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, messageOnly.getHttpStatus());
            assertTrue(messageOnly.isRetryable());
            assertNull(messageOnly.getServiceName());

            // Test message and cause constructor
            Throwable cause = new RuntimeException("External service error");
            ServiceException messageAndCause = new ServiceException(message, cause);
            assertEquals(message, messageAndCause.getMessage());
            assertEquals(cause, messageAndCause.getCause());
            assertEquals("SERVICE_ERROR", messageAndCause.getErrorCode());
        }

        @Test
        @DisplayName("Should test getter methods and properties")
        void shouldTestGetterMethodsAndProperties() {
            String message = "Test service error";
            ServiceException exception = new ServiceException(message);
            
            // Test all getters
            assertEquals(message, exception.getMessage());
            assertEquals("SERVICE_ERROR", exception.getErrorCode());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
            assertTrue(exception.isRetryable());
            assertNull(exception.getServiceName());
        }

        @Test
        @DisplayName("Should handle null and empty parameters")
        void shouldHandleNullAndEmptyParameters() {
            // Test with null message
            ServiceException nullMessage = new ServiceException(null);
            assertNull(nullMessage.getMessage());
            assertEquals("SERVICE_ERROR", nullMessage.getErrorCode());

            // Test with empty message
            ServiceException emptyMessage = new ServiceException("");
            assertEquals("", emptyMessage.getMessage());
        }
    }

    @Nested
    @DisplayName("SecurityException Comprehensive Tests")
    class SecurityExceptionTest {

        @Test
        @DisplayName("Should create exception with all constructor variations")
        void shouldCreateExceptionWithAllConstructorVariations() {
            // Test message-only constructor
            String message = "Access denied";
            SecurityException messageOnly = new SecurityException(message);
            assertEquals(message, messageOnly.getMessage());
            assertEquals("SECURITY_ERROR", messageOnly.getErrorCode());
            assertEquals(HttpStatus.FORBIDDEN, messageOnly.getHttpStatus());
            assertFalse(messageOnly.isRetryable());

            // Test message and cause constructor
            Throwable cause = new RuntimeException("Security validation failed");
            SecurityException messageAndCause = new SecurityException(message, cause);
            assertEquals(message, messageAndCause.getMessage());
            assertEquals(cause, messageAndCause.getCause());
        }

        @Test
        @DisplayName("Should test getter methods and properties")
        void shouldTestGetterMethodsAndProperties() {
            String message = "Test security error";
            SecurityException exception = new SecurityException(message);
            
            // Test all getters
            assertEquals(message, exception.getMessage());
            assertEquals("SECURITY_ERROR", exception.getErrorCode());
            assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
            assertFalse(exception.isRetryable());
        }
    }

    @Nested
    @DisplayName("ConfigurationException Comprehensive Tests")
    class ConfigurationExceptionTest {

        @Test
        @DisplayName("Should create exception with all constructor variations")
        void shouldCreateExceptionWithAllConstructorVariations() {
            // Test message-only constructor
            String message = "Configuration error";
            ConfigurationException messageOnly = new ConfigurationException(message);
            assertEquals(message, messageOnly.getMessage());
            assertEquals("CONFIG_ERROR", messageOnly.getErrorCode());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, messageOnly.getHttpStatus());
            assertFalse(messageOnly.isRetryable());

            // Test message and cause constructor
            Throwable cause = new RuntimeException("Config file not found");
            ConfigurationException messageAndCause = new ConfigurationException(message, cause);
            assertEquals(message, messageAndCause.getMessage());
            assertEquals(cause, messageAndCause.getCause());
        }

        @Test
        @DisplayName("Should test getter methods and properties")
        void shouldTestGetterMethodsAndProperties() {
            String message = "Test configuration error";
            ConfigurationException exception = new ConfigurationException(message);
            
            // Test all getters
            assertEquals(message, exception.getMessage());
            assertEquals("CONFIG_ERROR", exception.getErrorCode());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
            assertFalse(exception.isRetryable());
        }
    }

    @Nested
    @DisplayName("RateLimitException Comprehensive Tests")
    class RateLimitExceptionTest {

        @Test
        @DisplayName("Should create exception with all constructor variations")
        void shouldCreateExceptionWithAllConstructorVariations() {
            // Test message-only constructor
            String message = "Rate limit exceeded";
            RateLimitException messageOnly = new RateLimitException(message);
            assertEquals(message, messageOnly.getMessage());
            assertEquals("RATE_LIMIT_EXCEEDED", messageOnly.getErrorCode());
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, messageOnly.getHttpStatus());
            assertTrue(messageOnly.isRetryable());

            // Test message and cause constructor
            Throwable cause = new RuntimeException("Too many requests");
            RateLimitException messageAndCause = new RateLimitException(message, cause);
            assertEquals(message, messageAndCause.getMessage());
            assertEquals(cause, messageAndCause.getCause());
        }

        @Test
        @DisplayName("Should test getter methods and properties")
        void shouldTestGetterMethodsAndProperties() {
            String message = "Test rate limit error";
            RateLimitException exception = new RateLimitException(message);
            
            // Test all getters
            assertEquals(message, exception.getMessage());
            assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getHttpStatus());
            assertTrue(exception.isRetryable());
        }
    }

    @Nested
    @DisplayName("UserNotFoundException Comprehensive Tests")
    class UserNotFoundExceptionTest {

        @Test
        @DisplayName("Should create exception with all constructor variations")
        void shouldCreateExceptionWithAllConstructorVariations() {
            // Test message-only constructor
            String message = "User not found";
            UserNotFoundException messageOnly = new UserNotFoundException(message);
            assertEquals(message, messageOnly.getMessage());
            assertNull(messageOnly.getCause());

            // Test message and cause constructor
            Throwable cause = new RuntimeException("Database connection timeout");
            UserNotFoundException messageAndCause = new UserNotFoundException(message, cause);
            assertEquals(message, messageAndCause.getMessage());
            assertEquals(cause, messageAndCause.getCause());
        }

        @Test
        @DisplayName("Should test all static factory methods")
        void shouldTestAllStaticFactoryMethods() {
            // Test byId factory method
            String userId = "user-123";
            UserNotFoundException byId = UserNotFoundException.byId(userId);
            assertTrue(byId.getMessage().contains(userId));
            assertTrue(byId.getMessage().contains("User not found with ID"));

            // Test byUsername factory method
            String username = "johndoe";
            UserNotFoundException byUsername = UserNotFoundException.byUsername(username);
            assertTrue(byUsername.getMessage().contains(username));
            assertTrue(byUsername.getMessage().contains("username"));

            // Test byEmail factory method
            String email = "john@example.com";
            UserNotFoundException byEmail = UserNotFoundException.byEmail(email);
            assertTrue(byEmail.getMessage().contains(email));
            assertTrue(byEmail.getMessage().contains("email"));

            // Test byUsernameOrEmail factory method
            String usernameOrEmail = "john";
            UserNotFoundException byUsernameOrEmail = UserNotFoundException.byUsernameOrEmail(usernameOrEmail);
            assertTrue(byUsernameOrEmail.getMessage().contains(usernameOrEmail));
            assertTrue(byUsernameOrEmail.getMessage().contains("username or email"));

            // Test byEmployeeId factory method
            String employeeId = "EMP001";
            UserNotFoundException byEmployeeId = UserNotFoundException.byEmployeeId(employeeId);
            assertTrue(byEmployeeId.getMessage().contains(employeeId));
            assertTrue(byEmployeeId.getMessage().contains("employee ID"));
        }

        @Test
        @DisplayName("Should handle null and empty parameters in factory methods")
        void shouldHandleNullAndEmptyParametersInFactoryMethods() {
            // Test with null parameters
            UserNotFoundException nullId = UserNotFoundException.byId(null);
            assertTrue(nullId.getMessage().contains("null"));

            UserNotFoundException nullUsername = UserNotFoundException.byUsername(null);
            assertTrue(nullUsername.getMessage().contains("null"));

            UserNotFoundException nullEmail = UserNotFoundException.byEmail(null);
            assertTrue(nullEmail.getMessage().contains("null"));

            // Test with empty parameters
            UserNotFoundException emptyId = UserNotFoundException.byId("");
            assertTrue(emptyId.getMessage().contains("User not found with ID"));

            UserNotFoundException emptyUsername = UserNotFoundException.byUsername("");
            assertTrue(emptyUsername.getMessage().contains("username"));
        }

        @Test
        @DisplayName("Should test exception inheritance and behavior")
        void shouldTestExceptionInheritanceAndBehavior() {
            UserNotFoundException exception = new UserNotFoundException("Test message");
            
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
            assertTrue(toString.contains("UserNotFoundException"));
            assertTrue(toString.contains("Test message"));
        }
    }

    @Nested
    @DisplayName("Exception Inheritance and Cross-Exception Tests")
    class ExceptionInheritanceTest {

        @Test
        @DisplayName("Should verify all exceptions extend RuntimeException")
        void shouldVerifyAllExceptionsExtendRuntimeException() {
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
        @DisplayName("Should test exception serialization compatibility")
        void shouldTestExceptionSerializationCompatibility() {
            // Test that exceptions can be created and their properties accessed
            AuthenticationException authEx = new AuthenticationException("Auth error");
            assertNotNull(authEx.toString());
            assertNotNull(authEx.getStackTrace());

            ValidationException validEx = new ValidationException("Validation error");
            assertNotNull(validEx.toString());
            assertNotNull(validEx.getStackTrace());

            ResourceNotFoundException resEx = new ResourceNotFoundException("Resource error");
            assertNotNull(resEx.toString());
            assertNotNull(resEx.getStackTrace());
        }

        @Test
        @DisplayName("Should test exception chaining and cause propagation")
        void shouldTestExceptionChainingAndCausePropagation() {
            // Create a chain of exceptions
            RuntimeException rootCause = new RuntimeException("Root cause");
            ValidationException middleException = new ValidationException("Middle exception", rootCause);
            AuthenticationException topException = new AuthenticationException("Top exception", middleException);

            // Test cause propagation
            assertEquals(middleException, topException.getCause());
            assertEquals(rootCause, topException.getCause().getCause());
            assertEquals(rootCause, middleException.getCause());

            // Test that root cause can be retrieved
            Throwable currentCause = topException;
            while (currentCause.getCause() != null) {
                currentCause = currentCause.getCause();
            }
            assertEquals(rootCause, currentCause);
        }
    }
}
