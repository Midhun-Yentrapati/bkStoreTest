package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.User;

public interface AuthenticationService {
    
    // Authentication Operations
    AuthResponse authenticateUser(LoginRequest loginRequest);
    AuthResponse refreshToken(String refreshToken);
    void logout(String sessionId);
    void logoutUser(String userId);
    void logoutAllSessions(String userId);
    
    // Registration Operations
    AuthResponse registerUser(UserRegistrationRequest request);
    AuthResponse registerAdminUser(AdminRegistrationRequest request);
    
    // Token Operations
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    boolean validateToken(String token);
    String extractUserIdFromToken(String token);
    String extractUserRoleFromToken(String token);
    
    // Session Management
    String createUserSession(User user, LoginRequest loginRequest);
    void invalidateSession(String sessionId);
    void invalidateAllUserSessions(String userId);
    boolean isSessionValid(String sessionId);
    
    // Password Operations
    boolean verifyPassword(String rawPassword, String encodedPassword);
    String encodePassword(String rawPassword);
    
    // Account Verification
    void sendEmailVerification(String userId);
    boolean verifyEmailToken(String token);
    void sendMobileVerification(String userId);
    boolean verifyMobileOtp(String userId, String otp);
    
    // Password Reset
    void initiatePasswordReset(String email);
    boolean validatePasswordResetToken(String token);
    void resetPasswordWithToken(String token, String newPassword);
    
    // Two-Factor Authentication
    void enableTwoFactorAuth(String userId);
    void disableTwoFactorAuth(String userId);
    String generateTwoFactorSecret(String userId);
    boolean verifyTwoFactorCode(String userId, String code);
    
    // Security Checks
    boolean isAccountLocked(String userId);
    void handleFailedLoginAttempt(String userId);
    void handleSuccessfulLogin(String userId, String ipAddress);
    
    // Availability Validation
    void validateUsernameAvailability(String username);
    void validateEmailAvailability(String email);
}
