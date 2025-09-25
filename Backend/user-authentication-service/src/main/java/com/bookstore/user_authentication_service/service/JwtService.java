package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.entity.User;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.Map;

public interface JwtService {
    
    // Token Generation
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String generateToken(Map<String, Object> extraClaims, User user);
    String generateTokenWithExpiration(Map<String, Object> extraClaims, User user, long expirationMillis);
    
    // Token Validation
    boolean isTokenValid(String token, User user);
    boolean isTokenExpired(String token);
    boolean isTokenSignatureValid(String token);
    
    // Claims Extraction
    String extractUserId(String token);
    String extractUsername(String token);
    String extractUserRole(String token);
    String extractUserType(String token);
    Date extractExpiration(String token);
    Date extractIssuedAt(String token);
    Claims extractAllClaims(String token);
    <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver);
    
    // Token Information
    long getAccessTokenExpiration();
    long getRefreshTokenExpiration();
    String getTokenType();
    
    // Token Utilities
    String refreshAccessToken(String refreshToken);
    void invalidateToken(String token);
    boolean isRefreshToken(String token);
    boolean isAccessToken(String token);
    
    // Security
    String getSigningKey();
    void rotateSigningKey();
}
