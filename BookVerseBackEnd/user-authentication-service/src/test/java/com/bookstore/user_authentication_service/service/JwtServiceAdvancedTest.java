package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.entity.UserType;
import com.bookstore.user_authentication_service.util.TestDataBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Advanced JUnit tests for JwtService
 * Tests JWT token generation, validation, claims extraction, and security features
 */
@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=advancedTestSecretKey123456789012345678901234567890123456789012345678901234567890",
    "jwt.access-token-expiration=3600000", // 1 hour
    "jwt.refresh-token-expiration=86400000" // 24 hours
})
class JwtServiceAdvancedTest {

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser().build();
        adminUser = TestDataBuilder.createTestAdminUser().build();
    }

    // ========== TOKEN GENERATION TESTS ==========

    @Test
    void testGenerateAccessToken_CustomerUser() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
        assertTrue(jwtService.isAccessToken(token));
        assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    void testGenerateAccessToken_AdminUser() {
        // When
        String token = jwtService.generateAccessToken(adminUser);

        // Then
        assertNotNull(token);
        assertTrue(jwtService.isAccessToken(token));
        
        // Verify admin-specific claims
        String extractedRole = jwtService.extractUserRole(token);
        assertEquals(UserType.ADMIN.name(), extractedRole);
    }

    @Test
    void testGenerateRefreshToken() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(jwtService.isRefreshToken(refreshToken));
        assertFalse(jwtService.isAccessToken(refreshToken));
    }

    @Test
    void testGenerateTokensAreDifferent() {
        // When
        String accessToken1 = jwtService.generateAccessToken(testUser);
        String accessToken2 = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertNotEquals(accessToken1, accessToken2); // Different timestamps
        assertNotEquals(accessToken1, refreshToken);
        assertNotEquals(accessToken2, refreshToken);
    }

    // ========== TOKEN VALIDATION TESTS ==========

    @Test
    void testValidateToken_ValidAccessToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_ValidRefreshToken() {
        // Given
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(refreshToken, testUser);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidFormat() {
        // Given
        String invalidToken = "invalid.token.format";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.isTokenValid(invalidToken, testUser);
        });
    }

    @Test
    void testValidateToken_EmptyToken() {
        // When
        boolean isValid = jwtService.isTokenValid("", testUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken() {
        // When
        boolean isValid = jwtService.isTokenValid(null, testUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WrongUser() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        User differentUser = TestDataBuilder.createTestUser()
                .username("differentuser")
                .email("different@example.com")
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    // ========== CLAIMS EXTRACTION TESTS ==========

    @Test
    void testExtractUserId() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String extractedUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(testUser.getId(), extractedUserId);
    }

    @Test
    void testExtractUsername() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals(testUser.getUsername(), extractedUsername);
    }

    @Test
    void testExtractUserRole() {
        // Given
        String customerToken = jwtService.generateAccessToken(testUser);
        String adminToken = jwtService.generateAccessToken(adminUser);

        // When
        String customerRole = jwtService.extractUserRole(customerToken);
        String adminRole = jwtService.extractUserRole(adminToken);

        // Then
        assertEquals(UserType.CUSTOMER.name(), customerRole);
        assertEquals(UserType.ADMIN.name(), adminRole);
    }

    @Test
    void testExtractUserType() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String extractedUserType = jwtService.extractUserType(token);

        // Then
        assertEquals(testUser.getUserType().name(), extractedUserType);
    }

    @Test
    void testExtractExpiration() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }

    @Test
    void testExtractIssuedAt() {
        // Given
        Date beforeGeneration = new Date();
        String token = jwtService.generateAccessToken(testUser);
        Date afterGeneration = new Date();

        // When
        Date issuedAt = jwtService.extractIssuedAt(token);

        // Then
        assertNotNull(issuedAt);
        assertTrue(issuedAt.after(beforeGeneration) || issuedAt.equals(beforeGeneration));
        assertTrue(issuedAt.before(afterGeneration) || issuedAt.equals(afterGeneration));
    }

    // ========== TOKEN TYPE TESTS ==========

    @Test
    void testIsAccessToken() {
        // Given
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // When & Then
        assertTrue(jwtService.isAccessToken(accessToken));
        assertFalse(jwtService.isAccessToken(refreshToken));
    }

    @Test
    void testIsRefreshToken() {
        // Given
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // When & Then
        assertFalse(jwtService.isRefreshToken(accessToken));
        assertTrue(jwtService.isRefreshToken(refreshToken));
    }

    // ========== TOKEN EXPIRATION TESTS ==========

    @Test
    void testTokenExpiration_AccessToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        Date expiration = jwtService.extractExpiration(token);
        Date issuedAt = jwtService.extractIssuedAt(token);

        // Then
        long expectedDuration = jwtService.getAccessTokenExpiration();
        long actualDuration = expiration.getTime() - issuedAt.getTime();
        
        // Allow for small timing differences (within 1 second)
        assertTrue(Math.abs(actualDuration - expectedDuration) < 1000);
    }

    @Test
    void testTokenExpiration_RefreshToken() {
        // Given
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // When
        Date expiration = jwtService.extractExpiration(refreshToken);
        Date issuedAt = jwtService.extractIssuedAt(refreshToken);

        // Then
        long expectedDuration = jwtService.getRefreshTokenExpiration();
        long actualDuration = expiration.getTime() - issuedAt.getTime();
        
        // Allow for small timing differences (within 1 second)
        assertTrue(Math.abs(actualDuration - expectedDuration) < 1000);
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    // ========== SECURITY TESTS ==========

    @Test
    void testTokenTampering_ModifiedSignature() {
        // Given
        String validToken = jwtService.generateAccessToken(testUser);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // When & Then
        assertThrows(SignatureException.class, () -> {
            jwtService.isTokenValid(tamperedToken, testUser);
        });
    }

    @Test
    void testTokenTampering_ModifiedPayload() {
        // Given
        String validToken = jwtService.generateAccessToken(testUser);
        String[] parts = validToken.split("\\.");
        
        // Modify the payload (middle part)
        String tamperedPayload = parts[1].substring(0, parts[1].length() - 5) + "XXXXX";
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        // When & Then
        assertThrows(SignatureException.class, () -> {
            jwtService.isTokenValid(tamperedToken, testUser);
        });
    }

    // ========== EDGE CASES ==========

    @Test
    void testExtractClaims_InvalidToken() {
        // Given
        String invalidToken = "invalid.token";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractUserId(invalidToken);
        });
    }

    @Test
    void testGenerateToken_NullUser() {
        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.generateAccessToken(null);
        });
    }

    @Test
    void testGenerateToken_UserWithNullId() {
        // Given
        User userWithNullId = TestDataBuilder.createTestUser()
                .id(null)
                .build();

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.generateAccessToken(userWithNullId);
        });
    }

    // ========== PERFORMANCE TESTS ==========

    @Test
    void testTokenGeneration_Performance() {
        // Test that token generation is reasonably fast
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            jwtService.generateAccessToken(testUser);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should generate 100 tokens in less than 1 second
        assertTrue(duration < 1000, "Token generation took too long: " + duration + "ms");
    }

    @Test
    void testTokenValidation_Performance() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        
        // Test that token validation is reasonably fast
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            jwtService.isTokenValid(token, testUser);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should validate 100 tokens in less than 1 second
        assertTrue(duration < 1000, "Token validation took too long: " + duration + "ms");
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    void testCompleteTokenLifecycle() {
        // 1. Generate access token
        String accessToken = jwtService.generateAccessToken(testUser);
        assertNotNull(accessToken);
        assertTrue(jwtService.isAccessToken(accessToken));

        // 2. Validate token
        assertTrue(jwtService.isTokenValid(accessToken, testUser));

        // 3. Extract claims
        assertEquals(testUser.getId(), jwtService.extractUserId(accessToken));
        assertEquals(testUser.getUsername(), jwtService.extractUsername(accessToken));
        assertEquals(testUser.getUserType().name(), jwtService.extractUserType(accessToken));

        // 4. Check expiration
        assertFalse(jwtService.isTokenExpired(accessToken));

        // 5. Generate refresh token
        String refreshToken = jwtService.generateRefreshToken(testUser);
        assertNotNull(refreshToken);
        assertTrue(jwtService.isRefreshToken(refreshToken));

        // 6. Validate refresh token
        assertTrue(jwtService.isTokenValid(refreshToken, testUser));

        // 7. Verify tokens are different
        assertNotEquals(accessToken, refreshToken);
    }
}
