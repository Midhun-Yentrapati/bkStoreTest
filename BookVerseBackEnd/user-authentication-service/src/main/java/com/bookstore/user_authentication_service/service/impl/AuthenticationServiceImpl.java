package com.bookstore.user_authentication_service.service.impl;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.exception.*;
import com.bookstore.user_authentication_service.repository.*;
import com.bookstore.user_authentication_service.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    
    @Override
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsernameOrEmail());
        
        try {
            // Find user by username or email
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                    .orElseThrow(() -> AuthenticationException.invalidCredentials());
            
            // Check account status
            validateAccountStatus(user);
            
            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                handleFailedLoginAttempt(user.getId());
                throw AuthenticationException.invalidCredentials();
            }
            
            // Handle successful login
            handleSuccessfulLogin(user.getId(), loginRequest.getIpAddress());
            
            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // Create session
            String sessionId = createUserSession(user, loginRequest);
            
            // Convert user to DTO
            UserDTO userDTO = convertToUserDTO(user);
            
            log.info("User authenticated successfully: {}", user.getUsername());
            
            return AuthResponse.success(
                "Authentication successful",
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration() / 1000, // Convert to seconds
                userDTO,
                sessionId
            );
            
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {} - {}", loginRequest.getUsernameOrEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new AuthenticationException("Authentication failed due to system error");
        }
    }
    
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");
        
        try {
            // Validate refresh token
            if (!jwtService.isTokenValid(refreshToken, null)) {
                throw AuthenticationException.tokenInvalid();
            }
            
            // Extract user ID from token
            String userId = jwtService.extractUserId(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> UserNotFoundException.byId(userId));
            
            // Validate account status
            validateAccountStatus(user);
            
            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(user);
            
            // Convert user to DTO
            UserDTO userDTO = convertToUserDTO(user);
            
            log.info("Token refreshed successfully for user: {}", user.getUsername());
            
            return AuthResponse.success(
                "Token refreshed successfully",
                newAccessToken,
                refreshToken, // Keep the same refresh token
                jwtService.getAccessTokenExpiration() / 1000,
                userDTO,
                null
            );
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw AuthenticationException.tokenInvalid();
        }
    }
    
    @Override
    public AuthResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        try {
            // Validate uniqueness
            if (userRepository.existsByUsername(request.getUsername())) {
                throw ValidationException.usernameAlreadyExists(request.getUsername());
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw ValidationException.emailAlreadyExists(request.getEmail());
            }
            
            // Create user
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .mobileNumber(request.getMobileNumber())
                    .dateOfBirth(request.getDateOfBirth())
                    .bio(request.getBio())
                    .userRole(UserRole.CUSTOMER)
                    .userType(UserType.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(false)
                    .isMobileVerified(false)
                    .isTwoFactorEnabled(false)
                    .failedLoginAttempts(0)
                    .build();
            
            user = userRepository.save(user);
            
            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // Create login request for session
            LoginRequest loginRequest = LoginRequest.builder()
                    .usernameOrEmail(request.getUsername())
                    .ipAddress(request.getIpAddress())
                    .userAgent(request.getUserAgent())
                    .build();
            
            String sessionId = createUserSession(user, loginRequest);
            
            UserDTO userDTO = convertToUserDTO(user);
            
            log.info("User registered successfully: {}", user.getUsername());
            
            return AuthResponse.success(
                "Registration successful",
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration() / 1000,
                userDTO,
                sessionId
            );
            
        } catch (ValidationException e) {
            log.error("Registration validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Registration failed", e);
            throw new ValidationException("Registration failed due to system error");
        }
    }
    
    @Override
    public AuthResponse registerAdminUser(AdminRegistrationRequest request) {
        log.info("Registering new admin user: {}", request.getUsername());
        
        try {
            // Use UserService to create the admin user
            UserDTO createdUser = userService.createAdminUser(request);
            log.info("Admin user created successfully: {}", createdUser.getUsername());
            
            // Convert UserDTO to User entity for token generation
            User user = userRepository.findById(createdUser.getId())
                    .orElseThrow(() -> new ValidationException("Failed to retrieve created admin user"));
            
            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // Create session
            UserSession session = UserSession.builder()
                    .sessionToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(user)
                    .ipAddress(request.getIpAddress())
                    .userAgent(request.getUserAgent())
                    .isActive(true)
                    .expiresAt(LocalDateTime.now().plusHours(24)) // 24 hour expiry
                    .build();
            
            userSessionRepository.save(session);
            log.info("Admin session created for user: {}", user.getUsername());
            
            // Build successful response
            return AuthResponse.builder()
                    .success(true)
                    .message("Admin registration successful")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .userRole(user.getUserRole())
                            .userType(user.getUserType())
                            .employeeId(user.getEmployeeId())
                            .department(user.getDepartment())
                            .build())
                    .build();
                    
        } catch (ValidationException e) {
            log.error("Admin registration validation failed: {}", e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Admin registration failed for user: {}", request.getUsername(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Admin registration failed: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public String generateAccessToken(User user) {
        return jwtService.generateAccessToken(user);
    }
    
    @Override
    public String generateRefreshToken(User user) {
        return jwtService.generateRefreshToken(user);
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtService.isTokenValid(token, null);
    }
    
    @Override
    public String extractUserIdFromToken(String token) {
        return jwtService.extractUserId(token);
    }
    
    @Override
    public String extractUserRoleFromToken(String token) {
        return jwtService.extractUserRole(token);
    }
    
    @Override
    public String createUserSession(User user, LoginRequest loginRequest) {
        UserSession session = UserSession.builder()
                .sessionToken(UUID.randomUUID().toString())
                .refreshToken(UUID.randomUUID().toString())
                .user(user)
                .ipAddress(loginRequest.getIpAddress())
                .userAgent(loginRequest.getUserAgent())
                .device(loginRequest.getDevice())
                .location(loginRequest.getLocation())
                .sessionType(SessionType.WEB)
                .loginMethod(LoginMethod.PASSWORD)
                .isActive(true)
                .isTwoFactorVerified(false)
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 days expiration
                .lastAccessedAt(LocalDateTime.now())
                .build();
        
        session = userSessionRepository.save(session);
        
        log.info("Session created for user: {} with ID: {}", user.getUsername(), session.getId());
        
        return session.getId();
    }
    
    @Override
    public void invalidateSession(String sessionId) {
        logout(sessionId);
    }
    
    @Override
    public void invalidateAllUserSessions(String userId) {
        logoutAllSessions(userId);
    }
    
    @Override
    public boolean isSessionValid(String sessionId) {
        return userSessionRepository.findById(sessionId)
                .map(UserSession::isValid)
                .orElse(false);
    }
    
    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    @Override
    public boolean isAccountLocked(String userId) {
        return userRepository.findById(userId)
                .map(user -> !user.isAccountNonLocked())
                .orElse(false);
    }
    
    @Override
    public void handleFailedLoginAttempt(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            log.warn("Failed login attempt for user: {} (Attempts: {})", user.getUsername(), user.getFailedLoginAttempts());
        }
    }
    
    @Override
    public void handleSuccessfulLogin(String userId, String ipAddress) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.updateLastLogin(ipAddress);
            userRepository.save(user);
            log.info("Successful login for user: {}", user.getUsername());
        }
    }
    
    // Helper methods
    private void validateAccountStatus(User user) {
        if (!user.isAccountActive()) {
            throw AuthenticationException.accountInactive();
        }
        
        if (!user.isAccountNonLocked()) {
            throw AuthenticationException.accountLocked();
        }
        
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw AuthenticationException.accountSuspended();
        }
        
        if (user.getAccountStatus() == AccountStatus.DELETED) {
            throw AuthenticationException.accountDeleted();
        }
    }
    
    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .userRole(user.getUserRole())
                .userType(user.getUserType())
                .accountStatus(user.getAccountStatus())
                .isEmailVerified(user.getIsEmailVerified())
                .isMobileVerified(user.getIsMobileVerified())
                .createdAt(user.getCreatedAt())
                .build()
                .excludePassword();
    }
    
    // Session Management and Logout Operations
    @Override
    public void logout(String sessionId) {
        log.info("Logging out session: {}", sessionId);
        
        try {
            // Find and invalidate the session
            userSessionRepository.findById(sessionId).ifPresent(session -> {
                session.logout(); // Uses the built-in logout method which sets isActive=false and loggedOutAt=now
                userSessionRepository.save(session);
                log.info("Session {} invalidated successfully", sessionId);
            });
        } catch (Exception e) {
            log.error("Error invalidating session {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to logout session", e);
        }
    }
    
    @Override
    public void logoutUser(String userId) {
        log.info("Logging out all sessions for user: {}", userId);
        
        try {
            // Find all active sessions for the user and invalidate them
            var activeSessions = userSessionRepository.findActiveSessionsByUserId(userId, LocalDateTime.now());
            
            for (UserSession session : activeSessions) {
                session.logout(); // Uses the existing business method that sets isActive=false and loggedOutAt
            }
            
            if (!activeSessions.isEmpty()) {
                userSessionRepository.saveAll(activeSessions);
                log.info("Invalidated {} active sessions for user {}", activeSessions.size(), userId);
            } else {
                log.info("No active sessions found for user {}", userId);
            }
        } catch (Exception e) {
            log.error("Error logging out user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to logout user", e);
        }
    }
    
    @Override
    public void logoutAllSessions(String userId) {
        log.info("Logging out ALL sessions for user: {}", userId);
        
        try {
            // Find ALL sessions (active and inactive) for the user and mark as logged out
            var allSessions = userSessionRepository.findByUserId(userId);
            
            for (UserSession session : allSessions) {
                if (session.getIsActive()) {
                    session.setIsActive(false);
                    session.setLoggedOutAt(LocalDateTime.now());
                }
            }
            
            if (!allSessions.isEmpty()) {
                userSessionRepository.saveAll(allSessions);
                log.info("Invalidated {} total sessions for user {}", allSessions.size(), userId);
            }
        } catch (Exception e) {
            log.error("Error logging out all sessions for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to logout all sessions", e);
        }
    }
    
    // Placeholder implementations for remaining methods
    @Override public void sendEmailVerification(String userId) { }
    @Override public boolean verifyEmailToken(String token) { return false; }
    @Override public void sendMobileVerification(String userId) { }
    @Override public boolean verifyMobileOtp(String userId, String otp) { return false; }
    @Override public void initiatePasswordReset(String email) { }
    @Override public boolean validatePasswordResetToken(String token) { return false; }
    @Override public void resetPasswordWithToken(String token, String newPassword) { }
    @Override public void enableTwoFactorAuth(String userId) { }
    @Override public void disableTwoFactorAuth(String userId) { }
    @Override public String generateTwoFactorSecret(String userId) { return null; }
    @Override public boolean verifyTwoFactorCode(String userId, String code) { return false; }
    
    // Availability Validation
    @Override
    public void validateUsernameAvailability(String username) {
        log.info("Validating username availability: {}", username);
        
        if (userRepository.existsByUsername(username)) {
            throw ValidationException.usernameAlreadyExists(username);
        }
        
        log.info("Username is available: {}", username);
    }
    
    @Override
    public void validateEmailAvailability(String email) {
        log.info("Validating email availability: {}", email);
        
        if (userRepository.existsByEmail(email)) {
            throw ValidationException.emailAlreadyExists(email);
        }
        
        log.info("Email is available: {}", email);
    }
}
