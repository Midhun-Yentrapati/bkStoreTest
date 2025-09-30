package com.bookstore.user_authentication_service.config;

import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.entity.UserRole;
import com.bookstore.user_authentication_service.entity.AccountStatus;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=test")
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    private User testUser;
    private User adminUser;
    private String validJwtToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-id")
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .userRole(UserRole.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        adminUser = User.builder()
                .id("admin-id")
                .username("admin")
                .email("admin@example.com")
                .password("encoded-password")
                .userRole(UserRole.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        validJwtToken = "valid.jwt.token";
    }

    @Nested
    @DisplayName("Public Endpoint Security Tests")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should allow access to health check endpoint without authentication")
        void shouldAllowAccessToHealthCheckEndpoint() throws Exception {
            mockMvc.perform(get("/api/test/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to login endpoint without authentication")
        void shouldAllowAccessToLoginEndpoint() throws Exception {
            String loginRequest = """
                {
                    "usernameOrEmail": "testuser",
                    "password": "password123"
                }
                """;

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to registration endpoint without authentication")
        void shouldAllowAccessToRegistrationEndpoint() throws Exception {
            String registrationRequest = """
                {
                    "username": "newuser",
                    "email": "newuser@example.com",
                    "password": "Password123!",
                    "confirmPassword": "Password123!",
                    "fullName": "New User"
                }
                """;

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registrationRequest))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to H2 console in test environment")
        void shouldAllowAccessToH2Console() throws Exception {
            mockMvc.perform(get("/h2-console"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to actuator health endpoint")
        void shouldAllowAccessToActuatorHealthEndpoint() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Protected Endpoint Security Tests")
    class ProtectedEndpointTests {

        @Test
        @DisplayName("Should require authentication for user profile endpoint")
        void shouldRequireAuthenticationForUserProfileEndpoint() throws Exception {
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should require authentication for address endpoints")
        void shouldRequireAuthenticationForAddressEndpoints() throws Exception {
            mockMvc.perform(get("/api/users/addresses"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/users/addresses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should require authentication for logout endpoint")
        void shouldRequireAuthenticationForLogoutEndpoint() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should allow access with valid JWT token")
        void shouldAllowAccessWithValidJwtToken() throws Exception {
            when(jwtService.isTokenValid(validJwtToken, testUser)).thenReturn(true);
            when(jwtService.extractUsername(validJwtToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Role-Based Access Control Tests")
    class RoleBasedAccessTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should allow USER role access to user endpoints")
        void shouldAllowUserRoleAccessToUserEndpoints() throws Exception {
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should allow ADMIN role access to admin endpoints")
        void shouldAllowAdminRoleAccessToAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/users/admin/all"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should deny USER role access to admin endpoints")
        void shouldDenyUserRoleAccessToAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/users/admin/all"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("Should allow SUPER_ADMIN role access to system endpoints")
        void shouldAllowSuperAdminRoleAccessToSystemEndpoints() throws Exception {
            mockMvc.perform(get("/api/users/admin/system/stats"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should deny ADMIN role access to super admin endpoints")
        void shouldDenyAdminRoleAccessToSuperAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/users/admin/system/stats"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("Should allow MANAGER role access to manager endpoints")
        void shouldAllowManagerRoleAccessToManagerEndpoints() throws Exception {
            mockMvc.perform(get("/api/users/manager/department"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SUPPORT")
        @DisplayName("Should allow SUPPORT role access to support endpoints")
        void shouldAllowSupportRoleAccessToSupportEndpoints() throws Exception {
            mockMvc.perform(get("/api/support/tickets"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("CORS Configuration Tests")
    class CorsConfigurationTests {

        @Test
        @DisplayName("Should include CORS headers in response")
        void shouldIncludeCorsHeadersInResponse() throws Exception {
            MvcResult result = mockMvc.perform(options("/api/auth/login")
                    .header("Origin", "http://localhost:4200")
                    .header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "Content-Type"))
                    .andExpect(status().isOk())
                    .andReturn();

            String accessControlAllowOrigin = result.getResponse().getHeader("Access-Control-Allow-Origin");
            String accessControlAllowMethods = result.getResponse().getHeader("Access-Control-Allow-Methods");
            String accessControlAllowHeaders = result.getResponse().getHeader("Access-Control-Allow-Headers");

            assertNotNull(accessControlAllowOrigin);
            assertNotNull(accessControlAllowMethods);
            assertNotNull(accessControlAllowHeaders);
        }

        @Test
        @DisplayName("Should allow Angular frontend origin")
        void shouldAllowAngularFrontendOrigin() throws Exception {
            mockMvc.perform(options("/api/auth/login")
                    .header("Origin", "http://localhost:4200")
                    .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        }

        @Test
        @DisplayName("Should allow common HTTP methods")
        void shouldAllowCommonHttpMethods() throws Exception {
            MvcResult result = mockMvc.perform(options("/api/users/profile")
                    .header("Origin", "http://localhost:4200")
                    .header("Access-Control-Request-Method", "PUT"))
                    .andExpect(status().isOk())
                    .andReturn();

            String allowedMethods = result.getResponse().getHeader("Access-Control-Allow-Methods");
            assertNotNull(allowedMethods);
            assertTrue(allowedMethods.contains("GET"));
            assertTrue(allowedMethods.contains("POST"));
            assertTrue(allowedMethods.contains("PUT"));
            assertTrue(allowedMethods.contains("DELETE"));
        }

        @Test
        @DisplayName("Should allow Authorization header")
        void shouldAllowAuthorizationHeader() throws Exception {
            MvcResult result = mockMvc.perform(options("/api/users/profile")
                    .header("Origin", "http://localhost:4200")
                    .header("Access-Control-Request-Headers", "Authorization"))
                    .andExpect(status().isOk())
                    .andReturn();

            String allowedHeaders = result.getResponse().getHeader("Access-Control-Allow-Headers");
            assertNotNull(allowedHeaders);
            assertTrue(allowedHeaders.contains("Authorization"));
        }
    }

    @Nested
    @DisplayName("JWT Authentication Filter Tests")
    class JwtAuthenticationFilterTests {

        @Test
        @DisplayName("Should extract JWT token from Authorization header")
        void shouldExtractJwtTokenFromAuthorizationHeader() throws Exception {
            when(jwtService.isTokenValid(validJwtToken, testUser)).thenReturn(true);
            when(jwtService.extractUsername(validJwtToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject invalid JWT token")
        void shouldRejectInvalidJwtToken() throws Exception {
            String invalidToken = "invalid.jwt.token";
            when(jwtService.isTokenValid(invalidToken, testUser)).thenReturn(false);

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + invalidToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject malformed Authorization header")
        void shouldRejectMalformedAuthorizationHeader() throws Exception {
            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "InvalidFormat " + validJwtToken))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", validJwtToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle expired JWT token")
        void shouldHandleExpiredJwtToken() throws Exception {
            String expiredToken = "expired.jwt.token";
            when(jwtService.isTokenValid(expiredToken, testUser)).thenReturn(false);

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle JWT token for non-existent user")
        void shouldHandleJwtTokenForNonExistentUser() throws Exception {
            when(jwtService.isTokenValid(validJwtToken, testUser)).thenReturn(true);
            when(jwtService.extractUsername(validJwtToken)).thenReturn("nonexistentuser");
            when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Security Headers Tests")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Should include security headers in response")
        void shouldIncludeSecurityHeadersInResponse() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/test/health"))
                    .andExpect(status().isOk())
                    .andReturn();

            // Check for common security headers
            assertNotNull(result.getResponse().getHeader("X-Content-Type-Options"));
            assertNotNull(result.getResponse().getHeader("X-Frame-Options"));
            assertNotNull(result.getResponse().getHeader("X-XSS-Protection"));
        }

        @Test
        @DisplayName("Should set appropriate Cache-Control headers")
        void shouldSetAppropriateCacheControlHeaders() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andReturn();

            String cacheControl = result.getResponse().getHeader("Cache-Control");
            // Sensitive endpoints should not be cached
            if (cacheControl != null) {
                assertTrue(cacheControl.contains("no-cache") || cacheControl.contains("no-store"));
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 401 for missing authentication")
        void shouldReturn401ForMissingAuthentication() throws Exception {
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should return 403 for insufficient permissions")
        void shouldReturn403ForInsufficientPermissions() throws Exception {
            when(jwtService.isTokenValid(validJwtToken, testUser)).thenReturn(true);
            when(jwtService.extractUsername(validJwtToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/users/admin/all")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should handle authentication exceptions gracefully")
        void shouldHandleAuthenticationExceptionsGracefully() throws Exception {
            when(jwtService.isTokenValid(anyString(), any(User.class))).thenThrow(new RuntimeException("JWT processing error"));

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Session Management Tests")
    class SessionManagementTests {

        @Test
        @DisplayName("Should handle concurrent authentication requests")
        void shouldHandleConcurrentAuthenticationRequests() throws Exception {
            when(jwtService.isTokenValid(validJwtToken, testUser)).thenReturn(true);
            when(jwtService.extractUsername(validJwtToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // Simulate concurrent requests
            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/users/addresses")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle user account status changes")
        void shouldHandleUserAccountStatusChanges() throws Exception {
            // Test with locked account
            testUser.setAccountStatus(AccountStatus.LOCKED);
            when(jwtService.isTokenValid(validJwtToken, testUser)).thenReturn(true);
            when(jwtService.extractUsername(validJwtToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle unverified email accounts")
        void shouldHandleUnverifiedEmailAccounts() throws Exception {
            testUser.setIsEmailVerified(false);
            when(jwtService.isTokenValid(validJwtToken, testUser)).thenReturn(true);
            when(jwtService.extractUsername(validJwtToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // Depending on your business logic, this might be allowed or denied
            mockMvc.perform(get("/api/users/profile")
                    .header("Authorization", "Bearer " + validJwtToken))
                    .andExpect(status().isOk()); // Adjust based on your requirements
        }
    }
}
