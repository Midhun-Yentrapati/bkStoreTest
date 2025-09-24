package com.bookverse.bookCatalog.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested book is not found
 */
public class BookNotFoundException extends BaseException {
    
    public BookNotFoundException(Long bookId) {
        super("Book not found with ID: " + bookId, HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND");
    }
    
    public BookNotFoundException(String isbn) {
        super("Book not found with ISBN: " + isbn, HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND");
    }
    
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND");
    }
} 