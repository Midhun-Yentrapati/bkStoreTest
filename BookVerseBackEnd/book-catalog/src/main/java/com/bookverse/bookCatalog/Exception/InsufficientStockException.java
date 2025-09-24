package com.bookverse.bookCatalog.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's insufficient stock for an operation
 */
public class InsufficientStockException extends BaseException {
    
    public InsufficientStockException(Long bookId, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for book ID %d. Requested: %d, Available: %d", 
              bookId, requestedQuantity, availableStock), 
              HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK");
    }
    
    public InsufficientStockException(String bookTitle, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for book '%s'. Requested: %d, Available: %d", 
              bookTitle, requestedQuantity, availableStock), 
              HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK");
    }
    
    public InsufficientStockException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK");
    }
} 