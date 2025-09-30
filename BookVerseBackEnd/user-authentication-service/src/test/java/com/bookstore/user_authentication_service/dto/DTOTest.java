package com.bookstore.user_authentication_service.dto;

import com.bookstore.user_authentication_service.controller.AuthController.RefreshTokenRequest;
import com.bookstore.user_authentication_service.controller.AuthController.LogoutRequest;
import com.bookstore.user_authentication_service.controller.AuthController.LogoutAllRequest;
import com.bookstore.user_authentication_service.controller.AuthController.TokenValidationResponse;
import com.bookstore.user_authentication_service.controller.AuthController.UsernameAvailabilityResponse;
import com.bookstore.user_authentication_service.controller.AuthController.EmailAvailabilityResponse;
import com.bookstore.user_authentication_service.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all DTO classes
 * Tests DTO creation, serialization, validation, and mapping
 */
class DTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime support
    }

    // ========== USER DTO TESTS ==========

    @Test
    void testUserDTO_Creation() {
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .mobileNumber("9876543210")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .bio("Test bio")
                .profilePictureUrl("http://example.com/profile.jpg")
                .userRole(UserRole.CUSTOMER)
                .userType(UserType.CUSTOMER)
                .employeeId("EMP001")
                .department("IT")
                .managerId("manager-123")
                .permissions(Set.of(Permission.USER_READ, Permission.USER_UPDATE))
                .hireDate(LocalDate.of(2020, 1, 1))
                .salary(BigDecimal.valueOf(50000.0))
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .isMobileVerified(false)
                .isTwoFactorEnabled(true)
                .failedLoginAttempts(0)
                .lastLoginAt(now.minusHours(1))
                .lastLoginIp("192.168.1.100")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Test all fields
        assertEquals(userId, userDTO.getId());
        assertEquals("testuser", userDTO.getUsername());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("Test User", userDTO.getFullName());
        assertEquals("9876543210", userDTO.getMobileNumber());
        assertEquals(LocalDate.of(1990, 1, 1), userDTO.getDateOfBirth());
        assertEquals("Test bio", userDTO.getBio());
        assertEquals("http://example.com/profile.jpg", userDTO.getProfilePictureUrl());
        assertEquals(UserRole.CUSTOMER, userDTO.getUserRole());
        assertEquals(UserType.CUSTOMER, userDTO.getUserType());
        assertEquals("EMP001", userDTO.getEmployeeId());
        assertEquals("IT", userDTO.getDepartment());
        assertEquals("manager-123", userDTO.getManagerId());
        assertEquals(List.of("READ", "WRITE"), userDTO.getPermissions());
        assertEquals(LocalDate.of(2020, 1, 1), userDTO.getHireDate());
        assertEquals(50000.0, userDTO.getSalary());
        assertEquals(AccountStatus.ACTIVE, userDTO.getAccountStatus());
        assertTrue(userDTO.getIsEmailVerified());
        assertFalse(userDTO.getIsMobileVerified());
        assertTrue(userDTO.getIsTwoFactorEnabled());
        assertEquals(0, userDTO.getFailedLoginAttempts());
        assertEquals("192.168.1.100", userDTO.getLastLoginIp());
        assertNotNull(userDTO.getCreatedAt());
        assertNotNull(userDTO.getUpdatedAt());
    }

    @Test
    void testUserDTO_Serialization() throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .id("user-123")
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .build();

        // Test serialization to JSON
        String json = objectMapper.writeValueAsString(userDTO);
        assertNotNull(json);
        assertTrue(json.contains("testuser"));
        assertTrue(json.contains("test@example.com"));
        assertTrue(json.contains("CUSTOMER"));
        assertTrue(json.contains("ACTIVE"));

        // Test deserialization from JSON
        UserDTO deserializedDTO = objectMapper.readValue(json, UserDTO.class);
        assertEquals(userDTO.getId(), deserializedDTO.getId());
        assertEquals(userDTO.getUsername(), deserializedDTO.getUsername());
        assertEquals(userDTO.getEmail(), deserializedDTO.getEmail());
        assertEquals(userDTO.getUserType(), deserializedDTO.getUserType());
    }

    @Test
    void testUserDTO_SettersAndGetters() {
        UserDTO userDTO = new UserDTO();
        LocalDateTime now = LocalDateTime.now();

        // Test setters
        userDTO.setId("user-123");
        userDTO.setUsername("newuser");
        userDTO.setEmail("new@example.com");
        userDTO.setFullName("New User");
        userDTO.setUserType(UserType.ADMIN);
        userDTO.setAccountStatus(AccountStatus.INACTIVE);
        userDTO.setIsEmailVerified(false);
        userDTO.setCreatedAt(now);

        // Test getters
        assertEquals("user-123", userDTO.getId());
        assertEquals("newuser", userDTO.getUsername());
        assertEquals("new@example.com", userDTO.getEmail());
        assertEquals("New User", userDTO.getFullName());
        assertEquals(UserType.ADMIN, userDTO.getUserType());
        assertEquals(AccountStatus.INACTIVE, userDTO.getAccountStatus());
        assertFalse(userDTO.getIsEmailVerified());
        assertEquals(now, userDTO.getCreatedAt());
    }

    // ========== ADDRESS DTO TESTS ==========

    @Test
    void testAddressDTO_Creation() {
        String addressId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        AddressDTO addressDTO = AddressDTO.builder()
                .id(addressId)
                .userId("user-123")
                .name("John Doe")
                .phone("9876543210")
                .addressLine1("123 Main Street")
                .addressLine2("Apt 4B")
                .landmark("Near Central Park")
                .city("New York")
                .state("NY")
                .pincode("10001")
                .country("USA")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .instructions("Ring doorbell twice")
                .accessCode("1234")
                .latitude(40.7128)
                .longitude(-74.0060)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Test all fields
        assertEquals(addressId, addressDTO.getId());
        assertEquals("user-123", addressDTO.getUserId());
        assertEquals("John Doe", addressDTO.getName());
        assertEquals("9876543210", addressDTO.getPhone());
        assertEquals("123 Main Street", addressDTO.getAddressLine1());
        assertEquals("Apt 4B", addressDTO.getAddressLine2());
        assertEquals("Near Central Park", addressDTO.getLandmark());
        assertEquals("New York", addressDTO.getCity());
        assertEquals("NY", addressDTO.getState());
        assertEquals("10001", addressDTO.getPincode());
        assertEquals("USA", addressDTO.getCountry());
        assertEquals(AddressType.HOME, addressDTO.getAddressType());
        assertTrue(addressDTO.getIsDefault());
        assertEquals("Ring doorbell twice", addressDTO.getInstructions());
        assertEquals("1234", addressDTO.getAccessCode());
        assertEquals(40.7128, addressDTO.getLatitude());
        assertEquals(-74.0060, addressDTO.getLongitude());
        assertNotNull(addressDTO.getCreatedAt());
        assertNotNull(addressDTO.getUpdatedAt());
    }

    @Test
    void testAddressDTO_Serialization() throws Exception {
        AddressDTO addressDTO = AddressDTO.builder()
                .id("address-123")
                .userId("user-123")
                .name("John Doe")
                .addressLine1("123 Main St")
                .city("New York")
                .state("NY")
                .pincode("10001")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .build();

        // Test serialization to JSON
        String json = objectMapper.writeValueAsString(addressDTO);
        assertNotNull(json);
        assertTrue(json.contains("John Doe"));
        assertTrue(json.contains("123 Main St"));
        assertTrue(json.contains("New York"));
        assertTrue(json.contains("HOME"));

        // Test deserialization from JSON
        AddressDTO deserializedDTO = objectMapper.readValue(json, AddressDTO.class);
        assertEquals(addressDTO.getId(), deserializedDTO.getId());
        assertEquals(addressDTO.getName(), deserializedDTO.getName());
        assertEquals(addressDTO.getCity(), deserializedDTO.getCity());
        assertEquals(addressDTO.getAddressType(), deserializedDTO.getAddressType());
    }

    // ========== LOGIN REQUEST TESTS ==========

    @Test
    void testLoginRequest_Creation() {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .device("Desktop")
                .location("New York")
                .rememberMe(true)
                .build();

        assertEquals("testuser", loginRequest.getUsernameOrEmail());
        assertEquals("password123", loginRequest.getPassword());
        assertEquals("192.168.1.100", loginRequest.getIpAddress());
        assertEquals("Mozilla/5.0", loginRequest.getUserAgent());
        assertEquals("Desktop", loginRequest.getDevice());
        assertEquals("New York", loginRequest.getLocation());
        assertTrue(loginRequest.getRememberMe());
    }

    @Test
    void testLoginRequest_Serialization() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent")
                .rememberMe(false)
                .build();

        // Test serialization to JSON
        String json = objectMapper.writeValueAsString(loginRequest);
        assertNotNull(json);
        assertTrue(json.contains("testuser"));
        assertTrue(json.contains("password123"));
        assertTrue(json.contains("192.168.1.100"));

        // Test deserialization from JSON
        LoginRequest deserializedRequest = objectMapper.readValue(json, LoginRequest.class);
        assertEquals(loginRequest.getUsernameOrEmail(), deserializedRequest.getUsernameOrEmail());
        assertEquals(loginRequest.getPassword(), deserializedRequest.getPassword());
        assertEquals(loginRequest.getIpAddress(), deserializedRequest.getIpAddress());
    }

    @Test
    void testLoginRequest_Validation() {
        // Test with null values
        LoginRequest emptyRequest = LoginRequest.builder().build();
        assertNull(emptyRequest.getUsernameOrEmail());
        assertNull(emptyRequest.getPassword());
        
        // Test with empty strings
        LoginRequest emptyStringRequest = LoginRequest.builder()
                .usernameOrEmail("")
                .password("")
                .build();
        assertEquals("", emptyStringRequest.getUsernameOrEmail());
        assertEquals("", emptyStringRequest.getPassword());
    }

    // ========== USER REGISTRATION REQUEST TESTS ==========

    @Test
    void testUserRegistrationRequest_Creation() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .confirmPassword("password123")
                .fullName("New User")
                .mobileNumber("9876543210")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .bio("New user bio")
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent")
                .build();

        assertEquals("newuser", request.getUsername());
        assertEquals("new@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals("password123", request.getConfirmPassword());
        assertEquals("New User", request.getFullName());
        assertEquals("9876543210", request.getMobileNumber());
        assertEquals(LocalDate.of(1990, 1, 1), request.getDateOfBirth());
        assertEquals("New user bio", request.getBio());
        assertEquals("192.168.1.100", request.getIpAddress());
        assertEquals("Test Agent", request.getUserAgent());
    }

    @Test
    void testUserRegistrationRequest_PasswordMatching() {
        // Test matching passwords
        UserRegistrationRequest matchingRequest = UserRegistrationRequest.builder()
                .password("password123")
                .confirmPassword("password123")
                .build();
        
        assertTrue(matchingRequest.isPasswordMatching());

        // Test non-matching passwords
        UserRegistrationRequest nonMatchingRequest = UserRegistrationRequest.builder()
                .password("password123")
                .confirmPassword("differentpassword")
                .build();
        
        assertFalse(nonMatchingRequest.isPasswordMatching());

        // Test null passwords
        UserRegistrationRequest nullPasswordRequest = UserRegistrationRequest.builder()
                .password(null)
                .confirmPassword("password123")
                .build();
        
        assertFalse(nullPasswordRequest.isPasswordMatching());
    }

    @Test
    void testUserRegistrationRequest_Serialization() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .fullName("New User")
                .build();

        // Test serialization to JSON
        String json = objectMapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("newuser"));
        assertTrue(json.contains("new@example.com"));
        assertTrue(json.contains("New User"));
        // Password should be included in serialization for registration
        assertTrue(json.contains("password123"));

        // Test deserialization from JSON
        UserRegistrationRequest deserializedRequest = objectMapper.readValue(json, UserRegistrationRequest.class);
        assertEquals(request.getUsername(), deserializedRequest.getUsername());
        assertEquals(request.getEmail(), deserializedRequest.getEmail());
        assertEquals(request.getFullName(), deserializedRequest.getFullName());
    }

    // ========== ADMIN REGISTRATION REQUEST TESTS ==========

    @Test
    void testAdminRegistrationRequest_Creation() {
        AdminRegistrationRequest request = AdminRegistrationRequest.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("adminpassword123")
                .confirmPassword("adminpassword123")
                .fullName("Admin User")
                .userRole(UserRole.ADMIN)
                .department("IT")
                .employeeId("EMP001")
                .managerId("manager-123")
                .permissions(Set.of(Permission.USER_READ, Permission.USER_UPDATE))
                .salary(BigDecimal.valueOf(75000.0))
                .hireDate(LocalDate.of(2020, 1, 1))
                .ipAddress("192.168.1.100")
                .userAgent("Admin Agent")
                .build();

        assertEquals("adminuser", request.getUsername());
        assertEquals("admin@example.com", request.getEmail());
        assertEquals("adminpassword123", request.getPassword());
        assertEquals("adminpassword123", request.getConfirmPassword());
        assertEquals("Admin User", request.getFullName());
        assertEquals(UserRole.ADMIN, request.getUserRole());
        assertEquals("IT", request.getDepartment());
        assertEquals("EMP001", request.getEmployeeId());
        assertEquals("manager-123", request.getManagerId());
        assertEquals(List.of("USER_READ", "USER_WRITE"), request.getPermissions());
        assertEquals(75000.0, request.getSalary());
        assertEquals(LocalDate.of(2020, 1, 1), request.getHireDate());
        assertEquals("192.168.1.100", request.getIpAddress());
        assertEquals("Admin Agent", request.getUserAgent());
    }

    @Test
    void testAdminRegistrationRequest_Serialization() throws Exception {
        AdminRegistrationRequest request = AdminRegistrationRequest.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("adminpassword123")
                .fullName("Admin User")
                .userRole(UserRole.ADMIN)
                .department("IT")
                .build();

        // Test serialization to JSON
        String json = objectMapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("adminuser"));
        assertTrue(json.contains("admin@example.com"));
        assertTrue(json.contains("Admin User"));
        assertTrue(json.contains("ADMIN"));
        assertTrue(json.contains("IT"));

        // Test deserialization from JSON
        AdminRegistrationRequest deserializedRequest = objectMapper.readValue(json, AdminRegistrationRequest.class);
        assertEquals(request.getUsername(), deserializedRequest.getUsername());
        assertEquals(request.getEmail(), deserializedRequest.getEmail());
        assertEquals(request.getUserRole(), deserializedRequest.getUserRole());
        assertEquals(request.getDepartment(), deserializedRequest.getDepartment());
    }

    // ========== AUTH RESPONSE TESTS ==========

    @Test
    void testAuthResponse_Success() {
        UserDTO userDTO = UserDTO.builder()
                .id("user-123")
                .username("testuser")
                .email("test@example.com")
                .build();

        AuthResponse response = AuthResponse.builder()
                .success(true)
                .message("Authentication successful")
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .expiresIn(3600L)
                .user(userDTO)
                .sessionId("session-123")
                .build();

        assertTrue(response.getSuccess());
        assertEquals("Authentication successful", response.getMessage());
        assertEquals("access-token-123", response.getAccessToken());
        assertEquals("refresh-token-123", response.getRefreshToken());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(userDTO, response.getUser());
        assertEquals("session-123", response.getSessionId());
    }

    @Test
    void testAuthResponse_Failure() {
        AuthResponse response = AuthResponse.builder()
                .success(false)
                .message("Authentication failed")
                .errorCode("INVALID_CREDENTIALS")
                .errorDetails("Username or password is incorrect")
                .build();

        assertFalse(response.getSuccess());
        assertEquals("Authentication failed", response.getMessage());
        assertEquals("INVALID_CREDENTIALS", response.getErrorCode());
        assertEquals("Username or password is incorrect", response.getErrorDetails());
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertNull(response.getUser());
    }

    @Test
    void testAuthResponse_StaticMethods() {
        UserDTO userDTO = UserDTO.builder()
                .id("user-123")
                .username("testuser")
                .build();

        // Test success method
        AuthResponse successResponse = AuthResponse.success(
                "Login successful",
                "access-token",
                "refresh-token",
                3600L,
                userDTO,
                "session-123"
        );

        assertTrue(successResponse.getSuccess());
        assertEquals("Login successful", successResponse.getMessage());
        assertEquals("access-token", successResponse.getAccessToken());
        assertEquals(userDTO, successResponse.getUser());

        // Test error method
        AuthResponse failureResponse = AuthResponse.error("Login failed", "INVALID_CREDENTIALS", "Invalid username or password");

        assertFalse(failureResponse.getSuccess());
        assertEquals("Login failed", failureResponse.getMessage());
        assertEquals("INVALID_CREDENTIALS", failureResponse.getErrorCode());
        assertEquals("Invalid username or password", failureResponse.getErrorDetails());
        assertNull(failureResponse.getAccessToken());
    }

    @Test
    void testAuthResponse_Serialization() throws Exception {
        UserDTO userDTO = UserDTO.builder()
                .id("user-123")
                .username("testuser")
                .email("test@example.com")
                .build();

        AuthResponse response = AuthResponse.builder()
                .success(true)
                .message("Authentication successful")
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .user(userDTO)
                .build();

        // Test serialization to JSON
        String json = objectMapper.writeValueAsString(response);
        assertNotNull(json);
        assertTrue(json.contains("Authentication successful"));
        assertTrue(json.contains("access-token-123"));
        assertTrue(json.contains("testuser"));

        // Test deserialization from JSON
        AuthResponse deserializedResponse = objectMapper.readValue(json, AuthResponse.class);
        assertEquals(response.getSuccess(), deserializedResponse.getSuccess());
        assertEquals(response.getMessage(), deserializedResponse.getMessage());
        assertEquals(response.getAccessToken(), deserializedResponse.getAccessToken());
        assertEquals(response.getUser().getUsername(), deserializedResponse.getUser().getUsername());
    }

    // ========== INNER DTO CLASSES TESTS ==========

    @Test
    void testRefreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-123");
        assertEquals("refresh-token-123", request.getRefreshToken());

        // Test setter
        request.setRefreshToken("new-refresh-token");
        assertEquals("new-refresh-token", request.getRefreshToken());
    }

    @Test
    void testLogoutRequest() {
        LogoutRequest request = new LogoutRequest("session-123");
        assertEquals("session-123", request.getSessionId());

        // Test setter
        request.setSessionId("new-session-123");
        assertEquals("new-session-123", request.getSessionId());
    }

    @Test
    void testLogoutAllRequest() {
        LogoutAllRequest request = new LogoutAllRequest("user-123");
        assertEquals("user-123", request.getUserId());

        // Test setter
        request.setUserId("new-user-123");
        assertEquals("new-user-123", request.getUserId());
    }

    @Test
    void testTokenValidationResponse() {
        TokenValidationResponse response = new TokenValidationResponse(true, "Token is valid");
        assertTrue(response.getValid());
        assertEquals("Token is valid", response.getMessage());

        // Test setters
        response.setValid(false);
        response.setMessage("Token is invalid");
        assertFalse(response.getValid());
        assertEquals("Token is invalid", response.getMessage());
    }

    @Test
    void testUsernameAvailabilityResponse() {
        UsernameAvailabilityResponse response = new UsernameAvailabilityResponse("testuser", true, "Username is available");
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getAvailable());
        assertEquals("Username is available", response.getMessage());

        // Test setters
        response.setUsername("newuser");
        response.setAvailable(false);
        response.setMessage("Username is taken");
        assertEquals("newuser", response.getUsername());
        assertFalse(response.getAvailable());
        assertEquals("Username is taken", response.getMessage());
    }

    @Test
    void testEmailAvailabilityResponse() {
        EmailAvailabilityResponse response = new EmailAvailabilityResponse("test@example.com", false, "Email is already registered");
        assertEquals("test@example.com", response.getEmail());
        assertFalse(response.getAvailable());
        assertEquals("Email is already registered", response.getMessage());

        // Test setters
        response.setEmail("new@example.com");
        response.setAvailable(true);
        response.setMessage("Email is available");
        assertEquals("new@example.com", response.getEmail());
        assertTrue(response.getAvailable());
        assertEquals("Email is available", response.getMessage());
    }

    // ========== DTO VALIDATION TESTS ==========

    @Test
    void testDTO_NullValues() {
        // Test DTOs with null values
        UserDTO userDTO = UserDTO.builder().build();
        assertNull(userDTO.getId());
        assertNull(userDTO.getUsername());
        assertNull(userDTO.getEmail());

        AddressDTO addressDTO = AddressDTO.builder().build();
        assertNull(addressDTO.getId());
        assertNull(addressDTO.getName());
        assertNull(addressDTO.getCity());

        LoginRequest loginRequest = LoginRequest.builder().build();
        assertNull(loginRequest.getUsernameOrEmail());
        assertNull(loginRequest.getPassword());
    }

    @Test
    void testDTO_DefaultValues() {
        // Test DTOs with default values
        UserDTO userDTO = UserDTO.builder()
                .isEmailVerified(false)
                .isMobileVerified(false)
                .isTwoFactorEnabled(false)
                .failedLoginAttempts(0)
                .build();
        
        assertFalse(userDTO.getIsEmailVerified());
        assertFalse(userDTO.getIsMobileVerified());
        assertFalse(userDTO.getIsTwoFactorEnabled());
        assertEquals(0, userDTO.getFailedLoginAttempts());

        AddressDTO addressDTO = AddressDTO.builder()
                .isDefault(false)
                .build();
        
        assertFalse(addressDTO.getIsDefault());
    }



    @Test
    void testLoginRequest_DefaultValues() {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        assertNotNull(loginRequest);
        assertEquals("testuser", loginRequest.getUsernameOrEmail());
        assertEquals("password123", loginRequest.getPassword());
        assertNull(loginRequest.getRememberMe());
        assertNull(loginRequest.getIpAddress());
        assertNull(loginRequest.getUserAgent());
    }

    @Test
    void testLoginRequest_JsonSerialization() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .rememberMe(true)
                .build();

        String json = objectMapper.writeValueAsString(loginRequest);
        assertNotNull(json);
        assertTrue(json.contains("testuser"));
        assertTrue(json.contains("password123"));
        assertTrue(json.contains("true"));

        LoginRequest deserialized = objectMapper.readValue(json, LoginRequest.class);
        assertEquals(loginRequest.getUsernameOrEmail(), deserialized.getUsernameOrEmail());
        assertEquals(loginRequest.getPassword(), deserialized.getPassword());
        assertEquals(loginRequest.getRememberMe(), deserialized.getRememberMe());
    }

    @Test
    void testLoginRequest_EqualsAndHashCode() {
        LoginRequest request1 = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        LoginRequest request2 = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        LoginRequest request3 = LoginRequest.builder()
                .usernameOrEmail("different")
                .password("password123")
                .build();

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    // ========== AUTH RESPONSE TESTS ==========

    @Test
    void testAuthResponse_SuccessResponse() {
        UserDTO userDTO = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .email("test@example.com")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .accessToken("access-token-123")
                .refreshToken("refresh-token-456")
                .user(userDTO)
                .build();

        assertNotNull(authResponse);
        assertTrue(authResponse.getSuccess());
        assertEquals("Login successful", authResponse.getMessage());
        assertEquals("access-token-123", authResponse.getAccessToken());
        assertEquals("refresh-token-456", authResponse.getRefreshToken());
        assertNotNull(authResponse.getUser());
        assertEquals("testuser", authResponse.getUser().getUsername());
    }

    @Test
    void testAuthResponse_ErrorResponse() {
        AuthResponse authResponse = AuthResponse.builder()
                .success(false)
                .message("Invalid credentials")
                .errorCode("AUTH_001")
                .errorDetails("Username or password is incorrect")
                .build();

        assertNotNull(authResponse);
        assertFalse(authResponse.getSuccess());
        assertEquals("Invalid credentials", authResponse.getMessage());
        assertEquals("AUTH_001", authResponse.getErrorCode());
        assertEquals("Username or password is incorrect", authResponse.getErrorDetails());
        assertNull(authResponse.getAccessToken());
        assertNull(authResponse.getRefreshToken());
        assertNull(authResponse.getUser());
    }

    @Test
    void testAuthResponse_JsonSerialization() throws Exception {
        AuthResponse authResponse = AuthResponse.builder()
                .success(true)
                .message("Success")
                .accessToken("token123")
                .build();

        String json = objectMapper.writeValueAsString(authResponse);
        assertNotNull(json);
        assertTrue(json.contains("true"));
        assertTrue(json.contains("Success"));
        assertTrue(json.contains("token123"));

        AuthResponse deserialized = objectMapper.readValue(json, AuthResponse.class);
        assertEquals(authResponse.getSuccess(), deserialized.getSuccess());
        assertEquals(authResponse.getMessage(), deserialized.getMessage());
        assertEquals(authResponse.getAccessToken(), deserialized.getAccessToken());
    }

    @Test
    void testUserRegistrationRequest_JsonSerialization() throws Exception {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .confirmPassword("password123")
                .fullName("Test User")
                .build();

        String json = objectMapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("testuser"));
        assertTrue(json.contains("test@example.com"));

        UserRegistrationRequest deserialized = objectMapper.readValue(json, UserRegistrationRequest.class);
        assertEquals(request.getUsername(), deserialized.getUsername());
        assertEquals(request.getEmail(), deserialized.getEmail());
        assertEquals(request.getFullName(), deserialized.getFullName());
    }

    // ========== ADMIN REGISTRATION REQUEST TESTS ==========


    @Test
    void testAdminRegistrationRequest_DefaultRole() {
        AdminRegistrationRequest request = AdminRegistrationRequest.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("AdminPass123!")
                .confirmPassword("AdminPass123!")
                .fullName("Admin User")
                .build();

        assertNotNull(request);
        // Should default to ADMIN role if not specified
        assertEquals(UserRole.ADMIN, request.getUserRole());
    }

    @Test
    void testAdminRegistrationRequest_JsonSerialization() throws Exception {
        AdminRegistrationRequest request = AdminRegistrationRequest.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("AdminPass123!")
                .confirmPassword("AdminPass123!")
                .fullName("Admin User")
                .employeeId("EMP001")
                .userRole(UserRole.SUPER_ADMIN)
                .build();

        String json = objectMapper.writeValueAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("adminuser"));
        assertTrue(json.contains("EMP001"));
        assertTrue(json.contains("SUPER_ADMIN"));

        AdminRegistrationRequest deserialized = objectMapper.readValue(json, AdminRegistrationRequest.class);
        assertEquals(request.getUsername(), deserialized.getUsername());
        assertEquals(request.getEmployeeId(), deserialized.getEmployeeId());
        assertEquals(request.getUserRole(), deserialized.getUserRole());
    }

    // ========== INNER DTO TESTS ==========


    // ========== DTO VALIDATION TESTS ==========

    

    @Test
    void testDTO_ToString() {
        // Test toString methods don't expose sensitive data
        UserDTO userDTO = UserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        String toString = userDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        // Should not contain password even if it was set
        assertFalse(toString.toLowerCase().contains("password"));

        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("secret123")
                .build();

        String loginToString = loginRequest.toString();
        assertNotNull(loginToString);
        // Should not expose password in toString
        assertFalse(loginToString.contains("secret123"));
    }
}

