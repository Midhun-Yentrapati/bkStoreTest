package com.bookverse.bookCatalog.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when business logic rules are violated
 */
public class BusinessLogicException extends BaseException {
    
    public BusinessLogicException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_LOGIC_ERROR");
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "BUSINESS_LOGIC_ERROR");
    }
    
    public BusinessLogicException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
} 