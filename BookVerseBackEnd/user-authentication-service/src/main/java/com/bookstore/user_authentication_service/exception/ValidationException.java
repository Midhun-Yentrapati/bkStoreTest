package com.bookstore.user_authentication_service.exception;

import java.util.List;
import java.util.Map;

public class ValidationException extends RuntimeException {
    
    private final Map<String, List<String>> fieldErrors;
    
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = null;
    }
    
    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldErrors = null;
    }
    
    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
    
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }
    
    // Specific validation error types
    public static ValidationException usernameAlreadyExists(String username) {
        return new ValidationException("Username '" + username + "' is already taken");
    }
    
    public static ValidationException emailAlreadyExists(String email) {
        return new ValidationException("Email '" + email + "' is already registered");
    }
    
    public static ValidationException employeeIdAlreadyExists(String employeeId) {
        return new ValidationException("Employee ID '" + employeeId + "' is already assigned");
    }
    
    public static ValidationException passwordMismatch() {
        return new ValidationException("Password and confirm password do not match");
    }
    
    public static ValidationException weakPassword() {
        return new ValidationException("Password does not meet security requirements");
    }
    
    public static ValidationException invalidEmailFormat(String email) {
        return new ValidationException("Invalid email format: " + email);
    }
    
    public static ValidationException invalidPhoneNumber(String phoneNumber) {
        return new ValidationException("Invalid phone number format: " + phoneNumber);
    }
    
    public static ValidationException invalidUserRole(String role) {
        return new ValidationException("Invalid user role: " + role);
    }
    
    public static ValidationException invalidDateOfBirth() {
        return new ValidationException("Date of birth must be in the past");
    }
    
    public static ValidationException missingRequiredField(String fieldName) {
        return new ValidationException("Required field is missing: " + fieldName);
    }
}
