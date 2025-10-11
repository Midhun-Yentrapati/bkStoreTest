package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.entity.PasswordResetToken;
import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.entity.UserRole;
import com.bookstore.user_authentication_service.entity.UserType;
import com.bookstore.user_authentication_service.entity.AccountStatus;
import com.bookstore.user_authentication_service.repository.PasswordResetTokenRepository;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .userRole(UserRole.CUSTOMER)
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        testToken = PasswordResetToken.builder()
                .id("token-id")
                .token("test-reset-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
    }

    @Test
    void testInitiatePasswordReset_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // When
        assertDoesNotThrow(() -> authenticationService.initiatePasswordReset("test@example.com"));

        // Then
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordResetTokenRepository).markAllUserTokensAsUsed("test-user-id");
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void testValidatePasswordResetToken_ValidToken() {
        // Given
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("test-reset-token"))
                .thenReturn(Optional.of(testToken));

        // When
        boolean result = authenticationService.validatePasswordResetToken("test-reset-token");

        // Then
        assertTrue(result);
        verify(passwordResetTokenRepository).findByTokenAndUsedFalse("test-reset-token");
    }

    @Test
    void testValidatePasswordResetToken_InvalidToken() {
        // Given
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("invalid-token"))
                .thenReturn(Optional.empty());

        // When
        boolean result = authenticationService.validatePasswordResetToken("invalid-token");

        // Then
        assertFalse(result);
    }

    @Test
    void testResetPasswordWithToken_Success() {
        // Given
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("test-reset-token"))
                .thenReturn(Optional.of(testToken));
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // When
        assertDoesNotThrow(() -> authenticationService.resetPasswordWithToken("test-reset-token", "newPassword"));

        // Then
        verify(passwordResetTokenRepository).findByTokenAndUsedFalse("test-reset-token");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUser);
        verify(passwordResetTokenRepository).save(testToken);
        assertTrue(testToken.getUsed());
    }

    @Test
    void testResetPasswordWithToken_InvalidToken() {
        // Given
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("invalid-token"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authenticationService.resetPasswordWithToken("invalid-token", "newPassword"));
        
        assertEquals("Invalid or expired reset token", exception.getMessage());
    }

    @Test
    void testResetPasswordWithToken_WeakPassword() {
        // Given
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("test-reset-token"))
                .thenReturn(Optional.of(testToken));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> authenticationService.resetPasswordWithToken("test-reset-token", "123"));
        
        assertEquals("New password must be at least 6 characters long", exception.getMessage());
    }
}