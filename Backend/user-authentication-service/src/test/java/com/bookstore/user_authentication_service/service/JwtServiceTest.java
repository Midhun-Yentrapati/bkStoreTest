package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.entity.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=testSecretKey123456789012345678901234567890",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=86400000"
})
class JwtServiceTest {
    
    @Autowired
    private JwtService jwtService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create a test user for JWT operations
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setUserType(UserType.CUSTOMER);
    }
    
    @Test
    void testGenerateAccessToken() {
        // Test access token generation
        String token = jwtService.generateAccessToken(testUser);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtService.isAccessToken(token));
    }
    
    @Test
    void testGenerateRefreshToken() {
        // Test refresh token generation
        String token = jwtService.generateRefreshToken(testUser);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtService.isRefreshToken(token));
    }
    
    @Test
    void testValidateToken() {
        // Test token validation
        String token = jwtService.generateAccessToken(testUser);
        
        assertTrue(jwtService.isTokenValid(token, testUser));
        assertFalse(jwtService.isTokenExpired(token));
        assertTrue(jwtService.isTokenSignatureValid(token));
    }
    
    @Test
    void testExtractUserId() {
        // Test extracting user ID from token
        String token = jwtService.generateAccessToken(testUser);
        String extractedUserId = jwtService.extractUserId(token);
        
        assertEquals(testUser.getId(), extractedUserId);
    }
    
    @Test
    void testExtractUserType() {
        // Test extracting user type from token
        String token = jwtService.generateAccessToken(testUser);
        String extractedUserType = jwtService.extractUserType(token);
        
        assertEquals(testUser.getUserType().toString(), extractedUserType);
    }
    
    @Test
    void testExtractUsername() {
        // Test extracting username from token
        String token = jwtService.generateAccessToken(testUser);
        String extractedUsername = jwtService.extractUsername(token);
        
        assertEquals(testUser.getUsername(), extractedUsername);
    }
    
    @Test
    void testRefreshToken() {
        // Test refresh token functionality
        String refreshToken = jwtService.generateRefreshToken(testUser);
        
        assertNotNull(refreshToken);
        assertTrue(jwtService.isRefreshToken(refreshToken));
        
        // Test refreshing access token
        String newAccessToken = jwtService.refreshAccessToken(refreshToken);
        assertNotNull(newAccessToken);
        assertTrue(jwtService.isAccessToken(newAccessToken));
    }
    
    @Test
    void testTokenExpiration() {
        // Test token expiration functionality
        String token = jwtService.generateAccessToken(testUser);
        
        assertNotNull(jwtService.extractExpiration(token));
        assertNotNull(jwtService.extractIssuedAt(token));
        assertFalse(jwtService.isTokenExpired(token));
    }
}
