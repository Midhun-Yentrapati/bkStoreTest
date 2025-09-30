package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.exception.*;
import com.bookstore.user_authentication_service.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuthenticationService
 * Tests the complete authentication flow with real database interactions
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=testSecretKey123456789012345678901234567890",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=86400000"
})
class AuthenticationServiceIntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserService userService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Clean up database
        userSessionRepository.deleteAll();
        userRepository.deleteAll();

        // Create and save test user
        testUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("integrationtestuser")
                .email("integration@test.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK") // "password123" encoded
                .fullName("Integration Test User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLockedUntil(null) // null means account is not locked
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser = userRepository.save(testUser);

        // Setup login request
        loginRequest = LoginRequest.builder()
                .usernameOrEmail("integrationtestuser")
                .password("password123")
                .ipAddress("192.168.1.100")
                .userAgent("Integration Test Agent")
                .device("Test Device")
                .location("Test Location")
                .rememberMe(false)
                .build();
    }

    @Test
    void testCompleteAuthenticationFlow_Success() {
        // Test 1: Authenticate user
        AuthResponse authResponse = authenticationService.authenticateUser(loginRequest);

        assertNotNull(authResponse);
        assertTrue(authResponse.getSuccess());
        assertEquals("Authentication successful", authResponse.getMessage());
        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());
        assertEquals(testUser.getUsername(), authResponse.getUser().getUsername());
        assertEquals(testUser.getEmail(), authResponse.getUser().getEmail());

        // Test 2: Validate access token
        boolean isTokenValid = authenticationService.validateToken(authResponse.getAccessToken());
        assertTrue(isTokenValid);

        // Test 3: Extract user ID from token
        String extractedUserId = authenticationService.extractUserIdFromToken(authResponse.getAccessToken());
        assertEquals(testUser.getId(), extractedUserId);

        // Test 4: Extract user role from token
        String extractedRole = authenticationService.extractUserRoleFromToken(authResponse.getAccessToken());
        assertEquals(testUser.getUserType().name(), extractedRole);

        // Test 5: Refresh token
        AuthResponse refreshResponse = authenticationService.refreshToken(authResponse.getRefreshToken());
        assertNotNull(refreshResponse);
        assertTrue(refreshResponse.getSuccess());
        assertNotNull(refreshResponse.getAccessToken());
        assertEquals(authResponse.getRefreshToken(), refreshResponse.getRefreshToken());

        // Test 6: Logout user
        assertDoesNotThrow(() -> {
            authenticationService.logoutUser(testUser.getId());
        });

        // Verify user sessions are deactivated
        var sessions = userSessionRepository.findActiveSessionsByUserId(testUser.getId(), LocalDateTime.now());
        assertTrue(sessions.isEmpty());
    }

    @Test
    void testUserRegistrationFlow_Success() {
        // Given
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .username("newintegrationuser")
                .email("newintegration@test.com")
                .password("newpassword123")
                .fullName("New Integration User")
                .mobileNumber("9876543210")
                .build();

        // When
        AuthResponse registrationResponse = authenticationService.registerUser(registrationRequest);

        // Then
        assertNotNull(registrationResponse);
        assertTrue(registrationResponse.getSuccess());
        assertEquals("Registration successful", registrationResponse.getMessage());
        assertNotNull(registrationResponse.getAccessToken());
        assertNotNull(registrationResponse.getRefreshToken());
        assertNotNull(registrationResponse.getUser());
        assertEquals(registrationRequest.getUsername(), registrationResponse.getUser().getUsername());
        assertEquals(registrationRequest.getEmail(), registrationResponse.getUser().getEmail());

        // Verify user was created in database
        var createdUser = userRepository.findByUsername(registrationRequest.getUsername());
        assertTrue(createdUser.isPresent());
        assertEquals(registrationRequest.getEmail(), createdUser.get().getEmail());
        assertEquals(UserType.CUSTOMER, createdUser.get().getUserType());
        assertEquals(AccountStatus.ACTIVE, createdUser.get().getAccountStatus());
    }

    @Test
    void testAdminRegistrationFlow_Success() {
        // Given
        AdminRegistrationRequest adminRequest = AdminRegistrationRequest.builder()
                .username("newadminuser")
                .email("newadmin@test.com")
                .password("adminpassword123")
                .fullName("New Admin User")
                .userRole(UserRole.ADMIN)
                .department("IT")
                .build();

        // When
        AuthResponse adminResponse = authenticationService.registerAdminUser(adminRequest);

        // Then
        assertNotNull(adminResponse);
        assertTrue(adminResponse.getSuccess());
        assertEquals("Admin registration successful", adminResponse.getMessage());
        assertNotNull(adminResponse.getAccessToken());
        assertNotNull(adminResponse.getRefreshToken());
        assertNotNull(adminResponse.getUser());
        assertEquals(adminRequest.getUsername(), adminResponse.getUser().getUsername());
        assertEquals(adminRequest.getEmail(), adminResponse.getUser().getEmail());

        // Verify admin user was created in database
        var createdAdmin = userRepository.findByUsername(adminRequest.getUsername());
        assertTrue(createdAdmin.isPresent());
        assertEquals(adminRequest.getEmail(), createdAdmin.get().getEmail());
        assertEquals(UserType.ADMIN, createdAdmin.get().getUserType());
    }

    @Test
    void testFailedLoginAttempts_AccountLocking() {
        // Test multiple failed login attempts
        LoginRequest wrongPasswordRequest = LoginRequest.builder()
                .usernameOrEmail(testUser.getUsername())
                .password("wrongpassword")
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent")
                .build();

        // Attempt 1-4: Should fail but not lock account
        for (int i = 1; i <= 4; i++) {
            assertThrows(AuthenticationException.class, () -> {
                authenticationService.authenticateUser(wrongPasswordRequest);
            });

            // Refresh user from database
            testUser = userRepository.findById(testUser.getId()).orElseThrow();
            assertEquals(i, testUser.getFailedLoginAttempts());
            assertTrue(testUser.isAccountNonLocked()); // Account should NOT be locked
        }

        // Attempt 5: Should lock the account
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(wrongPasswordRequest);
        });

        // Refresh user from database
        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(5, testUser.getFailedLoginAttempts());
        assertFalse(testUser.isAccountNonLocked()); // Account should be locked

        // Attempt 6: Should fail due to locked account even with correct password
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(loginRequest);
        });
    }

    @Test
    void testSessionManagement() {
        // Authenticate user to create session
        AuthResponse authResponse = authenticationService.authenticateUser(loginRequest);
        assertNotNull(authResponse.getSessionId());

        // Verify session exists and is active
        var activeSessions = userSessionRepository.findActiveSessionsByUserId(testUser.getId(), LocalDateTime.now());
        assertEquals(1, activeSessions.size());
        assertTrue(activeSessions.get(0).getIsActive());

        // Logout specific session
        authenticationService.logout(authResponse.getSessionId());

        // Verify session is deactivated
        activeSessions = userSessionRepository.findActiveSessionsByUserId(testUser.getId(), LocalDateTime.now());
        assertTrue(activeSessions.isEmpty());
    }

    @Test
    void testMultipleSessionsAndLogoutAll() {
        // Create multiple sessions by logging in multiple times
        AuthResponse session1 = authenticationService.authenticateUser(loginRequest);
        
        LoginRequest secondLogin = LoginRequest.builder()
                .usernameOrEmail(testUser.getEmail())
                .password("password123")
                .ipAddress("192.168.1.101")
                .userAgent("Different Agent")
                .build();
        
        AuthResponse session2 = authenticationService.authenticateUser(secondLogin);

        // Verify multiple active sessions
        var activeSessions = userSessionRepository.findActiveSessionsByUserId(testUser.getId(), LocalDateTime.now());
        assertEquals(2, activeSessions.size());

        // Logout all sessions
        authenticationService.logoutAllSessions(testUser.getId());

        // Verify all sessions are deactivated
        activeSessions = userSessionRepository.findActiveSessionsByUserId(testUser.getId(), LocalDateTime.now());
        assertTrue(activeSessions.isEmpty());
    }

    @Test
    void testPasswordOperations() {
        // Test password encoding
        String rawPassword = "testpassword123";
        String encodedPassword = authenticationService.encodePassword(rawPassword);
        
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));

        // Test password verification
        assertTrue(authenticationService.verifyPassword(rawPassword, encodedPassword));
        assertFalse(authenticationService.verifyPassword("wrongpassword", encodedPassword));
    }

    @Test
    void testTokenExpiration_Integration() {
        // This test would require manipulating system time or using shorter token expiration
        // For now, we'll test the token validation logic
        
        AuthResponse authResponse = authenticationService.authenticateUser(loginRequest);
        String accessToken = authResponse.getAccessToken();
        
        // Token should be valid immediately after creation
        assertTrue(authenticationService.validateToken(accessToken));
        
        // Test with invalid token format
        assertFalse(authenticationService.validateToken("invalid.token.format"));
        assertFalse(authenticationService.validateToken(""));
        assertFalse(authenticationService.validateToken(null));
    }

    @Test
    void testConcurrentAuthentication() {
        // Test concurrent authentication attempts
        // This is a simplified test - in real scenarios you'd use threading
        
        AuthResponse response1 = authenticationService.authenticateUser(loginRequest);
        
        // Second authentication with same credentials should succeed
        AuthResponse response2 = authenticationService.authenticateUser(loginRequest);
        
        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.getSuccess());
        assertTrue(response2.getSuccess());
        
        // Should have different session IDs
        assertNotEquals(response1.getSessionId(), response2.getSessionId());
        
        // Should have different access tokens
        assertNotEquals(response1.getAccessToken(), response2.getAccessToken());
    }

    @Test
    void testDatabaseConstraints() {
        // Test unique username constraint
        UserRegistrationRequest duplicateUsernameRequest = UserRegistrationRequest.builder()
                .username(testUser.getUsername()) // Same username as existing user
                .email("different@email.com")
                .password("password123")
                .fullName("Different User")
                .build();

        assertThrows(Exception.class, () -> {
            authenticationService.registerUser(duplicateUsernameRequest);
        });

        // Test unique email constraint
        UserRegistrationRequest duplicateEmailRequest = UserRegistrationRequest.builder()
                .username("differentusername")
                .email(testUser.getEmail()) // Same email as existing user
                .password("password123")
                .fullName("Different User")
                .build();

        assertThrows(Exception.class, () -> {
            authenticationService.registerUser(duplicateEmailRequest);
        });
    }

    @Test
    void testInvalidTokenOperations() {
        // Test extracting user ID from invalid tokens
        assertNull(authenticationService.extractUserIdFromToken(null));
        assertNull(authenticationService.extractUserIdFromToken(""));
        assertNull(authenticationService.extractUserIdFromToken("invalid.token"));
        
        // Test extracting user role from invalid tokens
        assertNull(authenticationService.extractUserRoleFromToken(null));
        assertNull(authenticationService.extractUserRoleFromToken(""));
        assertNull(authenticationService.extractUserRoleFromToken("invalid.token"));
        
        // Test refresh with invalid tokens
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.refreshToken(null);
        });
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.refreshToken("");
        });
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.refreshToken("invalid.refresh.token");
        });
    }

    @Test
    void testAccountStatusValidation() {
        // Create inactive user
        User inactiveUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("inactiveuser")
                .email("inactive@test.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK")
                .fullName("Inactive User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.INACTIVE)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        userRepository.save(inactiveUser);
        
        LoginRequest inactiveLoginRequest = LoginRequest.builder()
                .usernameOrEmail("inactiveuser")
                .password("password123")
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent")
                .build();
        
        // Should throw exception for inactive account
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(inactiveLoginRequest);
        });
    }

    @Test
    void testUserNotFoundScenarios() {
        LoginRequest nonExistentUserRequest = LoginRequest.builder()
                .usernameOrEmail("nonexistentuser")
                .password("password123")
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent")
                .build();
        
        // Should throw exception for non-existent user
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(nonExistentUserRequest);
        });
        
        // Test logout operations with non-existent user
        assertDoesNotThrow(() -> {
            authenticationService.logoutUser("non-existent-user-id");
        });
        
        assertDoesNotThrow(() -> {
            authenticationService.logoutAllSessions("non-existent-user-id");
        });
    }

    @Test
    void testSessionOperations() {
        // Test logout with non-existent session
        assertDoesNotThrow(() -> {
            authenticationService.logout("non-existent-session-id");
        });
        
        // Test session validation
        AuthResponse authResponse = authenticationService.authenticateUser(loginRequest);
        String sessionId = authResponse.getSessionId();
        
        // Session should be valid initially
        assertTrue(authenticationService.isSessionValid(sessionId));
        
        // After logout, session should be invalid
        authenticationService.logout(sessionId);
        assertFalse(authenticationService.isSessionValid(sessionId));
        
        // Test with invalid session ID
        assertFalse(authenticationService.isSessionValid("invalid-session-id"));
        assertFalse(authenticationService.isSessionValid(null));
    }

    @Test
    void testPasswordValidation() {
        // Test password verification with null/empty values
        assertFalse(authenticationService.verifyPassword(null, "encoded"));
        assertFalse(authenticationService.verifyPassword("", "encoded"));
        assertFalse(authenticationService.verifyPassword("password", null));
        assertFalse(authenticationService.verifyPassword("password", ""));
        
        // Test password encoding with null/empty values
        assertThrows(Exception.class, () -> {
            authenticationService.encodePassword(null);
        });
        assertThrows(Exception.class, () -> {
            authenticationService.encodePassword("");
        });
    }

    @Test
    void testRegistrationValidation() {
        // Test registration with null request
        assertThrows(Exception.class, () -> {
            authenticationService.registerUser(null);
        });
        
        // Test admin registration with null request
        assertThrows(Exception.class, () -> {
            authenticationService.registerAdminUser(null);
        });
        
        // Test registration with invalid data
        UserRegistrationRequest invalidRequest = UserRegistrationRequest.builder()
                .username("") // Empty username
                .email("invalid-email") // Invalid email format
                .password("123") // Too short password
                .build();
        
        assertThrows(Exception.class, () -> {
            authenticationService.registerUser(invalidRequest);
        });
    }

    @Test
    void testTokenGeneration() {
        // Test token generation with null user
        assertThrows(Exception.class, () -> {
            authenticationService.generateAccessToken(null);
        });
        
        assertThrows(Exception.class, () -> {
            authenticationService.generateRefreshToken(null);
        });
        
        // Test token generation with valid user
        String accessToken = authenticationService.generateAccessToken(testUser);
        String refreshToken = authenticationService.generateRefreshToken(testUser);
        
        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertNotEquals(accessToken, refreshToken);
    }

    @Test
    void testAccountLockingEdgeCases() {
        // Test account locking behavior
        User lockTestUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("locktestuser")
                .email("locktest@test.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK")
                .fullName("Lock Test User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginAttempts(4) // Already 4 failed attempts
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        userRepository.save(lockTestUser);
        
        LoginRequest wrongPasswordRequest = LoginRequest.builder()
                .usernameOrEmail("locktestuser")
                .password("wrongpassword")
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent")
                .build();
        
        // One more failed attempt should lock the account
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(wrongPasswordRequest);
        });
        
        // Verify account is locked
        lockTestUser = userRepository.findById(lockTestUser.getId()).orElseThrow();
        assertEquals(5, lockTestUser.getFailedLoginAttempts());
        assertFalse(lockTestUser.isAccountNonLocked());
    }

    @Test
    void testSessionInvalidation() {
        // Create session
        AuthResponse authResponse = authenticationService.authenticateUser(loginRequest);
        String sessionId = authResponse.getSessionId();
        
        // Test invalidateSession method
        authenticationService.invalidateSession(sessionId);
        
        // Session should be invalid
        assertFalse(authenticationService.isSessionValid(sessionId));
        
        // Test invalidateAllUserSessions method
        AuthResponse newAuthResponse = authenticationService.authenticateUser(loginRequest);
        authenticationService.invalidateAllUserSessions(testUser.getId());
        
        // All sessions should be invalid
        assertFalse(authenticationService.isSessionValid(newAuthResponse.getSessionId()));
    }
}
