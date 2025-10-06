package com.bookverse.bookCatalog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            //.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // --- ADMIN/PROTECTED ENDPOINTS 
                // Admin-specific operations
                .requestMatchers("/api/books/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                // Book management - Create (Admin & Super Admin only)
                .requestMatchers(HttpMethod.POST, "/api/books").hasAnyRole("ADMIN", "SUPER_ADMIN")
                // Book management - Update (Admin, Super Admin, Manager)
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                // Book management - Delete (Admin & Super Admin only)
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                // Admin review management
                .requestMatchers("/api/reviews/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // --- CUSTOMER ENDPOINTS
                // Review management (requires authenticated user)
                .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reviews/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()

                // --- PUBLIC ENDPOINTS ---
                // Public access for viewing books and categories
                .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/book/**", "/api/reviews/search/**").permitAll()
                // Health checks and API documentation
                .requestMatchers("/api/books/health", "/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Allow OPTIONS requests for CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}