package com.bookstore2.Bookstore2.Security;

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
                // Extract user info from custom headers set by the API Gateway
                String userId = request.getHeader("X-User-Id");
                String username = request.getHeader("X-User-Name");
                String roles = request.getHeader("X-User-Roles");
                String userType = request.getHeader("X-User-Type");
                
                if (userId != null && username != null && roles != null) {
                    // Validate that user has admin privileges
                    if (!"ADMIN".equals(userType) && !roles.contains("ADMIN") && !roles.contains("SUPER_ADMIN") && !roles.contains("MANAGER")) {
                        log.warn("Non-admin user {} attempted to access admin endpoint: {}", username, path);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    
                    List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                            .map(role -> new SimpleGrantedAuthority(role.trim()))
                            .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Admin authentication set for user: {} with roles: {}", username, roles);
                } else {
                    log.warn("Missing required headers for admin authentication");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                log.error("Error processing JWT token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            log.warn("Missing or invalid Authorization header for admin endpoint: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/swagger-resources/") ||
               path.startsWith("/webjars/");
    }
} 