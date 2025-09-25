package com.bookstore.user_authentication_service.controller;

import com.bookstore.user_authentication_service.dto.UserDTO;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for DebugController
 * Tests debugging and diagnostic endpoints
 */
@WebMvcTest(DebugController.class)
class DebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDTO testUserDTO;
    private List<UserDTO> testUsers;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .username("debuguser")
                .email("debug@example.com")
                .fullName("Debug User")
                .userType(UserType.CUSTOMER)
                .userRole(UserRole.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .isMobileVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        UserDTO adminUser = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .username("debugadmin")
                .email("admin@example.com")
                .fullName("Debug Admin")
                .userType(UserType.ADMIN)
                .userRole(UserRole.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .isMobileVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        testUsers = Arrays.asList(testUserDTO, adminUser);
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(testUsers);
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/debug/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content[0].username").value("debuguser"))
                .andExpect(jsonPath("$.content[1].username").value("debugadmin"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(userService).getAllUsers(any(PageRequest.class));
    }

    @Test
    void testGetAllUsers_EmptyResult() throws Exception {
        Page<UserDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/debug/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(userService).getAllUsers(any(PageRequest.class));
    }

    @Test
    void testGetAllUsers_WithCustomPagination() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(testUsers.subList(0, 1), PageRequest.of(0, 1), 2);
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/debug/users")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(userService).getAllUsers(PageRequest.of(0, 1));
    }

    @Test
    void testGetAllUsers_DefaultPagination() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(testUsers);
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/debug/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // Verify default pagination parameters (page=0, size=20)
        verify(userService).getAllUsers(PageRequest.of(0, 20));
    }

    @Test
    void testGetAllUsers_InvalidPageParameters() throws Exception {
        mockMvc.perform(get("/api/debug/users")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllUsers_LargePageSize() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(testUsers);
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/debug/users")
                        .param("page", "0")
                        .param("size", "1000"))
                .andExpect(status().isOk());

        // Should limit to maximum allowed size (typically 100)
        verify(userService).getAllUsers(any(PageRequest.class));
    }

    @Test
    void testGetAllUsers_ServiceException() throws Exception {
        when(userService.getAllUsers(any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/debug/users"))
                .andExpect(status().isInternalServerError());

        verify(userService).getAllUsers(any(PageRequest.class));
    }

    @Test
    void testGetAllUsers_ContentType() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(testUsers);
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/debug/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(userService).getAllUsers(any(PageRequest.class));
    }

    @Test
    void testGetAllUsers_ResponseStructure() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(testUsers, PageRequest.of(0, 10), 2);
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/debug/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.last").exists())
                .andExpect(jsonPath("$.first").exists())
                .andExpect(jsonPath("$.numberOfElements").exists())
                .andExpect(jsonPath("$.size").exists())
                .andExpect(jsonPath("$.number").exists());

        verify(userService).getAllUsers(any(PageRequest.class));
    }

    @Test
    void testGetAllUsers_UserDataIntegrity() throws Exception {
        Page<UserDTO> userPage = new PageImpl<>(Arrays.asList(testUserDTO));
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/debug/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].username").value("debuguser"))
                .andExpect(jsonPath("$.content[0].email").value("debug@example.com"))
                .andExpect(jsonPath("$.content[0].fullName").value("Debug User"))
                .andExpect(jsonPath("$.content[0].userType").value("CUSTOMER"))
                .andExpect(jsonPath("$.content[0].userRole").value("CUSTOMER"))
                .andExpect(jsonPath("$.content[0].accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].isEmailVerified").value(true))
                .andExpect(jsonPath("$.content[0].isMobileVerified").value(false))
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                // Ensure sensitive data is not exposed
                .andExpect(jsonPath("$.content[0].password").doesNotExist());

        verify(userService).getAllUsers(any(PageRequest.class));
    }
}
