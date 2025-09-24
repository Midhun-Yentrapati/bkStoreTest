package com.bookverse.bookCatalog.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested category is not found
 */
public class CategoryNotFoundException extends BaseException {
    
    public CategoryNotFoundException(Long categoryId) {
        super("Category not found with ID: " + categoryId, HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND");
    }
    
    public CategoryNotFoundException(String categoryName) {
        super("Category not found with name: " + categoryName, HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND");
    }
    
    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND");
    }
} 