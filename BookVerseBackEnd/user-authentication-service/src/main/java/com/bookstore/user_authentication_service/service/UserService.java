package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    
    // User CRUD Operations
    UserDTO createUser(UserRegistrationRequest request);
    UserDTO createAdminUser(AdminRegistrationRequest request);
    Optional<UserDTO> getUserById(String userId);
    Optional<UserDTO> getUserByUsername(String username);
    Optional<UserDTO> getUserByEmail(String email);
    UserDTO updateUser(String userId, UserDTO userDTO);
    UserDTO updateUserProfile(String userId, UserDTO userDTO);
    void deleteUser(String userId);
    void softDeleteUser(String userId);
    
    // User Search and Filtering
    Page<UserDTO> getAllUsers(Pageable pageable);
    Page<UserDTO> getAllCustomers(Pageable pageable);
    Page<UserDTO> getAllAdmins(Pageable pageable);
    Page<UserDTO> getUsersByRole(UserRole userRole, Pageable pageable);
    Page<UserDTO> searchUsers(String searchTerm, Pageable pageable);
    Page<UserDTO> searchCustomers(String searchTerm, Pageable pageable);
    Page<UserDTO> searchAdmins(String searchTerm, Pageable pageable);
    Page<UserDTO> getUsersWithFilters(UserRole userRole, AccountStatus accountStatus, 
                                     String department, Boolean isEmailVerified, 
                                     Boolean isMobileVerified, Pageable pageable);
    
    // Account Management
    void activateAccount(String userId);
    void deactivateAccount(String userId);
    void suspendAccount(String userId);
    void lockAccount(String userId);
    void unlockAccount(String userId);
    void verifyEmail(String userId);
    void verifyMobile(String userId);
    
    // Password Management
    void changePassword(String userId, String currentPassword, String newPassword);
    void resetPassword(String userId, String newPassword);
    void initiatePasswordReset(String email);
    boolean validatePasswordResetToken(String token);
    
    // User Validation
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    boolean isAccountLocked(String userId);
    boolean isAccountActive(String userId);
    
    // Address Management
    List<AddressDTO> getUserAddresses(String userId);
    AddressDTO addUserAddress(String userId, AddressDTO addressDTO);
    AddressDTO updateUserAddress(String userId, String addressId, AddressDTO addressDTO);
    void deleteUserAddress(String userId, String addressId);
    AddressDTO setDefaultAddress(String userId, String addressId);
    Optional<AddressDTO> getDefaultAddress(String userId);
    
    // User Statistics and Analytics
    long getTotalUserCount();
    long getCustomerCount();
    long getAdminCount();
    long getUserCountByRole(UserRole userRole);
    long getUserCountByStatus(AccountStatus accountStatus);
    Map<UserRole, Long> getUserRoleDistribution();
    Map<AccountStatus, Long> getUserStatusDistribution();
    Map<String, Long> getUserCountByDepartment();
    List<UserDTO> getRecentlyRegisteredUsers(int days, Pageable pageable);
    List<UserDTO> getRecentlyActiveUsers(int days, Pageable pageable);
    
    // Department and Manager Management
    List<UserDTO> getUsersByDepartment(String department, Pageable pageable);
    List<UserDTO> getUsersByManager(String managerId);
    List<String> getAllDepartments();
    
    // Bulk Operations
    List<UserDTO> createUsersInBulk(List<UserRegistrationRequest> requests);
    void updateUsersInBulk(List<UserDTO> users);
    void deleteUsersInBulk(List<String> userIds);
    
    // User Profile Completeness
    double calculateProfileCompleteness(String userId);
    List<String> getIncompleteProfileFields(String userId);
    
    // Security and Audit
    void incrementFailedLoginAttempts(String userId);
    void resetFailedLoginAttempts(String userId);
    void updateLastLogin(String userId, String ipAddress);
    List<UserDTO> getLockedUsers(Pageable pageable);
    List<UserDTO> getUnverifiedUsers(Pageable pageable);
}
