package com.bookverse.CartOrderManagement.service;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public interface JwtService {
    
    // Token Validation
    boolean isTokenValid(String token);
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
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    
    // Token Information
    long getAccessTokenExpiration();
    long getRefreshTokenExpiration();
    String getTokenType();
    
    // Token Utilities
    boolean isRefreshToken(String token);
    boolean isAccessToken(String token);
    
    // Security
    String getSigningKey();
} 