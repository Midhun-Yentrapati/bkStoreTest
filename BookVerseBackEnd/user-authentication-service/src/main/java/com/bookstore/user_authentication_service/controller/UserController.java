package com.bookstore.user_authentication_service.controller;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "User Management", description = "User Profile and Management API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    // ==================== USER PROFILE MANAGEMENT ====================
    
    @Operation(
            summary = "Get User Profile",
            description = "Get the authenticated user's profile information",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            String userId = getCurrentUserId();
            log.info("Getting profile for user: {}", userId);
            
            Optional<UserDTO> userDTO = userService.getUserById(userId);
            if (userDTO.isPresent()) {
                return ResponseEntity.ok(userDTO.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }
        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user profile"));
        }
    }
    
    @Operation(
            summary = "Update User Profile",
            description = "Update the authenticated user's profile information",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserDTO userDTO) {
        try {
            String userId = getCurrentUserId();
            log.info("Updating profile for user: {}", userId);
            
            // Clear sensitive fields that shouldn't be updated via this endpoint
            userDTO.setPassword(null);
            userDTO.setUsername(null);
            userDTO.setEmail(null);
            userDTO.setUserRole(null);
            userDTO.setUserType(null);
            userDTO.setAccountStatus(null);
            userDTO.setFailedLoginAttempts(null);
            userDTO.setAccountLockedUntil(null);
            
            UserDTO updatedUser = userService.updateUserProfile(userId, userDTO);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile updated successfully",
                    "user", updatedUser.excludePassword()
            ));
        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ==================== ADDRESS MANAGEMENT ====================
    
    @Operation(
            summary = "Get User Addresses",
            description = "Get all addresses for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/addresses")
    public ResponseEntity<?> getUserAddresses() {
        try {
            String userId = getCurrentUserId();
            log.info("Getting addresses for user: {}", userId);
            
            List<AddressDTO> addresses = userService.getUserAddresses(userId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            log.error("Error getting user addresses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve addresses"));
        }
    }
    
    @Operation(
            summary = "Add User Address",
            description = "Add a new address for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/addresses")
    public ResponseEntity<?> addUserAddress(@Valid @RequestBody AddressDTO addressDTO) {
        try {
            String userId = getCurrentUserId();
            log.info("Adding address for user: {}", userId);
            
            AddressDTO savedAddress = userService.addUserAddress(userId, addressDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Address added successfully",
                    "address", savedAddress
            ));
        } catch (Exception e) {
            log.error("Error adding user address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Update User Address",
            description = "Update an existing address for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<?> updateUserAddress(
            @PathVariable String addressId,
            @Valid @RequestBody AddressDTO addressDTO) {
        try {
            String userId = getCurrentUserId();
            log.info("Updating address {} for user: {}", addressId, userId);
            
            AddressDTO updatedAddress = userService.updateUserAddress(userId, addressId, addressDTO);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Address updated successfully",
                    "address", updatedAddress
            ));
        } catch (Exception e) {
            log.error("Error updating user address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Delete User Address",
            description = "Delete an address for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<?> deleteUserAddress(@PathVariable String addressId) {
        try {
            String userId = getCurrentUserId();
            log.info("Deleting address {} for user: {}", addressId, userId);
            
            userService.deleteUserAddress(userId, addressId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Address deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting user address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Set Default Address",
            description = "Set an address as default for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable String addressId) {
        try {
            String userId = getCurrentUserId();
            log.info("Setting default address {} for user: {}", addressId, userId);
            
            AddressDTO defaultAddress = userService.setDefaultAddress(userId, addressId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Default address set successfully",
                    "address", defaultAddress
            ));
        } catch (Exception e) {
            log.error("Error setting default address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Get Default Address",
            description = "Get the default address for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/addresses/default")
    public ResponseEntity<?> getDefaultAddress() {
        try {
            String userId = getCurrentUserId();
            log.info("Getting default address for user: {}", userId);
            
            Optional<AddressDTO> defaultAddress = userService.getDefaultAddress(userId);
            if (defaultAddress.isPresent()) {
                return ResponseEntity.ok(defaultAddress.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No default address found"));
            }
        } catch (Exception e) {
            log.error("Error getting default address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve default address"));
        }
    }
    
    // ==================== ADMIN USER MANAGEMENT ====================
    
    @Operation(
            summary = "Get All Users (Admin)",
            description = "Get all users with pagination (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            log.info("Admin getting all users - page: {}, size: {}", page, size);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserDTO> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve users"));
        }
    }
    
    @Operation(
            summary = "Get All Customers (Admin)",
            description = "Get all customer users with pagination (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/admin/customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            // Debug authentication context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("[CONTROLLER DEBUG] Authentication: {}", auth);
            log.info("[CONTROLLER DEBUG] Principal: {}", auth != null ? auth.getPrincipal() : "null");
            log.info("[CONTROLLER DEBUG] Authorities: {}", auth != null ? auth.getAuthorities() : "null");
            log.info("[CONTROLLER DEBUG] Is Authenticated: {}", auth != null ? auth.isAuthenticated() : "false");
            
            log.info("Admin getting all customers - page: {}, size: {}", page, size);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserDTO> customers = userService.getAllCustomers(pageable);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error getting all customers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve customers"));
        }
    }
    
    @Operation(
            summary = "Get All Admins (Admin)",
            description = "Get all admin users with pagination (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    @GetMapping("/admin/admins")
    public ResponseEntity<?> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            log.info("Admin getting all admin users - page: {}, size: {}", page, size);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserDTO> admins = userService.getAllAdmins(pageable);
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            log.error("Error getting all admin users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve admin users"));
        }
    }
    
    @Operation(
            summary = "Search Users (Admin)",
            description = "Search users with filters and pagination (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/admin/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) UserRole userRole,
            @RequestParam(required = false) AccountStatus accountStatus,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean isEmailVerified,
            @RequestParam(required = false) Boolean isMobileVerified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            log.info("Admin searching users with term: {}, role: {}, status: {}", 
                    searchTerm, userRole, accountStatus);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<UserDTO> users;
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                users = userService.searchUsers(searchTerm, pageable);
            } else {
                users = userService.getUsersWithFilters(userRole, accountStatus, department, 
                        isEmailVerified, isMobileVerified, pageable);
            }
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search users"));
        }
    }
    
    @Operation(
            summary = "Get User by ID (Admin)",
            description = "Get a specific user by ID (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/admin/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            log.info("Admin getting user by ID: {}", userId);
            
            Optional<UserDTO> user = userService.getUserById(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
        } catch (Exception e) {
            log.error("Error getting user by ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user"));
        }
    }
    
    // ==================== ADMIN USER ACCOUNT MANAGEMENT ====================
    
    @Operation(
            summary = "Activate User Account (Admin)",
            description = "Activate a user account (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping("/admin/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable String userId) {
        try {
            log.info("Admin activating user: {}", userId);
            
            userService.activateAccount(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account activated successfully"
            ));
        } catch (Exception e) {
            log.error("Error activating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Deactivate User Account (Admin)",
            description = "Deactivate a user account (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping("/admin/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable String userId) {
        try {
            log.info("Admin deactivating user: {}", userId);
            
            userService.deactivateAccount(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account deactivated successfully"
            ));
        } catch (Exception e) {
            log.error("Error deactivating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Lock User Account (Admin)",
            description = "Lock a user account (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping("/admin/{userId}/lock")
    public ResponseEntity<?> lockUser(@PathVariable String userId) {
        try {
            log.info("Admin locking user: {}", userId);
            
            userService.lockAccount(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account locked successfully"
            ));
        } catch (Exception e) {
            log.error("Error locking user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Unlock User Account (Admin)",
            description = "Unlock a user account (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping("/admin/{userId}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable String userId) {
        try {
            log.info("Admin unlocking user: {}", userId);
            
            userService.unlockAccount(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account unlocked successfully"
            ));
        } catch (Exception e) {
            log.error("Error unlocking user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Toggle User Account Status (Admin)",
            description = "Enable or disable a user account (Admin only). SUPER_ADMIN can disable ADMIN accounts, but SUPER_ADMIN accounts cannot be disabled.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    @PutMapping("/admin/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String userId) {
        try {
            log.info("Admin toggling user status for user: {}", userId);
            
            // Check if user exists first
            Optional<UserDTO> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
            
            UserDTO user = userOpt.get();
            
            // Get current admin's role from SecurityContext
            String currentAdminId = getCurrentUserId();
            Optional<UserDTO> currentAdminOpt = userService.getUserById(currentAdminId);
            
            if (currentAdminOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Current admin user not found"));
            }
            
            UserDTO currentAdmin = currentAdminOpt.get();
            
            // Permission logic:
            // 1. SUPER_ADMIN accounts cannot be disabled by anyone
            // 2. ADMIN accounts can only be disabled by SUPER_ADMIN
            // 3. Other roles can be disabled by both SUPER_ADMIN and ADMIN
            
            if (user.getUserRole() == UserRole.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Cannot disable SUPER_ADMIN accounts"));
            }
            
            if (user.getUserRole() == UserRole.ADMIN && currentAdmin.getUserRole() != UserRole.SUPER_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only SUPER_ADMIN can disable ADMIN accounts"));
            }
            
            // Toggle the account status
            boolean currentlyActive = user.getAccountStatus() == AccountStatus.ACTIVE;
            if (currentlyActive) {
                userService.deactivateAccount(userId);
            } else {
                userService.activateAccount(userId);
            }
            
            String newStatus = currentlyActive ? "deactivated" : "activated";
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account " + newStatus + " successfully",
                    "isActive", !currentlyActive
            ));
        } catch (Exception e) {
            log.error("Error toggling user status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to toggle user status: " + e.getMessage()));
        }
    }
    
    // ==================== USER DELETION ENDPOINTS ====================
    
    @Operation(
            summary = "Delete User Account (Admin)",
            description = "Permanently delete a user account (Admin only). This action cannot be undone.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            log.info("Admin permanently deleting user: {}", userId);
            
            // Check if user exists first
            Optional<UserDTO> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
            
            // Perform hard delete
            userService.deleteUser(userId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account deleted permanently"
            ));
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete user: " + e.getMessage()));
        }
    }
    
    @Operation(
            summary = "Soft Delete User Account (Admin)",
            description = "Soft delete a user account (Admin only). User data is preserved but account is marked as deleted.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    @PutMapping("/admin/{userId}/soft-delete")
    public ResponseEntity<?> softDeleteUser(@PathVariable String userId) {
        try {
            log.info("Admin soft deleting user: {}", userId);
            
            // Check if user exists first
            Optional<UserDTO> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
            
            // Perform soft delete
            userService.softDeleteUser(userId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account soft deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error soft deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to soft delete user: " + e.getMessage()));
        }
    }
    
    // ==================== USER STATISTICS ====================
    
    @Operation(
            summary = "Get User Statistics (Admin)",
            description = "Get user count statistics (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/admin/stats/count")
    public ResponseEntity<?> getUserStatistics() {
        try {
            log.info("Admin getting user statistics");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getTotalUserCount());
            stats.put("customerCount", userService.getCustomerCount());
            stats.put("adminCount", userService.getAdminCount());
            stats.put("roleDistribution", userService.getUserRoleDistribution());
            stats.put("statusDistribution", userService.getUserStatusDistribution());
            stats.put("departmentDistribution", userService.getUserCountByDepartment());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting user statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user statistics"));
        }
    }
    
    @Operation(
            summary = "Get Inactive Users (Admin)",
            description = "Get list of inactive users (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/admin/stats/inactive")
    public ResponseEntity<?> getInactiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Admin getting inactive users");
            
            Pageable pageable = PageRequest.of(page, size);
            Page<UserDTO> inactiveUsers = userService.getUsersWithFilters(
                    null, AccountStatus.INACTIVE, null, null, null, pageable);
            
            return ResponseEntity.ok(inactiveUsers);
        } catch (Exception e) {
            log.error("Error getting inactive users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve inactive users"));
        }
    }
    
    // ==================== PASSWORD MANAGEMENT ====================
    
    @Operation(
            summary = "Change Password",
            description = "Change the authenticated user's password",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            String userId = getCurrentUserId();
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Current password and new password are required"));
            }
            
            log.info("Changing password for user: {}", userId);
            
            userService.changePassword(userId, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password changed successfully"
            ));
        } catch (Exception e) {
            log.error("Error changing password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ==================== VALIDATION ENDPOINTS ====================
    
    @Operation(
            summary = "Check Username Availability",
            description = "Check if a username is available for registration"
    )
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        try {
            boolean exists = userService.existsByUsername(username);
            return ResponseEntity.ok(Map.of(
                    "available", !exists,
                    "username", username
            ));
        } catch (Exception e) {
            log.error("Error checking username availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check username availability"));
        }
    }
    
    @Operation(
            summary = "Check Email Availability",
            description = "Check if an email is available for registration"
    )
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            return ResponseEntity.ok(Map.of(
                    "available", !exists,
                    "email", email
            ));
        } catch (Exception e) {
            log.error("Error checking email availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check email availability"));
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        throw new RuntimeException("No authenticated user found");
    }
}