package com.bookstore.user_authentication_service.service.impl;

import com.bookstore.user_authentication_service.dto.*;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.exception.*;
import com.bookstore.user_authentication_service.exception.AddressNotFoundException;
import com.bookstore.user_authentication_service.repository.*;
import com.bookstore.user_authentication_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDTO createUser(UserRegistrationRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());
        
        // Validate uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ValidationException.usernameAlreadyExists(request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ValidationException.emailAlreadyExists(request.getEmail());
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .mobileNumber(request.getMobileNumber())
                .dateOfBirth(request.getDateOfBirth())
                .bio(request.getBio())
                .userRole(request.getUserRole() != null ? request.getUserRole() : UserRole.CUSTOMER)
                .userType(request.getUserType() != null ? request.getUserType() : UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        
        user = userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());
        
        return convertToDTO(user);
    }
    
    @Override
    public UserDTO createAdminUser(AdminRegistrationRequest request) {
        log.info("Creating new admin user with username: {}", request.getUsername());
        
        // Validate uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ValidationException.usernameAlreadyExists(request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ValidationException.emailAlreadyExists(request.getEmail());
        }
        if (request.getEmployeeId() != null && userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw ValidationException.employeeIdAlreadyExists(request.getEmployeeId());
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .mobileNumber(request.getMobileNumber())
                .dateOfBirth(request.getDateOfBirth())
                .userRole(request.getUserRole())
                .userType(UserType.ADMIN)
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .managerId(request.getManagerId())
                .permissions(request.getPermissions())
                .hireDate(request.getHireDate())
                .salary(request.getSalary())
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        
        user = userRepository.save(user);
        log.info("Admin user created successfully with ID: {}", user.getId());
        
        return convertToDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ValidationException("User ID cannot be null or empty");
        }
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
    
    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));
        
        // Update fields
        if (userDTO.getFullName() != null) user.setFullName(userDTO.getFullName());
        if (userDTO.getMobileNumber() != null) user.setMobileNumber(userDTO.getMobileNumber());
        if (userDTO.getDateOfBirth() != null) user.setDateOfBirth(userDTO.getDateOfBirth());
        if (userDTO.getBio() != null) user.setBio(userDTO.getBio());
        if (userDTO.getProfilePictureUrl() != null) user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        
        user = userRepository.save(user);
        return convertToDTO(user);
    }
    
    @Override
    public UserDTO updateUserProfile(String userId, UserDTO userDTO) {
        return updateUser(userId, userDTO);
    }
    
    @Override
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
        log.info("User deleted permanently: {}", userId);
    }
    
    @Override
    public void softDeleteUser(String userId) {
        userRepository.softDeleteUser(userId, LocalDateTime.now());
        log.info("User soft deleted: {}", userId);
    }
    
    // Helper method to convert User entity to DTO
    private UserDTO convertToDTO(User user) {
        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .dateOfBirth(user.getDateOfBirth())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .userRole(user.getUserRole())
                .userType(user.getUserType())
                .employeeId(user.getEmployeeId())
                .department(user.getDepartment())
                .managerId(user.getManagerId())
                .permissions(user.getPermissions())
                .hireDate(user.getHireDate())
                .salary(user.getSalary())
                .accountStatus(user.getAccountStatus())
                .isEmailVerified(user.getIsEmailVerified())
                .isMobileVerified(user.getIsMobileVerified())
                .isTwoFactorEnabled(user.getIsTwoFactorEnabled())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .accountLockedUntil(user.getAccountLockedUntil())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .passwordChangedAt(user.getPasswordChangedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .profileCompleteness(user.getProfileCompleteness())
                .isAccountNonLocked(user.isAccountNonLocked())
                .isAccountActive(user.isAccountActive())
                .isCustomer(user.isCustomer())
                .isAdmin(user.isAdmin())
                .build();
        
        // Convert addresses if present
        if (user.getAddresses() != null) {
            dto.setAddresses(user.getAddresses().stream()
                    .map(this::convertAddressToDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto.excludePassword();
    }
    
    private AddressDTO convertAddressToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .name(address.getName())
                .phone(address.getPhone())
                .alternatePhone(address.getAlternatePhone())
                .email(address.getEmail())
                .pincode(address.getPincode())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .addressType(address.getAddressType())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.getIsDefault())
                .isActive(address.getIsActive())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .userId(address.getUser().getId())
                .fullAddress(address.getFullAddress())
                .shortAddress(address.getShortAddress())
                .hasCoordinates(address.hasCoordinates())
                .build();
    }
    
    // Implement remaining methods with similar patterns...
    // (Additional methods would be implemented here following the same pattern)
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllCustomers(Pageable pageable) {
        return userRepository.findAllCustomers(pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllAdmins(Pageable pageable) {
        return userRepository.findAllAdmins(pageable)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.countAllUsers();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getCustomerCount() {
        return userRepository.countCustomers();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getAdminCount() {
        return userRepository.countAdmins();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return userRepository.existsByEmployeeId(employeeId);
    }
    
    // Placeholder implementations for remaining methods
    @Override public Page<UserDTO> getUsersByRole(UserRole userRole, Pageable pageable) { return Page.empty(); }
    @Override public Page<UserDTO> searchUsers(String searchTerm, Pageable pageable) { return Page.empty(); }
    @Override public Page<UserDTO> searchCustomers(String searchTerm, Pageable pageable) { return Page.empty(); }
    @Override public Page<UserDTO> searchAdmins(String searchTerm, Pageable pageable) { return Page.empty(); }
    @Override public Page<UserDTO> getUsersWithFilters(UserRole userRole, AccountStatus accountStatus, String department, Boolean isEmailVerified, Boolean isMobileVerified, Pageable pageable) { return Page.empty(); }
    @Override 
    public void activateAccount(String userId) {
        log.info("Activating account for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found with ID: " + userId));
        
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.info("Account activated successfully for user: {}", userId);
    }
    @Override 
    public void deactivateAccount(String userId) {
        log.info("Deactivating account for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found with ID: " + userId));
        
        user.setAccountStatus(AccountStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.info("Account deactivated successfully for user: {}", userId);
    }
    @Override public void suspendAccount(String userId) { }
    @Override public void lockAccount(String userId) { }
    @Override public void unlockAccount(String userId) { }
    @Override public void verifyEmail(String userId) { }
    @Override public void verifyMobile(String userId) { }
    @Override 
    public void changePassword(String userId, String currentPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);
        
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found with ID: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Invalid current password provided for user: {}", userId);
            throw new ValidationException("Current password is incorrect");
        }
        
        // Validate new password (basic validation)
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new ValidationException("New password must be at least 6 characters long");
        }
        
        // Hash and set new password
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);
        user.setPasswordChangedAt(LocalDateTime.now());
        
        // Save the user
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", userId);
    }
    @Override public void resetPassword(String userId, String newPassword) { }
    @Override public void initiatePasswordReset(String email) { }
    @Override public boolean validatePasswordResetToken(String token) { return false; }
    @Override public boolean isAccountLocked(String userId) { return false; }
    @Override public boolean isAccountActive(String userId) { return false; }
    @Override
    public List<AddressDTO> getUserAddresses(String userId) {
        log.info("Getting addresses for user: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        List<Address> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(this::convertToAddressDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public AddressDTO addUserAddress(String userId, AddressDTO addressDTO) {
        log.info("Adding address for user: {}", userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        Address address = Address.builder()
                .user(user)
                .name(addressDTO.getName())
                .phone(addressDTO.getPhone())
                .alternatePhone(addressDTO.getAlternatePhone())
                .email(addressDTO.getEmail())
                .pincode(addressDTO.getPincode())
                .addressLine1(addressDTO.getAddressLine1())
                .addressLine2(addressDTO.getAddressLine2())
                .landmark(addressDTO.getLandmark())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .country(addressDTO.getCountry())
                .addressType(addressDTO.getAddressType())
                .latitude(addressDTO.getLatitude())
                .longitude(addressDTO.getLongitude())
                .isDefault(addressDTO.getIsDefault())
                .build();
        
        // If this is set as default, unset other default addresses
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            addressRepository.unsetAllDefaultAddressesForUser(userId);
        }
        
        Address savedAddress = addressRepository.save(address);
        return convertToAddressDTO(savedAddress);
    }
    
    @Override
    public AddressDTO updateUserAddress(String userId, String addressId, AddressDTO addressDTO) {
        log.info("Updating address {} for user: {}", addressId, userId);
        
        Address address = addressRepository.findById(addressId)
                .filter(addr -> addr.getUser().getId().equals(userId))
                .orElseThrow(() -> AddressNotFoundException.addressNotFoundForUser(addressId, userId));
        
        // Update fields
        if (addressDTO.getName() != null) address.setName(addressDTO.getName());
        if (addressDTO.getPhone() != null) address.setPhone(addressDTO.getPhone());
        if (addressDTO.getAlternatePhone() != null) address.setAlternatePhone(addressDTO.getAlternatePhone());
        if (addressDTO.getEmail() != null) address.setEmail(addressDTO.getEmail());
        if (addressDTO.getPincode() != null) address.setPincode(addressDTO.getPincode());
        if (addressDTO.getAddressLine1() != null) address.setAddressLine1(addressDTO.getAddressLine1());
        if (addressDTO.getAddressLine2() != null) address.setAddressLine2(addressDTO.getAddressLine2());
        if (addressDTO.getLandmark() != null) address.setLandmark(addressDTO.getLandmark());
        if (addressDTO.getCity() != null) address.setCity(addressDTO.getCity());
        if (addressDTO.getState() != null) address.setState(addressDTO.getState());
        if (addressDTO.getCountry() != null) address.setCountry(addressDTO.getCountry());
        if (addressDTO.getAddressType() != null) address.setAddressType(addressDTO.getAddressType());
        if (addressDTO.getLatitude() != null) address.setLatitude(addressDTO.getLatitude());
        if (addressDTO.getLongitude() != null) address.setLongitude(addressDTO.getLongitude());
        
        // Handle default address change
        if (Boolean.TRUE.equals(addressDTO.getIsDefault()) && !address.getIsDefault()) {
            addressRepository.unsetAllDefaultAddressesForUser(userId);
            address.setIsDefault(true);
        } else if (Boolean.FALSE.equals(addressDTO.getIsDefault())) {
            address.setIsDefault(false);
        }
        
        Address savedAddress = addressRepository.save(address);
        return convertToAddressDTO(savedAddress);
    }
    
    @Override
    public void deleteUserAddress(String userId, String addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);
        
        Address address = addressRepository.findById(addressId)
                .filter(addr -> addr.getUser().getId().equals(userId))
                .orElseThrow(() -> AddressNotFoundException.addressNotFoundForUser(addressId, userId));
        
        addressRepository.delete(address);
    }
    
    @Override
    public AddressDTO setDefaultAddress(String userId, String addressId) {
        log.info("Setting default address {} for user: {}", addressId, userId);
        
        Address address = addressRepository.findById(addressId)
                .filter(addr -> addr.getUser().getId().equals(userId))
                .orElseThrow(() -> AddressNotFoundException.addressNotFoundForUser(addressId, userId));
        
        // Unset all other default addresses for this user
        addressRepository.unsetAllDefaultAddressesForUser(userId);
        
        // Set this address as default
        address.setIsDefault(true);
        Address savedAddress = addressRepository.save(address);
        
        return convertToAddressDTO(savedAddress);
    }
    
    @Override
    public Optional<AddressDTO> getDefaultAddress(String userId) {
        log.info("Getting default address for user: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        
        Optional<Address> defaultAddress = addressRepository.findDefaultAddressByUserId(userId);
        return defaultAddress.map(this::convertToAddressDTO);
    }
    
    private AddressDTO convertToAddressDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .name(address.getName())
                .phone(address.getPhone())
                .alternatePhone(address.getAlternatePhone())
                .email(address.getEmail())
                .pincode(address.getPincode())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .addressType(address.getAddressType())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
    @Override public long getUserCountByRole(UserRole userRole) { return 0; }
    @Override public long getUserCountByStatus(AccountStatus accountStatus) { return 0; }
    @Override public Map<UserRole, Long> getUserRoleDistribution() { return Collections.emptyMap(); }
    @Override public Map<AccountStatus, Long> getUserStatusDistribution() { return Collections.emptyMap(); }
    @Override public Map<String, Long> getUserCountByDepartment() { return Collections.emptyMap(); }
    @Override public List<UserDTO> getRecentlyRegisteredUsers(int days, Pageable pageable) { return Collections.emptyList(); }
    @Override public List<UserDTO> getRecentlyActiveUsers(int days, Pageable pageable) { return Collections.emptyList(); }
    @Override public List<UserDTO> getUsersByDepartment(String department, Pageable pageable) { return Collections.emptyList(); }
    @Override public List<UserDTO> getUsersByManager(String managerId) { return Collections.emptyList(); }
    @Override public List<String> getAllDepartments() { return Collections.emptyList(); }
    @Override public List<UserDTO> createUsersInBulk(List<UserRegistrationRequest> requests) { return Collections.emptyList(); }
    @Override public void updateUsersInBulk(List<UserDTO> users) { }
    @Override public void deleteUsersInBulk(List<String> userIds) { }
    @Override public double calculateProfileCompleteness(String userId) { return 0.0; }
    @Override public List<String> getIncompleteProfileFields(String userId) { return Collections.emptyList(); }
    @Override public void incrementFailedLoginAttempts(String userId) { }
    @Override public void resetFailedLoginAttempts(String userId) { }
    @Override 
    public void updateLastLogin(String userId, String ipAddress) {
        log.info("Updating last login for user: {} from IP: {}", userId, ipAddress);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        
        userRepository.save(user);
        log.info("Last login updated successfully for user: {}", userId);
    }
    @Override public List<UserDTO> getLockedUsers(Pageable pageable) { return Collections.emptyList(); }
    @Override public List<UserDTO> getUnverifiedUsers(Pageable pageable) { return Collections.emptyList(); }
}
