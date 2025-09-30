package com.bookverse.CartOrderManagement.config;

import com.bookverse.CartOrderManagement.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip filter for public endpoints
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String jwt = authHeader.substring(7);
                log.debug("[JWT DEBUG] Processing JWT token for request: {}", path);
                
                if (jwtService.isTokenValid(jwt)) {
                    String userId = jwtService.extractUserId(jwt);
                    String username = jwtService.extractUsername(jwt);
                    String userType = jwtService.extractUserType(jwt);
                    String userRole = jwtService.extractUserRole(jwt);
                    
                    log.debug("[JWT DEBUG] Extracted from JWT - userId: {}, username: {}, userType: {}, userRole: {}", 
                             userId, username, userType, userRole);
                    
                    if (userId != null && username != null) {
                        // Create authorities from user type and role
                        List<SimpleGrantedAuthority> authorities = createAuthorities(userType, userRole);
                        log.debug("[JWT DEBUG] Created authorities: {}", authorities);
                        
                        UsernamePasswordAuthenticationToken authToken = 
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("[JWT DEBUG] Authentication set for user: {} with authorities: {}", username, authorities);
                    } else {
                        log.warn("[JWT DEBUG] Missing required claims in JWT token");
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    log.warn("[JWT DEBUG] JWT token is invalid or expired");
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                log.error("[JWT DEBUG] Error processing JWT token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("[JWT DEBUG] No Authorization header found for request: {}", path);
        }
        
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> createAuthorities(String userType, String userRole) {
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_" + (userRole != null ? userRole.toUpperCase() : "USER")),
            new SimpleGrantedAuthority("TYPE_" + (userType != null ? userType.toUpperCase() : "CUSTOMER"))
        );
        
        return authorities;
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/cart-order/health") ||
               path.startsWith("/api/cart-order/info") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               (path.startsWith("/api/coupons/") && (path.contains("/available") || path.contains("/validate")));
    }
} 