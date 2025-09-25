package com.bookstore.user_authentication_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3 Configuration for User Authentication Service
 * 
 * Provides comprehensive API documentation with JWT authentication support
 * Access: http://localhost:8081/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${spring.application.name:user-authentication-service}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway (Production)")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("BookStore User Authentication Service API")
                .description("""
                        ## User Authentication & Management Microservice
                        
                        This microservice provides comprehensive user authentication and management capabilities for the BookStore application.
                        
                        ### Key Features:
                        - **Unified Authentication System**: Single endpoint for all user types (customers, admins, managers)
                        - **Role-Based Access Control**: Hierarchical permissions (CUSTOMER, ADMIN, SUPER_ADMIN, MANAGER, MODERATOR, SUPPORT)
                        - **JWT Token Management**: Secure token generation, validation, and refresh
                        - **Session Management**: Multi-device session tracking and control
                        - **Account Security**: Failed login tracking, account locking, two-factor authentication
                        - **Profile Management**: Complete user profile and address management
                        - **Admin Features**: User management, role assignment, department organization
                        
                        ### Authentication Flow:
                        1. **Login**: POST `/api/auth/login` with username/email and password
                        2. **Get Token**: Receive JWT access token and refresh token
                        3. **Use Token**: Include `Authorization: Bearer <token>` header in requests
                        4. **Refresh**: Use refresh token to get new access token when expired
                        
                        ### User Roles & Permissions:
                        - **CUSTOMER**: Regular user access to profile, addresses, orders
                        - **SUPPORT**: Customer support capabilities
                        - **MODERATOR**: Content moderation permissions
                        - **MANAGER**: Department management and user oversight
                        - **ADMIN**: System administration capabilities
                        - **SUPER_ADMIN**: Full system access and user management
                        
                        ### Security Features:
                        - BCrypt password hashing (strength 12)
                        - JWT tokens with configurable expiration
                        - Account locking after failed attempts
                        - IP address and device tracking
                        - Session timeout and management
                        - Role-based endpoint protection
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("BookStore Development Team")
                        .email("dev@bookstore.com")
                        .url("https://bookstore.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("""
                        **JWT Authentication**
                        
                        To authenticate your requests:
                        1. Login using POST `/api/auth/login` to get your JWT token
                        2. Copy the `accessToken` from the response
                        3. Click the 'Authorize' button above
                        4. Enter: `Bearer <your-access-token>`
                        5. Click 'Authorize' to apply to all requests
                        
                        **Example Token Format:**
                        ```
                        Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLWlkIiwiaWF0IjoxNjMwMDAwMDAwLCJleHAiOjE2MzAwODY0MDB9.signature
                        ```
                        
                        **Token Expiration:**
                        - Access Token: 24 hours
                        - Refresh Token: 7 days
                        
                        Use the refresh endpoint to get new tokens when expired.
                        """);
    }
}
