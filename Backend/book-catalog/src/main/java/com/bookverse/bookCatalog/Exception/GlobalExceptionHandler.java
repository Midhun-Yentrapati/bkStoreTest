package com.bookverse.bookCatalog.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the BookVerse application
 */
// Temporarily disabled to fix SpringDoc compatibility
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle custom base exceptions
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        logger.error("Base exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getHttpStatus().value(),
            ex.getHttpStatus().getReasonPhrase(),
            ex.getMessage(),
            ex.getErrorCode(),
            request.getRequestURI()
        );
        
        // Add field errors if it's a validation exception
        if (ex instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) ex;
            if (validationEx.hasFieldErrors()) {
                errorResponse.setFieldErrors(validationEx.getFieldErrors());
            }
        }
        
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.error("Validation error occurred: {}", ex.getMessage());
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed",
            "VALIDATION_ERROR",
            request.getRequestURI()
        );
        errorResponse.setFieldErrors(fieldErrors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle bind exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        logger.error("Bind exception occurred: {}", ex.getMessage());
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed",
            "VALIDATION_ERROR",
            request.getRequestURI()
        );
        errorResponse.setFieldErrors(fieldErrors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        logger.error("Constraint violation occurred: {}", ex.getMessage());
        
        Map<String, List<String>> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed",
            "CONSTRAINT_VIOLATION",
            request.getRequestURI()
        );
        errorResponse.setFieldErrors(fieldErrors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle entity not found exceptions
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {
        logger.error("Entity not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            "ENTITY_NOT_FOUND",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle data integrity violation exceptions
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity constraint violated";
        String errorCode = "DATA_INTEGRITY_VIOLATION";
        
        // Try to provide more specific error messages
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry")) {
                message = "Resource with this identifier already exists";
                errorCode = "DUPLICATE_RESOURCE";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Cannot perform operation due to related data";
                errorCode = "FOREIGN_KEY_CONSTRAINT";
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            message,
            errorCode,
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    /**
     * Handle method argument type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.error("Method argument type mismatch: {}", ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            "INVALID_PARAMETER_TYPE",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle missing servlet request parameter exceptions
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.error("Missing request parameter: {}", ex.getMessage());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            "MISSING_PARAMETER",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle HTTP message not readable exceptions
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.error("HTTP message not readable: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Invalid JSON format or malformed request body",
            "MALFORMED_JSON",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle HTTP request method not supported exceptions
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logger.error("HTTP method not supported: {}", ex.getMessage());
        
        String message = String.format("Method '%s' is not supported for this endpoint. Supported methods: %s",
            ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
            message,
            "METHOD_NOT_ALLOWED",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    /**
     * Handle HTTP media type not supported exceptions
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        logger.error("HTTP media type not supported: {}", ex.getMessage());
        
        String message = String.format("Media type '%s' is not supported. Supported media types: %s",
            ex.getContentType(), ex.getSupportedMediaTypes().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(),
            message,
            "UNSUPPORTED_MEDIA_TYPE",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    
    /**
     * Handle no handler found exceptions - but exclude SpringDoc paths
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        // Don't handle SpringDoc related paths
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/v3/api-docs") || requestURI.contains("/swagger-ui")) {
            return null; // Let Spring handle it
        }
        
        logger.error("No handler found: {}", ex.getMessage());
        
        String message = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            message,
            "ENDPOINT_NOT_FOUND",
            requestURI
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle missing static resources (e.g., favicon.ico) with a clean 404
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        logger.warn("Static resource not found: {}", requestURI);
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "Resource not found",
            "RESOURCE_NOT_FOUND",
            requestURI
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle all other exceptions, but exclude SpringDoc and static resource exceptions
     * @throws Exception 
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) throws Exception {
        // Skip SpringDoc and Actuator related requests
        String uri = request.getRequestURI();
        if (uri.startsWith("/actuator") || uri.contains("/v3/api-docs") || uri.contains("/swagger-ui") || 
            ex.getClass().getName().contains("springdoc")) {
            throw ex; // Re-throw to let framework/actuator handle it
        }
        
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An unexpected error occurred. Please try again later.",
            "INTERNAL_SERVER_ERROR",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 