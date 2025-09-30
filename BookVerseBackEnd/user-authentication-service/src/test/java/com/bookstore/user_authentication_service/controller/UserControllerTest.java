package com.bookstore.user_authentication_service.controller;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.exception.*;
import com.bookstore.user_authentication_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for UserController
 * Tests all REST endpoints with various scenarios including security
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO testUserDTO;
    private AddressDTO testAddressDTO;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
        
        testUserDTO = UserDTO.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .mobileNumber("9876543210")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .userType(UserType.CUSTOMER)
                .userRole(UserRole.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .isMobileVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        testAddressDTO = AddressDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Address")
                .phone("9876543210")
                .addressLine1("123 Test Street")
                .city("Test City")
                .state("Test State")
                .country("Test Country")
                .pincode("123456")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .build();
    }

    // ==================== USER PROFILE TESTS ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetProfile_Success() throws Exception {
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUserDTO));

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer mock-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        verify(userService).getUserById(testUserId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetProfile_UserNotFound() throws Exception {
        when(userService.getUserById(testUserId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).getUserById(testUserId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateProfile_Success() throws Exception {
        UserDTO updatedUser = testUserDTO.toBuilder()
                .fullName("Updated Name")
                .bio("Updated bio")
                .build();

        when(userService.updateUserProfile(eq(testUserId), any(UserDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.bio").value("Updated bio"));

        verify(userService).updateUserProfile(eq(testUserId), any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateProfile_ValidationError() throws Exception {
        UserDTO invalidUser = testUserDTO.toBuilder()
                .fullName("") // Invalid empty name
                .email("invalid-email") // Invalid email format
                .build();

        mockMvc.perform(put("/api/users/profile")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testChangePassword_Success() throws Exception {
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "oldPassword123");
        passwordData.put("newPassword", "newPassword123");

        doNothing().when(userService).changePassword(testUserId, "oldPassword123", "newPassword123");

        mockMvc.perform(post("/api/users/change-password")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService).changePassword(testUserId, "oldPassword123", "newPassword123");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testChangePassword_InvalidCurrentPassword() throws Exception {
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "wrongPassword");
        passwordData.put("newPassword", "newPassword123");

        doThrow(new ValidationException("Current password is incorrect"))
                .when(userService).changePassword(testUserId, "wrongPassword", "newPassword123");

        mockMvc.perform(post("/api/users/change-password")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Current password is incorrect"));

        verify(userService).changePassword(testUserId, "wrongPassword", "newPassword123");
    }

    // ==================== ADDRESS MANAGEMENT TESTS ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAddresses_Success() throws Exception {
        List<AddressDTO> addresses = Arrays.asList(testAddressDTO);
        when(userService.getUserAddresses(testUserId)).thenReturn(addresses);

        mockMvc.perform(get("/api/users/addresses")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Address"))
                .andExpect(jsonPath("$[0].city").value("Test City"));

        verify(userService).getUserAddresses(testUserId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testAddAddress_Success() throws Exception {
        when(userService.addUserAddress(eq(testUserId), any(AddressDTO.class))).thenReturn(testAddressDTO);

        mockMvc.perform(post("/api/users/addresses")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Address"))
                .andExpect(jsonPath("$.city").value("Test City"));

        verify(userService).addUserAddress(eq(testUserId), any(AddressDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testAddAddress_ValidationError() throws Exception {
        AddressDTO invalidAddress = testAddressDTO.toBuilder()
                .name("") // Invalid empty name
                .phone("invalid-phone") // Invalid phone format
                .pincode("abc") // Invalid pincode
                .build();

        mockMvc.perform(post("/api/users/addresses")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAddress)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateAddress_Success() throws Exception {
        String addressId = testAddressDTO.getId();
        AddressDTO updatedAddress = testAddressDTO.toBuilder()
                .name("Updated Address")
                .build();

        when(userService.updateUserAddress(testUserId, addressId, updatedAddress)).thenReturn(updatedAddress);

        mockMvc.perform(put("/api/users/addresses/{addressId}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAddress)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Address"));

        verify(userService).updateUserAddress(testUserId, addressId, updatedAddress);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteAddress_Success() throws Exception {
        String addressId = testAddressDTO.getId();
        doNothing().when(userService).deleteUserAddress(testUserId, addressId);

        mockMvc.perform(delete("/api/users/addresses/{addressId}", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Address deleted successfully"));

        verify(userService).deleteUserAddress(testUserId, addressId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testSetDefaultAddress_Success() throws Exception {
        String addressId = testAddressDTO.getId();
        AddressDTO defaultAddress = testAddressDTO.toBuilder().isDefault(true).build();
        
        when(userService.setDefaultAddress(testUserId, addressId)).thenReturn(defaultAddress);

        mockMvc.perform(put("/api/users/addresses/{addressId}/default", addressId)
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(true));

        verify(userService).setDefaultAddress(testUserId, addressId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetDefaultAddress_Success() throws Exception {
        when(userService.getDefaultAddress(testUserId)).thenReturn(Optional.of(testAddressDTO));

        mockMvc.perform(get("/api/users/addresses/default")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Address"));

        verify(userService).getDefaultAddress(testUserId);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetDefaultAddress_NotFound() throws Exception {
        when(userService.getDefaultAddress(testUserId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/addresses/default")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No default address found"));

        verify(userService).getDefaultAddress(testUserId);
    }

    // ==================== ADMIN USER MANAGEMENT TESTS ====================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllCustomers_Success() throws Exception {
        Page<UserDTO> customerPage = new PageImpl<>(Arrays.asList(testUserDTO));
        when(userService.getAllCustomers(any(PageRequest.class))).thenReturn(customerPage);

        mockMvc.perform(get("/api/users/admin/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("testuser"));

        verify(userService).getAllCustomers(any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllAdmins_Success() throws Exception {
        UserDTO adminUser = testUserDTO.toBuilder()
                .userRole(UserRole.ADMIN)
                .userType(UserType.ADMIN)
                .build();
        Page<UserDTO> adminPage = new PageImpl<>(Arrays.asList(adminUser));
        
        when(userService.getAllAdmins(any(PageRequest.class))).thenReturn(adminPage);

        mockMvc.perform(get("/api/users/admin/admins")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userRole").value("ADMIN"));

        verify(userService).getAllAdmins(any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchUsers_Success() throws Exception {
        Page<UserDTO> searchResults = new PageImpl<>(Arrays.asList(testUserDTO));
        when(userService.searchUsers(eq("test"), any(PageRequest.class))).thenReturn(searchResults);

        mockMvc.perform(get("/api/users/admin/search")
                        .param("query", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("testuser"));

        verify(userService).searchUsers(eq("test"), any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "super-admin-id", roles = {"SUPER_ADMIN"})
    void testToggleUserStatus_Success() throws Exception {
        // Create a SUPER_ADMIN user as the current admin to have permission to toggle accounts
        String currentAdminId = "super-admin-id"; // This matches the @WithMockUser username
        UserDTO currentAdmin = UserDTO.builder()
                .id(currentAdminId)
                .username("superadmin")
                .email("admin@example.com")
                .fullName("Super Admin User")
                .userType(UserType.ADMIN)
                .userRole(UserRole.SUPER_ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        
        // Mock service calls
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUserDTO));
        when(userService.getUserById(currentAdminId)).thenReturn(Optional.of(currentAdmin));
        
        // Since testUserDTO has ACTIVE status, the controller will call deactivateAccount
        doNothing().when(userService).deactivateAccount(testUserId);

        mockMvc.perform(put("/api/users/admin/{userId}/toggle-status", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User account deactivated successfully"))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(userService).getUserById(testUserId);
        verify(userService).getUserById(currentAdminId);
        verify(userService).deactivateAccount(testUserId);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser_Success() throws Exception {
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUserDTO));
        doNothing().when(userService).deleteUser(testUserId);

        mockMvc.perform(delete("/api/users/admin/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User account deleted permanently"));

        verify(userService).getUserById(testUserId);
        verify(userService).deleteUser(testUserId);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser_NotFound() throws Exception {
        when(userService.getUserById(testUserId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/admin/{userId}", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).getUserById(testUserId);
        verify(userService, never()).deleteUser(testUserId);
    }

    // ==================== SECURITY TESTS ====================

    @Test
    void testGetProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAllCustomers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users/admin/customers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testDeleteUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/admin/{userId}", testUserId))
                .andExpect(status().isForbidden());
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateProfile_ServiceException() throws Exception {
        when(userService.updateUserProfile(eq(testUserId), any(UserDTO.class)))
                .thenThrow(new ServiceException("Database error"));

        mockMvc.perform(put("/api/users/profile")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Database error"));

        verify(userService).updateUserProfile(eq(testUserId), any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testAddAddress_ResourceNotFoundException() throws Exception {
        when(userService.addUserAddress(eq(testUserId), any(AddressDTO.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/api/users/addresses")
                        .with(jwt().jwt(jwt -> jwt.subject(testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAddressDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        verify(userService).addUserAddress(eq(testUserId), any(AddressDTO.class));
    }
}
