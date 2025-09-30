package com.bookverse.CartOrderManagement.service.impl;

import com.bookverse.CartOrderManagement.config.JwtProperties;
import com.bookverse.CartOrderManagement.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    
    private final JwtProperties jwtProperties;
    
    @Override
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
    
    @Override
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration", e);
            return true;
        }
    }
    
    @Override
    public boolean isTokenSignatureValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Token signature validation failed", e);
            return false;
        }
    }
    
    @Override
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }
    
    @Override
    public String extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("userRole", String.class));
    }
    
    @Override
    public String extractUserType(String token) {
        return extractClaim(token, claims -> claims.get("userType", String.class));
    }
    
    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    @Override
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }
    
    @Override
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error extracting claims from token", e);
            throw new RuntimeException("Invalid token", e);
        }
    }
    
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    @Override
    public long getAccessTokenExpiration() {
        return jwtProperties.getAccessTokenExpiration();
    }
    
    @Override
    public long getRefreshTokenExpiration() {
        return jwtProperties.getRefreshTokenExpiration();
    }
    
    @Override
    public String getTokenType() {
        return "Bearer";
    }
    
    @Override
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isAccessToken(String token) {
        try {
            String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
            return "ACCESS".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getSigningKey() {
        return jwtProperties.getSecret();
    }
    
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 