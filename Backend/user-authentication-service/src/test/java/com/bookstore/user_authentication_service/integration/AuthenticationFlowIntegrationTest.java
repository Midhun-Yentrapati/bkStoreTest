package com.bookstore.user_authentication_service.integration;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.dto.AuthResponse.RefreshTokenRequest;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.repository.UserSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=test")
@Transactional
@DisplayName("Authentication Flow Integration Tests")
class AuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserRegistrationRequest registrationRequest;
    private AdminRegistrationRequest adminRegistrationRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Clean up database
        userSessionRepository.deleteAll();
        userRepository.deleteAll();

        registrationRequest = UserRegistrationRequest.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .fullName("Integration Test User")
                .mobileNumber("9876543210")
                .build();

        adminRegistrationRequest = AdminRegistrationRequest.builder()
                .username("integrationadmin")
                .email("admin@example.com")
                .password("AdminPass123!")
                .confirmPassword("AdminPass123!")
                .fullName("Integration Admin User")
                .userRole(UserRole.ADMIN)
                .department("IT")
                .employeeId("EMP001")
                .permissions(Set.of(Permission.USER_CREATE, Permission.USER_READ))
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("integrationuser")
                .password("Password123!")
                .build();
    }

    @Nested
    @DisplayName("Complete Registration Flow Tests")
    class RegistrationFlowTests {

        @Test
        @DisplayName("Should complete full user registration flow")
        void shouldCompleteFullUserRegistrationFlow() throws Exception {
            // Step 1: Register user
            MvcResult registrationResult = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            AuthResponse authResponse = objectMapper.readValue(
                    registrationResult.getResponse().getContentAsString(), AuthResponse.class);

            assertTrue(authResponse.getSuccess());
            assertNotNull(authResponse.getAccessToken());
            assertNotNull(authResponse.getRefreshToken());
            assertNotNull(authResponse.getUser());
            assertEquals("integrationuser", authResponse.getUser().getUsername());
            assertEquals("integration@example.com", authResponse.getUser().getEmail());

            // Step 2: Verify user exists in database
            User savedUser = userRepository.findByUsername("integrationuser").orElse(null);
            assertNotNull(savedUser);
            assertEquals("integrationuser", savedUser.getUsername());
            assertEquals("integration@example.com", savedUser.getEmail());
            assertEquals(UserRole.CUSTOMER, savedUser.getUserRole());
            assertEquals(AccountStatus.ACTIVE, savedUser.getAccountStatus());
            assertTrue(passwordEncoder.matches("Password123!", savedUser.getPassword()));

            // Step 3: Verify session created
            List<UserSession> sessions = userSessionRepository.findByUserId(savedUser.getId());
            assertEquals(1, sessions.size());
            assertTrue(sessions.get(0).getIsActive());

            // Step 4: Use token to access protected endpoint
            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + authResponse.getAccessToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("integrationuser"));
        }

        @Test
        @DisplayName("Should complete full admin registration flow")
        void shouldCompleteFullAdminRegistrationFlow() throws Exception {
            // First create a super admin to perform admin registration
            User superAdmin = User.builder()
                    .username("superadmin")
                    .email("superadmin@example.com")
                    .password(passwordEncoder.encode("SuperPass123!"))
                    .userRole(UserRole.SUPER_ADMIN)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(superAdmin);

            // Login as super admin first
            LoginRequest superAdminLogin = LoginRequest.builder()
                    .usernameOrEmail("superadmin")
                    .password("SuperPass123!")
                    .build();

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(superAdminLogin)))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), AuthResponse.class);

            // Step 1: Register admin user
            MvcResult registrationResult = mockMvc.perform(post("/api/auth/admin/register")
                    .header("Authorization", "Bearer " + loginResponse.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(adminRegistrationRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse authResponse = objectMapper.readValue(
                    registrationResult.getResponse().getContentAsString(), AuthResponse.class);

            assertTrue(authResponse.getSuccess());
            assertNotNull(authResponse.getUser());
            assertEquals("integrationadmin", authResponse.getUser().getUsername());
            assertEquals(UserRole.ADMIN, authResponse.getUser().getUserRole());

            // Step 2: Verify admin user in database
            User savedAdmin = userRepository.findByUsername("integrationadmin").orElse(null);
            assertNotNull(savedAdmin);
            assertEquals(UserRole.ADMIN, savedAdmin.getUserRole());
            assertEquals("IT", savedAdmin.getDepartment());
            assertEquals("EMP001", savedAdmin.getEmployeeId());
        }

        @Test
        @DisplayName("Should handle registration validation errors")
        void shouldHandleRegistrationValidationErrors() throws Exception {
            // Test with invalid email
            registrationRequest.setEmail("invalid-email");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());

            // Test with mismatched passwords
            registrationRequest.setEmail("valid@example.com");
            registrationRequest.setConfirmPassword("DifferentPassword123!");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Complete Login Flow Tests")
    class LoginFlowTests {

        @Test
        @DisplayName("Should complete full login flow with username")
        void shouldCompleteFullLoginFlowWithUsername() throws Exception {
            // Step 1: Create user first
            User user = User.builder()
                    .username("loginuser")
                    .email("login@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .fullName("Login Test User")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(true)
                    .failedLoginAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest loginWithUsername = LoginRequest.builder()
                    .usernameOrEmail("loginuser")
                    .password("Password123!")
                    .build();

            // Step 2: Login
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginWithUsername)))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse authResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), AuthResponse.class);

            assertTrue(authResponse.getSuccess());
            assertNotNull(authResponse.getAccessToken());
            assertNotNull(authResponse.getRefreshToken());
            assertEquals("loginuser", authResponse.getUser().getUsername());

            // Step 3: Verify session created
            List<UserSession> sessions = userSessionRepository.findByUserId(user.getId());
            assertEquals(1, sessions.size());
            assertTrue(sessions.get(0).getIsActive());

            // Step 4: Verify last login time updated
            User updatedUser = userRepository.findById(user.getId()).orElse(null);
            assertNotNull(updatedUser);
            assertNotNull(updatedUser.getLastLoginAt());
            assertEquals(0, updatedUser.getFailedLoginAttempts());
        }

        @Test
        @DisplayName("Should complete full login flow with email")
        void shouldCompleteFullLoginFlowWithEmail() throws Exception {
            // Step 1: Create user first
            User user = User.builder()
                    .username("emailuser")
                    .email("emaillogin@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest loginWithEmail = LoginRequest.builder()
                    .usernameOrEmail("emaillogin@example.com")
                    .password("Password123!")
                    .build();

            // Step 2: Login with email
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginWithEmail)))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse authResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), AuthResponse.class);

            assertTrue(authResponse.getSuccess());
            assertEquals("emailuser", authResponse.getUser().getUsername());
        }

        @Test
        @DisplayName("Should handle failed login attempts")
        void shouldHandleFailedLoginAttempts() throws Exception {
            // Step 1: Create user
            User user = User.builder()
                    .username("failuser")
                    .email("fail@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(true)
                    .failedLoginAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest wrongPasswordLogin = LoginRequest.builder()
                    .usernameOrEmail("failuser")
                    .password("WrongPassword123!")
                    .build();

            // Step 2: Attempt login with wrong password multiple times
            for (int i = 1; i <= 3; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordLogin)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.success").value(false));

                // Verify failed attempts incremented
                User updatedUser = userRepository.findById(user.getId()).orElse(null);
                assertNotNull(updatedUser);
                assertEquals(i, updatedUser.getFailedLoginAttempts());
            }

            // Step 3: Verify account gets locked after max attempts
            for (int i = 4; i <= 5; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordLogin)))
                        .andExpect(status().isUnauthorized());
            }

            User lockedUser = userRepository.findById(user.getId()).orElse(null);
            assertNotNull(lockedUser);
            assertEquals(AccountStatus.LOCKED, lockedUser.getAccountStatus());
        }
    }

    @Nested
    @DisplayName("Session Management Flow Tests")
    class SessionManagementFlowTests {

        @Test
        @DisplayName("Should handle multiple concurrent sessions")
        void shouldHandleMultipleConcurrentSessions() throws Exception {
            // Step 1: Create user
            User user = User.builder()
                    .username("multiuser")
                    .email("multi@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest loginReq = LoginRequest.builder()
                    .usernameOrEmail("multiuser")
                    .password("Password123!")
                    .build();

            // Step 2: Login multiple times (different sessions)
            String token1 = null, token2 = null;

            for (int i = 0; i < 2; i++) {
                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                        .andExpect(status().isOk())
                        .andReturn();

                AuthResponse authResponse = objectMapper.readValue(
                        loginResult.getResponse().getContentAsString(), AuthResponse.class);

                if (i == 0) token1 = authResponse.getAccessToken();
                if (i == 1) token2 = authResponse.getAccessToken();
            }

            // Step 3: Verify both sessions work
            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + token2))
                    .andExpect(status().isOk());

            // Step 4: Verify multiple sessions in database
            List<UserSession> sessions = userSessionRepository.findByUserId(user.getId());
            assertEquals(2, sessions.size());
            assertTrue(sessions.stream().allMatch(UserSession::getIsActive));
        }

        @Test
        @DisplayName("Should handle logout from specific session")
        void shouldHandleLogoutFromSpecificSession() throws Exception {
            // Step 1: Create user and login
            User user = User.builder()
                    .username("logoutuser")
                    .email("logout@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest loginReq = LoginRequest.builder()
                    .usernameOrEmail("logoutuser")
                    .password("Password123!")
                    .build();

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse authResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), AuthResponse.class);

            // Step 2: Logout
            mockMvc.perform(post("/api/auth/logout")
                    .header("Authorization", "Bearer " + authResponse.getAccessToken()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Step 3: Verify token no longer works
            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + authResponse.getAccessToken()))
                    .andExpect(status().isUnauthorized());

            // Step 4: Verify session marked as inactive
            List<UserSession> sessions = userSessionRepository.findByUserId(user.getId());
            assertEquals(1, sessions.size());
            assertFalse(sessions.get(0).getIsActive());
            assertNotNull(sessions.get(0).getLoggedOutAt());
        }

        @Test
        @DisplayName("Should handle logout from all sessions")
        void shouldHandleLogoutFromAllSessions() throws Exception {
            // Step 1: Create user and multiple sessions
            User user = User.builder()
                    .username("logoutalluser")
                    .email("logoutall@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest loginReq = LoginRequest.builder()
                    .usernameOrEmail("logoutalluser")
                    .password("Password123!")
                    .build();

            // Create 2 sessions
            String token1 = null, token2 = null;
            for (int i = 0; i < 2; i++) {
                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                        .andExpect(status().isOk())
                        .andReturn();

                AuthResponse authResponse = objectMapper.readValue(
                        loginResult.getResponse().getContentAsString(), AuthResponse.class);

                if (i == 0) token1 = authResponse.getAccessToken();
                if (i == 1) token2 = authResponse.getAccessToken();
            }

            // Step 2: Logout from all sessions
            mockMvc.perform(post("/api/auth/logout/all")
                    .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Step 3: Verify both tokens no longer work
            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + token1))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + token2))
                    .andExpect(status().isUnauthorized());

            // Step 4: Verify all sessions marked as inactive
            List<UserSession> sessions = userSessionRepository.findByUserId(user.getId());
            assertEquals(2, sessions.size());
            assertTrue(sessions.stream().noneMatch(UserSession::getIsActive));
        }
    }

    @Nested
    @DisplayName("Token Refresh Flow Tests")
    class TokenRefreshFlowTests {

        @Test
        @DisplayName("Should refresh access token using refresh token")
        void shouldRefreshAccessTokenUsingRefreshToken() throws Exception {
            // Step 1: Create user and login
            User user = User.builder()
                    .username("refreshuser")
                    .email("refresh@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest loginReq = LoginRequest.builder()
                    .usernameOrEmail("refreshuser")
                    .password("Password123!")
                    .build();

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(), AuthResponse.class);

            // Step 2: Use refresh token to get new access token
            AuthResponse.RefreshTokenRequest refreshRequest = AuthResponse.RefreshTokenRequest.builder()
                    .refreshToken(loginResponse.getRefreshToken())
                    .build();

            MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse refreshResponse = objectMapper.readValue(
                    refreshResult.getResponse().getContentAsString(), AuthResponse.class);

            assertTrue(refreshResponse.getSuccess());
            assertNotNull(refreshResponse.getAccessToken());
            assertNotEquals(loginResponse.getAccessToken(), refreshResponse.getAccessToken());

            // Step 3: Verify new token works
            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + refreshResponse.getAccessToken()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject invalid refresh token")
        void shouldRejectInvalidRefreshToken() throws Exception {
            RefreshTokenRequest invalidRefreshRequest = RefreshTokenRequest.builder()
                    .refreshToken("invalid.refresh.token")
                    .build();

            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRefreshRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Account Status Flow Tests")
    class AccountStatusFlowTests {

        @Test
        @DisplayName("Should handle inactive account login attempt")
        void shouldHandleInactiveAccountLoginAttempt() throws Exception {
            // Step 1: Create inactive user
            User user = User.builder()
                    .username("inactiveuser")
                    .email("inactive@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.INACTIVE)
                    .isEmailVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest loginReq = LoginRequest.builder()
                    .usernameOrEmail("inactiveuser")
                    .password("Password123!")
                    .build();

            // Step 2: Attempt login
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("account")));
        }

        @Test
        @DisplayName("Should handle unverified email account")
        void shouldHandleUnverifiedEmailAccount() throws Exception {
            // Step 1: Create unverified user
            User user = User.builder()
                    .username("unverifieduser")
                    .email("unverified@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isEmailVerified(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            LoginRequest loginReq = LoginRequest.builder()
                    .usernameOrEmail("unverifieduser")
                    .password("Password123!")
                    .build();

            // Step 2: Attempt login (behavior depends on your business rules)
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
