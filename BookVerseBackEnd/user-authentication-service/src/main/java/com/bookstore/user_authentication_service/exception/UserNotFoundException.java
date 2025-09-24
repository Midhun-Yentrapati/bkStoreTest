package com.bookstore.user_authentication_service.exception;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static UserNotFoundException byId(String userId) {
        return new UserNotFoundException("User not found with ID: " + userId);
    }
    
    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("User not found with username: " + username);
    }
    
    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("User not found with email: " + email);
    }
    
    public static UserNotFoundException byUsernameOrEmail(String usernameOrEmail) {
        return new UserNotFoundException("User not found with username or email: " + usernameOrEmail);
    }
    
    public static UserNotFoundException byEmployeeId(String employeeId) {
        return new UserNotFoundException("User not found with employee ID: " + employeeId);
    }
}
