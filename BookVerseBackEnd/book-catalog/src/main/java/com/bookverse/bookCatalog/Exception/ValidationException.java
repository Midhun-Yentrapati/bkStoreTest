package com.bookverse.bookCatalog.Exception;

import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends BaseException {
    private final Map<String, List<String>> fieldErrors;
    
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        this.fieldErrors = null;
    }
    
    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        this.fieldErrors = fieldErrors;
    }
    
    public ValidationException(Map<String, List<String>> fieldErrors) {
        super("Validation failed", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        this.fieldErrors = fieldErrors;
    }
    
    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
    
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }
} 