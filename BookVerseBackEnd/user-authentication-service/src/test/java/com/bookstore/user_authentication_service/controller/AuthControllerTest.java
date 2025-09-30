package com.bookstore.user_authentication_service.controller;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.exception.*;
import com.bookstore.user_authentication_service.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for AuthController
 * Tests all REST endpoints with various scenarios
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    private LoginRequest loginRequest;
    private UserRegistrationRequest userRegistrationRequest;
    private AdminRegistrationRequest adminRegistrationRequest;
    private AuthResponse successAuthResponse;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent")
                .build();

        userRegistrationRequest = UserRegistrationRequest.builder()
                .username("newuser")
                .email("newuser@test.com")
                .password("password123")
                .fullName("New User")
                .mobileNumber("9876543210")
                .build();

        adminRegistrationRequest = AdminRegistrationRequest.builder()
                .username("newadmin")
                .email("newadmin@test.com")
                .password("adminpassword123")
                .fullName("New Admin")
                .userRole(UserRole.ADMIN)
                .department("IT")
                .build();

        testUserDTO = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .userType(UserType.CUSTOMER)
                .build();

        successAuthResponse = AuthResponse.builder()
                .success(true)
                .message("Authentication successful")
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .expiresIn(3600L)
                .user(testUserDTO)
                .sessionId("session-123")
                .build();
    }

    // ========== LOGIN TESTS ==========

    @Test
    void testLogin_Success() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(successAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication successful"))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authenticationService).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(AuthenticationException.invalidCredentials());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        verify(authenticationService).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void testLogin_AccountLocked() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(AuthenticationException.accountLocked());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testLogin_ValidationError() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .usernameOrEmail("") // Empty username
                .password("") // Empty password
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== REGISTRATION TESTS ==========

    @Test
    void testRegister_Success() throws Exception {
        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(successAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication successful"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user").exists());

        verify(authenticationService).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() throws Exception {
        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(ValidationException.usernameAlreadyExists("newuser"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRegister_EmailAlreadyExists() throws Exception {
        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(ValidationException.emailAlreadyExists("newuser@test.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ========== ADMIN REGISTRATION TESTS ==========

    @Test
    void testRegisterAdmin_Success() throws Exception {
        when(authenticationService.registerAdminUser(any(AdminRegistrationRequest.class)))
                .thenReturn(successAuthResponse);

        mockMvc.perform(post("/api/auth/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").exists());

        verify(authenticationService).registerAdminUser(any(AdminRegistrationRequest.class));
    }

    @Test
    void testRegisterAdmin_ValidationError() throws Exception {
        AdminRegistrationRequest invalidRequest = AdminRegistrationRequest.builder()
                .username("") // Empty username
                .email("invalid-email") // Invalid email
                .password("123") // Too short password
                .build();

        mockMvc.perform(post("/api/auth/admin/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== TOKEN REFRESH TESTS ==========

    @Test
    void testRefreshToken_Success() throws Exception {
        AuthController.RefreshTokenRequest refreshRequest = new AuthController.RefreshTokenRequest("refresh-token-123");
        
        when(authenticationService.refreshToken("refresh-token-123"))
                .thenReturn(successAuthResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").exists());

        verify(authenticationService).refreshToken("refresh-token-123");
    }

    @Test
    void testRefreshToken_InvalidToken() throws Exception {
        AuthController.RefreshTokenRequest refreshRequest = new AuthController.RefreshTokenRequest("invalid-token");
        
        when(authenticationService.refreshToken("invalid-token"))
                .thenThrow(AuthenticationException.tokenInvalid());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ========== LOGOUT TESTS ==========

    @Test
    void testLogout_Success() throws Exception {
        AuthController.LogoutRequest logoutRequest = new AuthController.LogoutRequest("session-123");
        
        doNothing().when(authenticationService).logout("session-123");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authenticationService).logout("session-123");
    }

    @Test
    void testLogoutAll_Success() throws Exception {
        AuthController.LogoutAllRequest logoutAllRequest = new AuthController.LogoutAllRequest("user-123");
        
        doNothing().when(authenticationService).logoutAllSessions("user-123");

        mockMvc.perform(post("/api/auth/logout-all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutAllRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All sessions logged out successfully"));

        verify(authenticationService).logoutAllSessions("user-123");
    }

    // ========== TOKEN VALIDATION TESTS ==========

    @Test
    void testValidateToken_Success() throws Exception {
        when(authenticationService.validateToken("valid-token"))
                .thenReturn(true);

        mockMvc.perform(get("/api/auth/validate")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("Token is valid"));

        verify(authenticationService).validateToken("valid-token");
    }

    @Test
    void testValidateToken_Invalid() throws Exception {
        when(authenticationService.validateToken("invalid-token"))
                .thenReturn(false);

        mockMvc.perform(get("/api/auth/validate")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Token is invalid"));
    }

    // ========== USERNAME AVAILABILITY TESTS ==========

    @Test
    void testCheckUsernameAvailability_Available() throws Exception {
        // Mock the service to not throw exception (username available)
        doNothing().when(authenticationService).validateUsernameAvailability("availableuser");

        mockMvc.perform(get("/api/auth/check-username/availableuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Username is available"));
    }

    @Test
    void testCheckUsernameAvailability_NotAvailable() throws Exception {
        doThrow(ValidationException.usernameAlreadyExists("takenuser"))
                .when(authenticationService).validateUsernameAvailability("takenuser");

        mockMvc.perform(get("/api/auth/check-username/takenuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    // ========== EMAIL AVAILABILITY TESTS ==========

    @Test
    void testCheckEmailAvailability_Available() throws Exception {
        doNothing().when(authenticationService).validateEmailAvailability("available@test.com");

        mockMvc.perform(get("/api/auth/check-email")
                        .param("email", "available@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Email is available"));
    }

    @Test
    void testCheckEmailAvailability_NotAvailable() throws Exception {
        doThrow(ValidationException.emailAlreadyExists("taken@test.com"))
                .when(authenticationService).validateEmailAvailability("taken@test.com");

        mockMvc.perform(get("/api/auth/check-email")
                        .param("email", "taken@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    void testInternalServerError() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Internal server error"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingContentType() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== CORS AND SECURITY TESTS ==========

    @Test
    void testCorsHeaders() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(successAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void testOptionsRequest() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
