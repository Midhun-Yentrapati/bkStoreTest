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
            .cors(cors -> cors.disable()) // Disabled - API Gateway handles CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // --- ADMIN/PROTECTED ENDPOINTS --- DEMO MODE: permitAll
                .requestMatchers("/api/books/admin/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/books").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/books/**").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/books/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").permitAll()
                .requestMatchers("/api/reviews/admin/**").permitAll()
                
                // --- CUSTOMER ENDPOINTS --- DEMO MODE: permitAll
                .requestMatchers(HttpMethod.POST, "/api/reviews/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/reviews/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").permitAll()

                // --- PUBLIC ENDPOINTS ---
                .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/book/**", "/api/reviews/search/**").permitAll()
                .requestMatchers("/api/books/health", "/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}