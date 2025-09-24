package com.bookstore.user_authentication_service.service.impl;

import com.bookstore.user_authentication_service.config.JwtProperties;
import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    
    @Override
    public String generateAccessToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userRole", user.getUserRole().name());
        extraClaims.put("userType", user.getUserType().name());
        extraClaims.put("tokenType", "ACCESS");
        
        if (user.isAdmin()) {
            extraClaims.put("department", user.getDepartment());
            extraClaims.put("employeeId", user.getEmployeeId());
        }
        
        return generateTokenWithExpiration(extraClaims, user, jwtProperties.getAccessTokenExpiration());
    }
    
    @Override
    public String generateRefreshToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tokenType", "REFRESH");
        
        return generateTokenWithExpiration(extraClaims, user, jwtProperties.getRefreshTokenExpiration());
    }
    
    @Override
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return generateTokenWithExpiration(extraClaims, user, jwtProperties.getAccessTokenExpiration());
    }
    
    @Override
    public String generateTokenWithExpiration(Map<String, Object> extraClaims, User user, long expirationMillis) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSignInKey())
                .compact();
    }
    
    @Override
    public boolean isTokenValid(String token, User user) {
        try {
            final String userId = extractUserId(token);
            return (user == null || userId.equals(user.getId())) && !isTokenExpired(token);
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
    public String refreshAccessToken(String refreshToken) {
        try {
            if (!isRefreshToken(refreshToken) || isTokenExpired(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }
            
            String userId = extractUserId(refreshToken);
            
            // Fetch user from database using the extracted userId
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found for refresh token"));
            
            // Generate new access token for the user
            return generateAccessToken(user);
            
        } catch (Exception e) {
            log.error("Error refreshing access token", e);
            throw new RuntimeException("Failed to refresh token", e);
        }
    }
    
    @Override
    public void invalidateToken(String token) {
        // In a stateless JWT implementation, we can't truly invalidate tokens
        // This would require a token blacklist or database tracking
        log.info("Token invalidation requested for token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
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
    
    @Override
    public void rotateSigningKey() {
        // This would be implemented in a production system
        // to periodically rotate the signing key for enhanced security
        log.warn("Signing key rotation requested but not implemented");
    }
    
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
