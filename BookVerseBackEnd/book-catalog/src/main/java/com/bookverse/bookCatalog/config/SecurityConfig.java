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
            .cors(cors -> cors.disable()) // API Gateway handles CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - book browsing, categories, health checks
                .requestMatchers(HttpMethod.GET, "/api/books").permitAll() // GET all books
                .requestMatchers(HttpMethod.GET, "/api/books/{id}").permitAll() // GET book by ID
                .requestMatchers(HttpMethod.GET, "/api/books/search/**").permitAll() // Book search
                .requestMatchers(HttpMethod.GET, "/api/books/sales-category/**").permitAll() // Books by sales category
                .requestMatchers(HttpMethod.GET, "/api/books/{id}/similar").permitAll() // Similar books
                .requestMatchers(HttpMethod.GET, "/api/books/category/**").permitAll() // Books by category
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll() // Categories (read-only)
                .requestMatchers("/api/books/health").permitAll() // Health check
                .requestMatchers("/actuator/**").permitAll()
                
                // Documentation endpoints
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                
                // Admin-only endpoints - book management
                .requestMatchers("/api/books/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                .requestMatchers("/api/books/{id}/images").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                
                // Protected endpoints - require authentication for modifications
                .requestMatchers(HttpMethod.POST, "/api/books").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN", "ROLE_MANAGER")
                
                // Review endpoints - authenticated users can create/modify their own reviews
                .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reviews/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll() // Anyone can read reviews
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
} 