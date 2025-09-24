package com.bookstore.user_authentication_service.exception;

/**
 * Exception thrown when an address is not found
 */
public class AddressNotFoundException extends RuntimeException {
    
    public AddressNotFoundException(String message) {
        super(message);
    }
    
    public AddressNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static AddressNotFoundException addressNotFound(String addressId) {
        return new AddressNotFoundException("Address not found with ID: " + addressId);
    }
    
    public static AddressNotFoundException addressNotFoundForUser(String addressId, String userId) {
        return new AddressNotFoundException("Address with ID " + addressId + " not found for user " + userId);
    }
}
