package com.bookstore.user_authentication_service.security;

import com.bookstore.user_authentication_service.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;
        
        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(request.getRequestURI())) {
            log.debug("Skipping JWT processing for public endpoint: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        log.debug("Processing JWT for endpoint: {}", request.getRequestURI());
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found in Authorization header for: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7);
        
        try {
            userId = jwtService.extractUserId(jwt);
            log.info("[JWT DEBUG] Extracted userId from JWT: {}", userId);
            
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                if (jwtService.isTokenValid(jwt, null)) {
                    log.info("[JWT DEBUG] JWT token is valid for user: {}", userId);
                    
                    // Extract user type and role to create proper authorities
                    String userType = jwtService.extractUserType(jwt);
                    String userRole = jwtService.extractUserRole(jwt);
                    log.info("[JWT DEBUG] Extracted userType: '{}', userRole: '{}'", userType, userRole);
                    
                    // Debug: Print all claims from the token
                    try {
                        var claims = jwtService.extractAllClaims(jwt);
                        log.info("[JWT DEBUG] All token claims: {}", claims);
                    } catch (Exception e) {
                        log.error("[JWT DEBUG] Error extracting all claims: {}", e.getMessage());
                    }
                    
                    List<SimpleGrantedAuthority> authorities = createAuthorities(userType, userRole);
                    log.info("[JWT DEBUG] Created authorities: {}", authorities);
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.info("[JWT DEBUG] JWT authentication successful for user: {} with authorities: {}", userId, authorities);
                    log.info("[JWT DEBUG] SecurityContext authentication set: {}", SecurityContextHolder.getContext().getAuthentication());
                } else {
                    log.debug("JWT token is invalid for user: {}", userId);
                }
            } else {
                log.debug("UserId is null or authentication already exists. UserId: {}, Existing auth: {}", 
                    userId, SecurityContextHolder.getContext().getAuthentication());
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: ", e);
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    private List<SimpleGrantedAuthority> createAuthorities(String userType, String userRole) {
        log.info("[AUTH DEBUG] Creating authorities for userType: '{}', userRole: '{}'", userType, userRole);
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        
        // Add base USER role for all users
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        log.info("[AUTH DEBUG] Added base ROLE_USER");
        
        // Add specific role based on userRole (more specific than userType)
        if (userRole != null) {
            log.info("[AUTH DEBUG] Processing userRole: '{}'", userRole);
            switch (userRole) {
                case "SUPER_ADMIN":
                    log.info("[AUTH DEBUG] Adding SUPER_ADMIN roles");
                    authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    break;
                case "ADMIN":
                    log.info("[AUTH DEBUG] Adding ADMIN roles");
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    break;
                case "MANAGER":
                    log.info("[AUTH DEBUG] Adding MANAGER role");
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    break;
                case "MODERATOR":
                    log.info("[AUTH DEBUG] Adding MODERATOR role");
                    authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
                    break;
                case "SUPPORT":
                    log.info("[AUTH DEBUG] Adding SUPPORT role");
                    authorities.add(new SimpleGrantedAuthority("ROLE_SUPPORT"));
                    break;
                default:
                    log.info("[AUTH DEBUG] Unknown userRole '{}', only USER role added", userRole);
                    break;
            }
        } else if ("ADMIN".equals(userType)) {
            // Fallback to userType if userRole is null
            log.info("[AUTH DEBUG] userRole is null, using userType '{}' fallback", userType);
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            log.info("[AUTH DEBUG] No userRole and userType is not ADMIN, only USER role added");
        }
        
        log.info("[AUTH DEBUG] Final authorities created: {}", authorities);
        return authorities;
    }
    
    private boolean isPublicEndpoint(String requestURI) {
        String[] publicEndpoints = {
            "/api/auth/login",
            "/api/auth/register", 
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email",
            "/api/auth/verify-phone",
            "/api/auth/check-username",
            "/api/auth/check-email",
            "/api/test",
            "/h2-console",
            "/error"
        };
        
        for (String endpoint : publicEndpoints) {
            if (requestURI.startsWith(endpoint)) {
                return true;
            }
        }
        
        return false;
    }
}
