package com.bookstore.user_authentication_service.exception;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends RuntimeException {
    
    private final Map<String, List<String>> fieldErrors;
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;
    
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = null;
        this.errorCode = "VALIDATION_ERROR";
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.retryable = true;
    }
    
    public ValidationException(String message, String errorCode) {
        super(message);
        this.fieldErrors = null;
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.retryable = true;
    }
    
    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
        this.errorCode = "VALIDATION_ERROR";
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.retryable = true;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldErrors = null;
        this.errorCode = "VALIDATION_ERROR";
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.retryable = true;
    }
    
    public ValidationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.fieldErrors = null;
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.retryable = true;
    }

    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    // Specific validation error types
    public static ValidationException usernameAlreadyExists(String username) {
        return new ValidationException("Username '" + username + "' is already taken", "USERNAME_EXISTS");
    }
    
    public static ValidationException emailAlreadyExists(String email) {
        return new ValidationException("Email '" + email + "' is already registered", "EMAIL_EXISTS");
    }
    
    public static ValidationException employeeIdAlreadyExists(String employeeId) {
        return new ValidationException("Employee ID '" + employeeId + "' is already assigned", "EMPLOYEE_ID_EXISTS");
    }
    
    public static ValidationException passwordMismatch() {
        return new ValidationException("Password and confirm password do not match", "PASSWORD_MISMATCH");
    }
    
    public static ValidationException weakPassword() {
        return new ValidationException("Password does not meet security requirements", "WEAK_PASSWORD");
    }
    
    public static ValidationException invalidEmailFormat(String email) {
        return new ValidationException("Invalid email format: " + email, "INVALID_EMAIL");
    }
    
    public static ValidationException invalidPhoneNumber(String phoneNumber) {
        return new ValidationException("Invalid phone number format: " + phoneNumber, "INVALID_PHONE");
    }
    
    public static ValidationException invalidUserRole(String role) {
        return new ValidationException("Invalid user role: " + role, "INVALID_ROLE");
    }
    
    public static ValidationException invalidDateOfBirth() {
        return new ValidationException("Date of birth must be in the past", "INVALID_DATE_OF_BIRTH");
    }
    
    public static ValidationException missingRequiredField(String fieldName) {
        return new ValidationException("Required field is missing: " + fieldName, "MISSING_FIELD");
    }
}
