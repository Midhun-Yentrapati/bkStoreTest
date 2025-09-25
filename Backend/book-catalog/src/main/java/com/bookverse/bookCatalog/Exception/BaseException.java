package com.bookverse.bookCatalog.Exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for all custom exceptions in the BookVerse application
 */
public abstract class BaseException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorCode;
    
    protected BaseException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
    
    protected BaseException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 