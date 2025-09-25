package com.bookstore.user_authentication_service.controller;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.exception.ValidationException;
import com.bookstore.user_authentication_service.service.AuthenticationService;
import com.bookstore.user_authentication_service.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Authentication", description = "Authentication and User Management API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Operation(
            summary = "User Login",
            description = "Authenticate user with username/email and password. Supports both customers and admin users.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Customer Login",
                                            summary = "Customer login example",
                                            value = """
                                                    {
                                                      "usernameOrEmail": "john.doe@example.com",
                                                      "password": "customerPassword123"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Admin Login",
                                            summary = "Admin login example",
                                            value = """
                                                    {
                                                      "usernameOrEmail": "admin@bookstore.com",
                                                      "password": "adminPassword123"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Login",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Authentication successful",
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "user": {
                                                "id": "user-uuid-123",
                                                "username": "johndoe",
                                                "email": "john.doe@example.com",
                                                "userRole": "CUSTOMER",
                                                "accountStatus": "ACTIVE"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed - Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Credentials",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Invalid username/email or password",
                                              "errorCode": "INVALID_CREDENTIALS"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "423",
                    description = "Account locked due to multiple failed attempts",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Account Locked",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Account is locked due to multiple failed login attempts",
                                              "errorCode": "ACCOUNT_LOCKED"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")        // POST /api/auth/login - User login
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        // Enrich login request with client information
        loginRequest.setIpAddress(getClientIpAddress(request));
        loginRequest.setUserAgent(request.getHeader("User-Agent"));
        
        log.info("Login attempt for user: {} from IP: {}", 
                loginRequest.getUsernameOrEmail(), loginRequest.getIpAddress());
        
        AuthResponse response = authenticationService.authenticateUser(loginRequest);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "User Logout",
            description = "Logout user and invalidate session. Clears user session from backend.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful Logout",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Logout successful"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Unauthorized access",
                                              "errorCode": "UNAUTHORIZED"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/logout")       // POST /api/auth/logout - User logout
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LogoutResponse> logout(Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Logout request for user: {}", userId);
        
        try {
            // Call authentication service to handle session cleanup
            authenticationService.logoutUser(userId);
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            LogoutResponse response = LogoutResponse.builder()
                    .success(true)
                    .message("Logout successful")
                    .build();
                    
            log.info("User {} logged out successfully", userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during logout for user {}: {}", userId, e.getMessage());
            
            LogoutResponse response = LogoutResponse.builder()
                    .success(false)
                    .message("Logout failed: " + e.getMessage())
                    .build();
                    
            return ResponseEntity.ok(response);
        }
    }
    
    @Operation(
            summary = "User Registration",
            description = "Register a new customer account. Creates a new user with CUSTOMER role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRegistrationRequest.class),
                            examples = @ExampleObject(
                                    name = "Customer Registration",
                                    summary = "New customer registration",
                                    value = """
                                            {
                                              "username": "johndoe123",
                                              "email": "john.doe@example.com",
                                              "password": "SecurePassword123!",
                                              "fullName": "John Doe",
                                              "mobileNumber": "+1234567890",
                                              "dateOfBirth": "1990-05-15"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registration successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Registration",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Registration successful",
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "user": {
                                                "id": "user-uuid-456",
                                                "username": "johndoe123",
                                                "email": "john.doe@example.com",
                                                "userRole": "CUSTOMER",
                                                "accountStatus": "ACTIVE",
                                                "emailVerified": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Registration failed - Validation errors or duplicate data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Username Already Exists",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Username already exists",
                                                      "errorCode": "USERNAME_EXISTS"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Email Already Registered",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Email is already registered",
                                                      "errorCode": "EMAIL_EXISTS"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/register")     // POST /api/auth/register - User registration
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody UserRegistrationRequest registrationRequest,
            HttpServletRequest request) {
        
        // Enrich registration request with client information
        registrationRequest.setIpAddress(getClientIpAddress(request));
        registrationRequest.setUserAgent(request.getHeader("User-Agent"));
        
        log.info("Registration attempt for user: {} from IP: {}", 
                registrationRequest.getUsername(), registrationRequest.getIpAddress());
        
        AuthResponse response = authenticationService.registerUser(registrationRequest);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admin/register") // POST /api/auth/admin/register - Admin registration
    public ResponseEntity<AuthResponse> registerAdmin(
            @Valid @RequestBody AdminRegistrationRequest registrationRequest,
            HttpServletRequest request) {
        
        // Enrich registration request with client information
        registrationRequest.setIpAddress(getClientIpAddress(request));
        registrationRequest.setUserAgent(request.getHeader("User-Agent"));
        
        log.info("Admin registration attempt for user: {} from IP: {}", 
                registrationRequest.getUsername(), registrationRequest.getIpAddress());
        
        AuthResponse response = authenticationService.registerAdminUser(registrationRequest);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Refresh Access Token",
            description = "Generate a new access token using a valid refresh token. Extends user session without requiring login."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/refresh")      // POST /api/auth/refresh - Token refresh
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh requested");
        
        AuthResponse response = authenticationService.refreshToken(request.getRefreshToken());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Logout",
            description = "Logout from the current session."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful"
            )
    })
    @PostMapping("/logout-session") // POST /api/auth/logout-session - Logout specific session
    public ResponseEntity<Void> logoutSession(@RequestBody LogoutRequest request) {
        log.info("Logout requested for session: {}", request.getSessionId());
        
        authenticationService.logout(request.getSessionId());
        
        return ResponseEntity.ok().build();
    }
    
    @Operation(
            summary = "Logout All Sessions",
            description = "Logout from all active sessions for the user."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout all sessions successful"
            )
    })
    @PostMapping("/logout-all")   // POST /api/auth/logout-all - Logout all sessions
    public ResponseEntity<Void> logoutAll(@RequestBody LogoutAllRequest request) {
        log.info("Logout all sessions requested for user: {}", request.getUserId());
        
        authenticationService.logoutAllSessions(request.getUserId());
        
        return ResponseEntity.ok().build();
    }
    
    @Operation(
            summary = "Debug JWT Authentication",
            description = "Debug endpoint to check JWT token claims and Spring Security authorities."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/debug/jwt")   // GET /api/auth/debug/jwt - Debug JWT authentication
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> debugJwtAuth(
            Authentication authentication,
            HttpServletRequest request) {
        
        log.info("JWT Debug endpoint called by: {}", authentication.getName());
        log.info("User authorities: {}", authentication.getAuthorities());
        
        // Extract JWT token from request
        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("authenticated", authentication.isAuthenticated());
        response.put("principalClass", authentication.getPrincipal().getClass().getSimpleName());
        
        if (jwt != null) {
            try {
                // Extract claims from JWT
                String userRole = jwtService.extractUserRole(jwt);
                String userType = jwtService.extractUserType(jwt);
                
                response.put("jwtUserRole", userRole);
                response.put("jwtUserType", userType);
                response.put("jwtPresent", true);
            } catch (Exception e) {
                response.put("jwtError", e.getMessage());
                response.put("jwtPresent", false);
            }
        } else {
            response.put("jwtPresent", false);
        }
        
        response.put("message", "JWT Debug information retrieved successfully!");
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Validate JWT Token",
            description = "Validate if a JWT access token is valid and not expired. Used for token verification."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token validation result",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenValidationResponse.class)
                    )
            )
    })
    @GetMapping("/validate")      // GET /api/auth/validate - Validate JWT token
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Parameter(description = "JWT access token to validate", required = true)
            @RequestParam String token) {
        
        boolean isValid = authenticationService.validateToken(token);
        
        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(isValid)
                .message(isValid ? "Token is valid" : "Token is invalid")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-username/{username}") // GET /api/auth/check-username/{username} - Check username availability
    public ResponseEntity<UsernameAvailabilityResponse> checkUsernameAvailability(
            @PathVariable String username) {
        
        try {
            // Validate username availability using AuthenticationService
            authenticationService.validateUsernameAvailability(username);
            
            // If no exception is thrown, username is available
            UsernameAvailabilityResponse response = UsernameAvailabilityResponse.builder()
                    .username(username)
                    .available(true)
                    .message("Username is available")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (ValidationException e) {
            // Username is already taken
            UsernameAvailabilityResponse response = UsernameAvailabilityResponse.builder()
                    .username(username)
                    .available(false)
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/check-email/{email}") // GET /api/auth/check-email/{email} - Check email availability
    public ResponseEntity<EmailAvailabilityResponse> checkEmailAvailability(
            @PathVariable String email) {
        
        try {
            // Validate email availability using AuthenticationService
            authenticationService.validateEmailAvailability(email);
            
            // If no exception is thrown, email is available
            EmailAvailabilityResponse response = EmailAvailabilityResponse.builder()
                    .email(email)
                    .available(true)
                    .message("Email is available")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (ValidationException e) {
            // Email is already registered
            EmailAvailabilityResponse response = EmailAvailabilityResponse.builder()
                    .email(email)
                    .available(false)
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/generate-hash/{password}") // GET /api/auth/generate-hash/{password} - Generate password hash
    public ResponseEntity<String> generateHash(@PathVariable String password) {
        String hash = passwordEncoder.encode(password);
        return ResponseEntity.ok("BCrypt hash for '" + password + "': " + hash);
    }
    
    @Operation(
            summary = "Debug authentication",
            description = "Get current authentication context for debugging (Admin only)"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication debug info"
            )
    })
    @GetMapping("/debug")         // GET /api/auth/debug - Debug authentication context
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.ok("Authenticated as: " + auth.getName() + 
                               " with authorities: " + authorities);
    }
    
    // Helper method to extract client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    // Additional DTOs for request/response
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LogoutRequest {
        private String sessionId;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LogoutAllRequest {
        private String userId;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenValidationResponse {
        private Boolean valid;
        private String message;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UsernameAvailabilityResponse {
        private String username;
        private Boolean available;
        private String message;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmailAvailabilityResponse {
        private String email;
        private Boolean available;
        private String message;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LogoutResponse {
        private Boolean success;
        private String message;
    }
}
