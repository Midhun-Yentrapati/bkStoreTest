package com.bookstore.user_authentication_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * This exception provides detailed information about the missing resource
 * including resource type, ID, and appropriate HTTP status codes.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;
    private final String resourceType;
    private final String resourceId;
    
    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = "RESOURCE_NOT_FOUND";
        this.httpStatus = HttpStatus.NOT_FOUND;
        this.retryable = false;
        this.resourceType = "Resource";
        this.resourceId = null;
    }
    
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " not found with ID: " + resourceId);
        this.errorCode = "RESOURCE_NOT_FOUND";
        this.httpStatus = HttpStatus.NOT_FOUND;
        this.retryable = false;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String message, String resourceType, String resourceId) {
        super(message);
        this.errorCode = resourceType.toUpperCase() + "_NOT_FOUND";
        this.httpStatus = HttpStatus.NOT_FOUND;
        this.retryable = false;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String message, String errorCode, String resourceType, String resourceId) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.NOT_FOUND;
        this.retryable = false;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RESOURCE_NOT_FOUND";
        this.httpStatus = HttpStatus.NOT_FOUND;
        this.retryable = false;
        this.resourceType = "Resource";
        this.resourceId = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    // Specific resource not found factory methods
    public static ResourceNotFoundException userNotFound(String userId) {
        return new ResourceNotFoundException(
            "User not found with ID: " + userId,
            "USER_NOT_FOUND",
            "User",
            userId
        );
    }
    
    public static ResourceNotFoundException sessionNotFound(String sessionId) {
        return new ResourceNotFoundException(
            "Session not found with ID: " + sessionId,
            "SESSION_NOT_FOUND",
            "Session",
            sessionId
        );
    }
    
    public static ResourceNotFoundException addressNotFound(String addressId) {
        return new ResourceNotFoundException(
            "Address not found with ID: " + addressId,
            "ADDRESS_NOT_FOUND",
            "Address",
            addressId
        );
    }
    
    public static ResourceNotFoundException adminUserNotFound(String adminId) {
        return new ResourceNotFoundException(
            "Admin user not found with ID: " + adminId,
            "ADMIN_USER_NOT_FOUND",
            "AdminUser",
            adminId
        );
    }
    
    public static ResourceNotFoundException tokenNotFound(String token) {
        return new ResourceNotFoundException(
            "Token not found or invalid",
            "TOKEN_NOT_FOUND",
            "Token",
            token
        );
    }
    
    public static ResourceNotFoundException roleNotFound(String roleName) {
        return new ResourceNotFoundException(
            "Role not found: " + roleName,
            "ROLE_NOT_FOUND",
            "Role",
            roleName
        );
    }
    
    public static ResourceNotFoundException permissionNotFound(String permission) {
        return new ResourceNotFoundException(
            "Permission not found: " + permission,
            "PERMISSION_NOT_FOUND",
            "Permission",
            permission
        );
    }
    
    public static ResourceNotFoundException departmentNotFound(String departmentName) {
        return new ResourceNotFoundException(
            "Department not found: " + departmentName,
            "DEPARTMENT_NOT_FOUND",
            "Department",
            departmentName
        );
    }
    
    public static ResourceNotFoundException customResource(String resourceType, String resourceId, String customMessage) {
        return new ResourceNotFoundException(
            customMessage,
            resourceType.toUpperCase() + "_NOT_FOUND",
            resourceType,
            resourceId
        );
    }
}
