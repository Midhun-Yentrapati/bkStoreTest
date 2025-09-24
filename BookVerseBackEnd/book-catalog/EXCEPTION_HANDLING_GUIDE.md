# Exception Handling Guide - BookVerse Backend

## Overview

This document describes the comprehensive exception handling system implemented for the BookVerse Spring Boot backend application.

## Exception Architecture

### Base Exception Class

**`BaseException`** - Abstract base class for all custom exceptions
- Contains HTTP status code and error code
- Provides consistent structure for all custom exceptions
- Extends `RuntimeException` for unchecked exception behavior

### Custom Exception Classes

1. **`BookNotFoundException`**
   - HTTP Status: 404 (Not Found)
   - Error Code: `BOOK_NOT_FOUND`
   - Used when: Requested book doesn't exist

2. **`CategoryNotFoundException`**
   - HTTP Status: 404 (Not Found)
   - Error Code: `CATEGORY_NOT_FOUND`
   - Used when: Requested category doesn't exist

3. **`InsufficientStockException`**
   - HTTP Status: 400 (Bad Request)
   - Error Code: `INSUFFICIENT_STOCK`
   - Used when: Not enough stock for requested operation

4. **`DuplicateResourceException`**
   - HTTP Status: 409 (Conflict)
   - Error Code: `DUPLICATE_RESOURCE`
   - Used when: Trying to create resource that already exists

5. **`ValidationException`**
   - HTTP Status: 400 (Bad Request)
   - Error Code: `VALIDATION_ERROR`
   - Used when: Input validation fails
   - Supports field-level error details

6. **`BusinessLogicException`**
   - HTTP Status: 400 (Bad Request)
   - Error Code: `BUSINESS_LOGIC_ERROR`
   - Used when: Business rules are violated

## Error Response Structure

All errors return a standardized JSON response:

```json
{
  "timestamp": "2025-09-15 09:07:18",
  "status": 404,
  "error": "Not Found",
  "message": "Book not found with ID: 999",
  "errorCode": "BOOK_NOT_FOUND",
  "path": "/api/books/999",
  "fieldErrors": {
    "fieldName": ["Error message 1", "Error message 2"]
  }
}
```

### Response Fields

- **timestamp**: When the error occurred (ISO format)
- **status**: HTTP status code
- **error**: HTTP status reason phrase
- **message**: Human-readable error description
- **errorCode**: Machine-readable error identifier
- **path**: Request URI that caused the error
- **fieldErrors**: Field-specific validation errors (optional)

## Global Exception Handler

**`GlobalExceptionHandler`** handles all exceptions across the application:

### Handled Exception Types

1. **Custom Base Exceptions** - All our custom exceptions
2. **Validation Exceptions** - `@Valid` annotation failures
3. **Constraint Violations** - JPA constraint violations
4. **Data Integrity Violations** - Database constraint violations
5. **HTTP Method Not Supported** - Wrong HTTP method
6. **Media Type Not Supported** - Wrong content type
7. **Missing Parameters** - Required parameters missing
8. **Malformed JSON** - Invalid request body
9. **Type Mismatch** - Wrong parameter types
10. **Generic Exceptions** - Catch-all for unexpected errors

## Service Layer Integration

### BookService

- **Validation**: Input validation with custom exceptions
- **Business Logic**: Stock management, duplicate checks
- **Error Handling**: Comprehensive error scenarios

Example methods:
- `getBookByIdOrThrow()` - Throws `BookNotFoundException`
- `decreaseStock()` - Throws `InsufficientStockException`
- `createBookFromRequest()` - Throws `ValidationException`, `DuplicateResourceException`

### CategoryService

- **Validation**: Name validation, ID validation
- **Duplicate Prevention**: Name uniqueness checks
- **Error Handling**: Category-specific exceptions

## Controller Layer Integration

Controllers are simplified - they no longer need try-catch blocks:

```java
@GetMapping("/{id}")
public ResponseEntity<Books> getBookById(@PathVariable Long id) {
    Books book = bookService.getBookByIdOrThrow(id);
    return ResponseEntity.ok(book);
}
```

## Usage Examples

### Testing Exception Handling

1. **Book Not Found**:
   ```bash
   GET /api/books/999
   # Returns 404 with BOOK_NOT_FOUND error
   ```

2. **Invalid ID**:
   ```bash
   GET /api/books/-1
   # Returns 400 with VALIDATION_ERROR
   ```

3. **Insufficient Stock**:
   ```bash
   PUT /api/books/1/stock?quantity=1000
   # Returns 400 with INSUFFICIENT_STOCK error
   ```

## Benefits

1. **Consistency**: All errors follow the same structure
2. **Debugging**: Detailed error information with timestamps and paths
3. **Client-Friendly**: Machine-readable error codes for frontend handling
4. **Maintainability**: Centralized error handling logic
5. **Logging**: Automatic error logging with appropriate levels
6. **Security**: Sensitive information is not exposed in error messages

## Error Codes Reference

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `BOOK_NOT_FOUND` | 404 | Requested book doesn't exist |
| `CATEGORY_NOT_FOUND` | 404 | Requested category doesn't exist |
| `VALIDATION_ERROR` | 400 | Input validation failed |
| `INSUFFICIENT_STOCK` | 400 | Not enough stock available |
| `DUPLICATE_RESOURCE` | 409 | Resource already exists |
| `BUSINESS_LOGIC_ERROR` | 400 | Business rule violation |
| `DATA_INTEGRITY_VIOLATION` | 409 | Database constraint violation |
| `METHOD_NOT_ALLOWED` | 405 | HTTP method not supported |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Content type not supported |
| `MISSING_PARAMETER` | 400 | Required parameter missing |
| `INVALID_PARAMETER_TYPE` | 400 | Parameter type mismatch |
| `MALFORMED_JSON` | 400 | Invalid JSON in request body |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected server error |

## Best Practices

1. **Use Specific Exceptions**: Choose the most appropriate exception type
2. **Provide Clear Messages**: Error messages should be helpful to developers
3. **Don't Expose Sensitive Data**: Keep error messages safe for production
4. **Log Appropriately**: Use different log levels based on error severity
5. **Test Error Scenarios**: Include error cases in your tests
6. **Document Error Codes**: Keep this reference up to date

## Future Enhancements

1. **Internationalization**: Support multiple languages for error messages
2. **Rate Limiting**: Add rate limiting exceptions
3. **Authentication**: Add authentication/authorization exceptions
4. **Monitoring**: Integrate with monitoring systems
5. **Custom Error Pages**: Create user-friendly error pages for web interface 