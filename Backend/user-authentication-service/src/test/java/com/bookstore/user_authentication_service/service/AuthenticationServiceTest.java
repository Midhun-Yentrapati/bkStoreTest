package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.exception.*;
import com.bookstore.user_authentication_service.repository.*;
import com.bookstore.user_authentication_service.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit tests for AuthenticationService
 * Tests all major authentication operations including login, registration, token management, and session handling
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User testUser;
    private LoginRequest loginRequest;
    private UserRegistrationRequest registrationRequest;
    private AdminRegistrationRequest adminRegistrationRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword123")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLockedUntil(null) // null means account is not locked
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup login request
        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .device("Desktop")
                .location("Test Location")
                .rememberMe(false)
                .build();

        // Setup registration request
        registrationRequest = UserRegistrationRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .fullName("New User")
                .mobileNumber("1234567890")
                .build();

        // Setup admin registration request
        adminRegistrationRequest = AdminRegistrationRequest.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("adminpass123")
                .fullName("Admin User")
                .userRole(UserRole.ADMIN)
                .department("IT")
                .build();
    }

    // ========== AUTHENTICATION TESTS ==========

    @Test
    void testAuthenticateUser_Success() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(jwtService.generateAccessToken(testUser))
                .thenReturn("access-token-123");
        when(jwtService.generateRefreshToken(testUser))
                .thenReturn("refresh-token-123");
        when(jwtService.getAccessTokenExpiration())
                .thenReturn(3600000L);
        
        // Mock session creation
        UserSession mockSession = new UserSession();
        mockSession.setId("session-123");
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(mockSession);

        // When
        AuthResponse response = authenticationService.authenticateUser(loginRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Authentication successful", response.getMessage());
        assertEquals("access-token-123", response.getAccessToken());
        assertEquals("refresh-token-123", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(testUser.getUsername(), response.getUser().getUsername());

        // Verify interactions
        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtService).generateAccessToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(loginRequest);
        });

        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testAuthenticateUser_InvalidPassword() {
        // Given
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(false);

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(loginRequest);
        });

        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    void testAuthenticateUser_AccountLocked() {
        // Given
        testUser.setAccountLockedUntil(LocalDateTime.now().plusHours(1)); // Lock account for 1 hour
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(loginRequest);
        });

        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testAuthenticateUser_InactiveAccount() {
        // Given
        testUser.setAccountStatus(AccountStatus.INACTIVE);
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.authenticateUser(loginRequest);
        });

        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    // ========== TOKEN MANAGEMENT TESTS ==========

    @Test
    void testRefreshToken_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        when(jwtService.isTokenValid(refreshToken, null)).thenReturn(true);
        when(jwtService.extractUserId(refreshToken)).thenReturn(testUser.getId());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600000L);

        // When
        AuthResponse response = authenticationService.refreshToken(refreshToken);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());

        verify(jwtService).isTokenValid(refreshToken, null);
        verify(jwtService).extractUserId(refreshToken);
        verify(userRepository).findById(testUser.getId());
        verify(jwtService).generateAccessToken(testUser);
    }

    @Test
    void testRefreshToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-refresh-token";
        when(jwtService.isTokenValid(invalidToken, null)).thenReturn(false);

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.refreshToken(invalidToken);
        });

        verify(jwtService).isTokenValid(invalidToken, null);
        verify(jwtService, never()).extractUserId(any());
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        String validToken = "valid-token";
        when(jwtService.isTokenValid(validToken, null)).thenReturn(true);

        // When
        boolean result = authenticationService.validateToken(validToken);

        // Then
        assertTrue(result);
        verify(jwtService).isTokenValid(validToken, null);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtService.isTokenValid(invalidToken, null)).thenReturn(false);

        // When
        boolean result = authenticationService.validateToken(invalidToken);

        // Then
        assertFalse(result);
        verify(jwtService).isTokenValid(invalidToken, null);
    }

    @Test
    void testExtractUserIdFromToken() {
        // Given
        String token = "valid-token";
        String expectedUserId = "user-123";
        when(jwtService.extractUserId(token)).thenReturn(expectedUserId);

        // When
        String actualUserId = authenticationService.extractUserIdFromToken(token);

        // Then
        assertEquals(expectedUserId, actualUserId);
        verify(jwtService).extractUserId(token);
    }

    @Test
    void testExtractUserRoleFromToken() {
        // Given
        String token = "valid-token";
        String expectedRole = "CUSTOMER";
        when(jwtService.extractUserRole(token)).thenReturn(expectedRole);

        // When
        String actualRole = authenticationService.extractUserRoleFromToken(token);

        // Then
        assertEquals(expectedRole, actualRole);
        verify(jwtService).extractUserRole(token);
    }

    // ========== REGISTRATION TESTS ==========

    @Test
    void testRegisterUser_Success() {
        // Given
        // Mock validation checks
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        
        // Mock password encoding
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encoded-password");
        
        // Mock user save
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Mock JWT generation
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(3600000L);
        
        // Mock session creation
        UserSession mockSession = new UserSession();
        mockSession.setId("session-123");
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(mockSession);

        // When
        AuthResponse response = authenticationService.registerUser(registrationRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Registration successful", response.getMessage());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        verify(userRepository).existsByUsername(registrationRequest.getUsername());
        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(passwordEncoder).encode(registrationRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateAccessToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void testRegisterAdminUser_Success() {
        // Given
        UserDTO createdAdminDTO = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .username(adminRegistrationRequest.getUsername())
                .email(adminRegistrationRequest.getEmail())
                .fullName(adminRegistrationRequest.getFullName())
                .userRole(UserRole.ADMIN)
                .build();

        when(userService.createAdminUser(adminRegistrationRequest)).thenReturn(createdAdminDTO);
        when(userRepository.findById(createdAdminDTO.getId())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("admin-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("admin-refresh-token");
        
        // Mock session creation
        UserSession mockSession = new UserSession();
        mockSession.setId("admin-session-123");
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(mockSession);

        // When
        AuthResponse response = authenticationService.registerAdminUser(adminRegistrationRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Admin registration successful", response.getMessage());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        verify(userService).createAdminUser(adminRegistrationRequest);
        verify(userRepository).findById(createdAdminDTO.getId());
        verify(jwtService).generateAccessToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
        verify(userSessionRepository).save(any(UserSession.class));
    }

    // ========== PASSWORD OPERATIONS TESTS ==========

    @Test
    void testVerifyPassword_CorrectPassword() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // When
        boolean result = authenticationService.verifyPassword(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testVerifyPassword_IncorrectPassword() {
        // Given
        String rawPassword = "wrongpassword";
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // When
        boolean result = authenticationService.verifyPassword(rawPassword, encodedPassword);

        // Then
        assertFalse(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void testEncodePassword() {
        // Given
        String rawPassword = "password123";
        String expectedEncodedPassword = "encodedPassword123";
        when(passwordEncoder.encode(rawPassword)).thenReturn(expectedEncodedPassword);

        // When
        String actualEncodedPassword = authenticationService.encodePassword(rawPassword);

        // Then
        assertEquals(expectedEncodedPassword, actualEncodedPassword);
        verify(passwordEncoder).encode(rawPassword);
    }

    // ========== SESSION MANAGEMENT TESTS ==========

    @Test
    void testLogoutUser_Success() {
        // Given
        String userId = testUser.getId();
        UserSession activeSession = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .user(testUser)
                .isActive(true)
                .build();

        when(userSessionRepository.findActiveSessionsByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(activeSession));

        // When
        authenticationService.logoutUser(userId);

        // Then
        verify(userSessionRepository).findActiveSessionsByUserId(eq(userId), any(LocalDateTime.class));
        verify(userSessionRepository).saveAll(anyList());
    }

    @Test
    void testLogoutAllSessions_Success() {
        // Given
        String userId = testUser.getId();
        UserSession mockSession = new UserSession();
        mockSession.setIsActive(true);
        when(userSessionRepository.findByUserId(userId)).thenReturn(List.of(mockSession));

        // When
        authenticationService.logoutAllSessions(userId);

        // Then
        verify(userSessionRepository).findByUserId(userId);
        verify(userSessionRepository).saveAll(any());
    }

    // ========== EDGE CASES AND ERROR HANDLING ==========

    @Test
    void testAuthenticateUser_NullLoginRequest() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            authenticationService.authenticateUser(null);
        });
    }

    @Test
    void testRefreshToken_NullToken() {
        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authenticationService.refreshToken(null);
        });
    }

    @Test
    void testValidateToken_NullToken() {
        // When
        boolean result = authenticationService.validateToken(null);

        // Then
        assertFalse(result);
    }

    @Test
    void testExtractUserIdFromToken_NullToken() {
        // Given
        when(jwtService.extractUserId(null)).thenReturn(null);

        // When
        String result = authenticationService.extractUserIdFromToken(null);

        // Then
        assertNull(result);
        verify(jwtService).extractUserId(null);
    }

    // ========== INTEGRATION-STYLE TESTS ==========

    @Test
    void testCompleteAuthenticationFlow() {
        // Given - Setup complete authentication flow
        when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(jwtService.generateAccessToken(any(User.class)))
                .thenReturn("access-token-123");
        when(jwtService.generateRefreshToken(any(User.class)))
                .thenReturn("refresh-token-123");
        when(jwtService.getAccessTokenExpiration())
                .thenReturn(3600000L);
        
        // Mock session creation
        UserSession mockSession = new UserSession();
        mockSession.setId("flow-session-123");
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(mockSession);

        // When - Authenticate user
        AuthResponse authResponse = authenticationService.authenticateUser(loginRequest);

        // Then - Verify authentication response
        assertNotNull(authResponse);
        assertTrue(authResponse.getSuccess());
        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());

        // Given - Setup token validation
        when(jwtService.isTokenValid(authResponse.getAccessToken(), null)).thenReturn(true);

        // When - Validate token
        boolean isValid = authenticationService.validateToken(authResponse.getAccessToken());

        // Then - Verify token is valid
        assertTrue(isValid);

        // Verify all expected interactions occurred
        verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtService).generateAccessToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).isTokenValid(authResponse.getAccessToken(), null);
    }
}
