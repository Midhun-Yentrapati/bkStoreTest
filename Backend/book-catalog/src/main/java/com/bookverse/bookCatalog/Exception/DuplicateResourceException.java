package com.bookverse.bookCatalog.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to create a resource that already exists
 */
public class DuplicateResourceException extends BaseException {
    
    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("%s already exists with identifier: %s", resourceType, identifier), 
              HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
    
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
} 