package com.bookstore.user_authentication_service.util;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for building test data objects
 * Provides convenient methods to create test entities and DTOs
 */
public class TestDataBuilder {

    // ========== USER ENTITY BUILDERS ==========

    public static User.UserBuilder createTestUser() {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .email("test@example.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK") // "password123"
                .fullName("Test User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLockedUntil(null) // Account not locked
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static User.UserBuilder createTestAdminUser() {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username("adminuser")
                .email("admin@example.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK") // "password123"
                .fullName("Admin User")
                .userType(UserType.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLockedUntil(null) // Account not locked
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static User.UserBuilder createLockedUser() {
        return createTestUser()
                .accountLockedUntil(LocalDateTime.now().plusHours(1)) // Lock account for 1 hour
                .failedLoginAttempts(5);
    }

    public static User.UserBuilder createInactiveUser() {
        return createTestUser()
                .accountStatus(AccountStatus.INACTIVE);
    }

    // ========== REQUEST DTO BUILDERS ==========

    public static LoginRequest.LoginRequestBuilder createLoginRequest() {
        return LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0 (Test Browser)")
                .device("Desktop")
                .location("Test Location")
                .rememberMe(false);
    }

    public static LoginRequest.LoginRequestBuilder createAdminLoginRequest() {
        return LoginRequest.builder()
                .usernameOrEmail("adminuser")
                .password("password123")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0 (Admin Browser)")
                .device("Admin Desktop")
                .location("Admin Location")
                .rememberMe(false);
    }

    public static UserRegistrationRequest.UserRegistrationRequestBuilder createUserRegistrationRequest() {
        return UserRegistrationRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("newpassword123")
                .fullName("New User")
                .mobileNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .bio("Test user bio");
    }

    public static AdminRegistrationRequest.AdminRegistrationRequestBuilder createAdminRegistrationRequest() {
        return AdminRegistrationRequest.builder()
                .username("newadmin")
                .email("newadmin@example.com")
                .password("adminpassword123")
                .fullName("New Admin")
                .userRole(UserRole.ADMIN)
                .department("IT")
                .managerId("manager-123");
    }

    // ========== RESPONSE DTO BUILDERS ==========

    public static AuthResponse.AuthResponseBuilder createSuccessAuthResponse() {
        return AuthResponse.builder()
                .success(true)
                .message("Authentication successful")
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .expiresIn(3600L)
                .tokenType("Bearer")
                .sessionId("session-123");
    }

    public static AuthResponse.AuthResponseBuilder createErrorAuthResponse() {
        return AuthResponse.builder()
                .success(false)
                .message("Authentication failed")
                .errorCode("AUTH_001")
                .errorDetails("Invalid credentials");
    }

    public static UserDTO.UserDTOBuilder createUserDTO() {
        return UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    // ========== SESSION ENTITY BUILDERS ==========

    public static UserSession.UserSessionBuilder createUserSession(User user) {
        return UserSession.builder()
                .id(UUID.randomUUID().toString())
                .sessionToken("session-token-123")
                .refreshToken("refresh-token-123")
                .user(user)
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .device("Desktop")
                .location("Test Location")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24));
    }

    // ========== ADMIN USER BUILDERS (Using User Entity) ==========

    public static User.UserBuilder createAdminUserEntity() {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .username("adminuser")
                .email("admin@example.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK")
                .fullName("Admin User")
                .userType(UserType.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLockedUntil(null) // null means account is not locked
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    public static User.UserBuilder createSuperAdminUserEntity() {
        return createAdminUserEntity()
                .username("superadmin")
                .email("superadmin@example.com")
                .fullName("Super Admin User");
    }

    public static User.UserBuilder createManagerUserEntity() {
        return createAdminUserEntity()
                .username("manager")
                .email("manager@example.com")
                .fullName("Manager User");
    }

    // ========== UTILITY METHODS ==========

    /**
     * Creates a test user with specific username and email
     */
    public static User createTestUserWithCredentials(String username, String email) {
        return createTestUser()
                .username(username)
                .email(email)
                .build();
    }

    /**
     * Creates a login request with specific credentials
     */
    public static LoginRequest createLoginRequestWithCredentials(String usernameOrEmail, String password) {
        return createLoginRequest()
                .usernameOrEmail(usernameOrEmail)
                .password(password)
                .build();
    }

    /**
     * Creates a user registration request with specific credentials
     */
    public static UserRegistrationRequest createRegistrationRequestWithCredentials(
            String username, String email, String password) {
        return createUserRegistrationRequest()
                .username(username)
                .email(email)
                .password(password)
                .build();
    }

    /**
     * Creates an admin registration request with specific role
     */
    public static AdminRegistrationRequest createAdminRegistrationRequestWithRole(UserRole role) {
        return createAdminRegistrationRequest()
                .userRole(role)
                .build();
    }

    /**
     * Creates a user with failed login attempts
     */
    public static User createUserWithFailedAttempts(int attempts) {
        return createTestUser()
                .failedLoginAttempts(attempts)
                .accountLockedUntil(attempts >= 5 ? LocalDateTime.now().plusHours(1) : null)
                .build();
    }

    /**
     * Creates multiple test users for bulk testing
     */
    public static User[] createMultipleTestUsers(int count) {
        User[] users = new User[count];
        for (int i = 0; i < count; i++) {
            users[i] = createTestUser()
                    .username("testuser" + i)
                    .email("test" + i + "@example.com")
                    .build();
        }
        return users;
    }

    /**
     * Creates a complete authentication flow test data set
     */
    public static class AuthenticationFlowData {
        public final User user;
        public final LoginRequest loginRequest;
        public final AuthResponse expectedResponse;

        public AuthenticationFlowData(User user, LoginRequest loginRequest, AuthResponse expectedResponse) {
            this.user = user;
            this.loginRequest = loginRequest;
            this.expectedResponse = expectedResponse;
        }

        public static AuthenticationFlowData createSuccessFlow() {
            User user = createTestUser().build();
            LoginRequest request = createLoginRequest()
                    .usernameOrEmail(user.getUsername())
                    .build();
            AuthResponse response = createSuccessAuthResponse()
                    .user(createUserDTO()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .build())
                    .build();
            return new AuthenticationFlowData(user, request, response);
        }

        public static AuthenticationFlowData createFailureFlow() {
            User user = createTestUser().build();
            LoginRequest request = createLoginRequest()
                    .usernameOrEmail(user.getUsername())
                    .password("wrongpassword")
                    .build();
            AuthResponse response = createErrorAuthResponse().build();
            return new AuthenticationFlowData(user, request, response);
        }
    }
}
