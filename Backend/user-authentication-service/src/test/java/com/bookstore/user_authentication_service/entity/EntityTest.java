package com.bookstore.user_authentication_service.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all entity classes
 * Tests entity creation, getters, setters, builders, and business logic
 */
class EntityTest {

    // ========== USER ENTITY TESTS ==========

    @Test
    void testUserEntity_Creation() {
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .mobileNumber("9876543210")
                .dateOfBirth(now.minusYears(25).toLocalDate())
                .bio("Test bio")
                .profilePictureUrl("http://example.com/profile.jpg")
                .userRole(UserRole.CUSTOMER)
                .userType(UserType.CUSTOMER)
                .employeeId("EMP001")
                .department("IT")
                .managerId("manager-123")
                .permissions(Set.of(Permission.USER_READ, Permission.USER_UPDATE))
                .hireDate(now.minusYears(2).toLocalDate())
                .salary(new BigDecimal("50000.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .isMobileVerified(false)
                .isTwoFactorEnabled(true)
                .failedLoginAttempts(0)
                .accountLockedUntil(null)
                .lastLoginAt(now.minusHours(1))
                .lastLoginIp("192.168.1.100")
                .passwordChangedAt(now.minusDays(30))
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();

        // Test all fields
        assertEquals(userId, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("Test User", user.getFullName());
        assertEquals("9876543210", user.getMobileNumber());
        assertEquals("Test bio", user.getBio());
        assertEquals("http://example.com/profile.jpg", user.getProfilePictureUrl());
        assertEquals(UserRole.CUSTOMER, user.getUserRole());
        assertEquals(UserType.CUSTOMER, user.getUserType());
        assertEquals("EMP001", user.getEmployeeId());
        assertEquals("IT", user.getDepartment());
        assertEquals("manager-123", user.getManagerId());
        assertEquals(List.of("READ", "WRITE"), user.getPermissions());
        assertEquals(50000.0, user.getSalary());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
        assertTrue(user.getIsEmailVerified());
        assertFalse(user.getIsMobileVerified());
        assertTrue(user.getIsTwoFactorEnabled());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getAccountLockedUntil());
        assertEquals("192.168.1.100", user.getLastLoginIp());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNull(user.getDeletedAt());
    }

    @Test
    void testUserEntity_BusinessMethods() {
        User user = User.builder()
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLockedUntil(null)
                .isEmailVerified(true)
                .isMobileVerified(true)
                .build();

        // Test account status methods
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());

        // Test locked account
        user.setAccountLockedUntil(LocalDateTime.now().plusHours(1));
        assertFalse(user.isAccountNonLocked());

        // Test expired lock
        user.setAccountLockedUntil(LocalDateTime.now().minusHours(1));
        assertTrue(user.isAccountNonLocked());

        // Test inactive account
        user.setAccountStatus(AccountStatus.INACTIVE);
        assertFalse(user.isEnabled());
    }

    @Test
    void testUserEntity_SettersAndGetters() {
        User user = new User();
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        // Test setters
        user.setId(userId);
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setFullName("New User");
        user.setFailedLoginAttempts(3);
        user.setLastLoginAt(now);

        // Test getters
        assertEquals(userId, user.getId());
        assertEquals("newuser", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("New User", user.getFullName());
        assertEquals(3, user.getFailedLoginAttempts());
        assertEquals(now, user.getLastLoginAt());
    }

    @Test
    void testUserEntity_Collections() {
        User user = new User();
        
        // Test addresses collection
        List<Address> addresses = new ArrayList<>();
        Address address = new Address();
        address.setId(UUID.randomUUID().toString());
        addresses.add(address);
        user.setAddresses(addresses);
        
        assertEquals(1, user.getAddresses().size());
        assertEquals(address.getId(), user.getAddresses().get(0).getId());

        // Test sessions collection
        List<UserSession> sessions = new ArrayList<>();
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID().toString());
        sessions.add(session);
        user.setSessions(sessions);
        
        assertEquals(1, user.getSessions().size());
        assertEquals(session.getId(), user.getSessions().get(0).getId());
    }

    // ========== USER SESSION ENTITY TESTS ==========

    @Test
    void testUserSessionEntity_Creation() {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .build();

        UserSession session = UserSession.builder()
                .id(sessionId)
                .sessionToken("session-token-123")
                .refreshToken("refresh-token-123")
                .user(user)
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent")
                .device("Test Device")
                .location("Test Location")
                .sessionType(SessionType.WEB)
                .isActive(true)
                .isTwoFactorVerified(false)
                .expiresAt(now.plusHours(24))
                .lastAccessedAt(now.minusMinutes(30))
                .loggedOutAt(null)
                .createdAt(now)
                .build();

        // Test all fields
        assertEquals(sessionId, session.getId());
        assertEquals("session-token-123", session.getSessionToken());
        assertEquals("refresh-token-123", session.getRefreshToken());
        assertEquals(user, session.getUser());
        assertEquals("192.168.1.100", session.getIpAddress());
        assertEquals("Test Agent", session.getUserAgent());
        assertEquals("Test Device", session.getDevice());
        assertEquals("Test Location", session.getLocation());
        assertEquals(SessionType.WEB, session.getSessionType());
        assertTrue(session.getIsActive());
        assertFalse(session.getIsTwoFactorVerified());
        assertNotNull(session.getExpiresAt());
        assertNotNull(session.getLastAccessedAt());
        assertNull(session.getLoggedOutAt());
        assertNotNull(session.getCreatedAt());
    }

    @Test
    void testUserSessionEntity_BusinessMethods() {
        LocalDateTime now = LocalDateTime.now();
        
        UserSession session = UserSession.builder()
                .isActive(true)
                .expiresAt(now.plusHours(1))
                .build();

        // Test expiration check
        assertFalse(session.isExpired());
        
        // Test expired session
        session.setExpiresAt(now.minusHours(1));
        assertTrue(session.isExpired());

        // Test logout method
        session.logout();
        assertFalse(session.getIsActive());
        assertNotNull(session.getLoggedOutAt());

        // Test extend session
        LocalDateTime newExpiry = now.plusHours(2);
        session.extendSession(newExpiry);
        assertEquals(newExpiry, session.getExpiresAt());
        assertNotNull(session.getLastAccessedAt());
    }

    @Test
    void testUserSessionEntity_SettersAndGetters() {
        UserSession session = new UserSession();
        LocalDateTime now = LocalDateTime.now();

        // Test setters
        session.setId("session-123");
        session.setSessionToken("token-123");
        session.setIsActive(false);
        session.setExpiresAt(now);
        session.setLoggedOutAt(now);

        // Test getters
        assertEquals("session-123", session.getId());
        assertEquals("token-123", session.getSessionToken());
        assertFalse(session.getIsActive());
        assertEquals(now, session.getExpiresAt());
        assertEquals(now, session.getLoggedOutAt());
    }

    // ========== ADDRESS ENTITY TESTS ==========

    @Test
    void testAddressEntity_Creation() {
        String addressId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .build();

        Address address = Address.builder()
                .id(addressId)
                .user(user)
                .name("John Doe")
                .phone("9876543210")
                .alternatePhone("+19876543211")
                .email("john.doe@example.com")
                .addressLine1("123 Main Street")
                .addressLine2("Apt 4B")
                .landmark("Near Central Park")
                .city("New York")
                .state("NY")
                .pincode("10001")
                .country("USA")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .latitude(40.7128)
                .longitude(-74.0060)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Test all fields
        assertEquals(addressId, address.getId());
        assertEquals(user, address.getUser());
        assertEquals("John Doe", address.getName());
        assertEquals("9876543210", address.getPhone());
        assertEquals("+19876543211", address.getAlternatePhone());
        assertEquals("john.doe@example.com", address.getEmail());
        assertEquals("123 Main Street", address.getAddressLine1());
        assertEquals("Apt 4B", address.getAddressLine2());
        assertEquals("Near Central Park", address.getLandmark());
        assertEquals("New York", address.getCity());
        assertEquals("NY", address.getState());
        assertEquals("10001", address.getPincode());
        assertEquals("USA", address.getCountry());
        assertEquals(AddressType.HOME, address.getAddressType());
        assertTrue(address.getIsDefault());
        assertEquals(40.7128, address.getLatitude());
        assertEquals(-74.0060, address.getLongitude());
        assertNotNull(address.getCreatedAt());
        assertNotNull(address.getUpdatedAt());
    }

    @Test
    void testAddressEntity_BusinessMethods() {
        Address address = Address.builder()
                .addressLine1("123 Main Street")
                .addressLine2("Apt 4B")
                .landmark("Downtown")
                .city("New York")
                .state("NY")
                .pincode("10001")
                .country("USA")
                .build();

        // Test full address method
        String fullAddress = address.getFullAddress();
        assertTrue(fullAddress.contains("123 Main Street"));
        assertTrue(fullAddress.contains("Apt 4B"));
        assertTrue(fullAddress.contains("Downtown"));
        assertTrue(fullAddress.contains("New York"));
        assertTrue(fullAddress.contains("NY"));
        assertTrue(fullAddress.contains("10001"));
        assertTrue(fullAddress.contains("USA"));

        // Test formatted address method
        String formattedAddress = address.getFullAddress();
        assertNotNull(formattedAddress);
        assertTrue(formattedAddress.length() > 0);
    }

    @Test
    void testAddressEntity_SettersAndGetters() {
        Address address = new Address();
        LocalDateTime now = LocalDateTime.now();

        // Test setters
        address.setId("address-123");
        address.setName("Jane Doe");
        address.setCity("Boston");
        address.setIsDefault(false);
        address.setCreatedAt(now);

        // Test getters
        assertEquals("address-123", address.getId());
        assertEquals("Jane Doe", address.getName());
        assertEquals("Boston", address.getCity());
        assertFalse(address.getIsDefault());
        assertEquals(now, address.getCreatedAt());
    }

    // ========== ENUM TESTS ==========

    @Test
    void testUserTypeEnum() {
        // Test all enum values that actually exist in UserType
        assertEquals("CUSTOMER", UserType.CUSTOMER.name());
        assertEquals("VENDOR", UserType.VENDOR.name());
        assertEquals("ADMIN", UserType.ADMIN.name());

        // Test enum methods
        UserType[] values = UserType.values();
        assertEquals(3, values.length);
        
        UserType customerType = UserType.valueOf("CUSTOMER");
        assertEquals(UserType.CUSTOMER, customerType);
        
        // Test display names and descriptions
        assertEquals("Customer", UserType.CUSTOMER.getDisplayName());
        assertEquals("Vendor", UserType.VENDOR.getDisplayName());
        assertEquals("Admin", UserType.ADMIN.getDisplayName());
        
        assertEquals("Regular customer", UserType.CUSTOMER.getDescription());
        assertEquals("Book vendor/supplier", UserType.VENDOR.getDescription());
        assertEquals("Administrative user", UserType.ADMIN.getDescription());
    }

    @Test
    void testUserRoleEnum() {
        // Test all enum values
        assertEquals("CUSTOMER", UserRole.CUSTOMER.name());
        assertEquals("ADMIN", UserRole.ADMIN.name());
        assertEquals("SUPER_ADMIN", UserRole.SUPER_ADMIN.name());
        assertEquals("MANAGER", UserRole.MANAGER.name());
        assertEquals("MODERATOR", UserRole.MODERATOR.name());
        assertEquals("SUPPORT", UserRole.SUPPORT.name());

        UserRole[] values = UserRole.values();
        assertEquals(6, values.length);
    }

    @Test
    void testAccountStatusEnum() {
        // Test all enum values
        assertEquals("ACTIVE", AccountStatus.ACTIVE.name());
        assertEquals("INACTIVE", AccountStatus.INACTIVE.name());
        assertEquals("SUSPENDED", AccountStatus.SUSPENDED.name());
        assertEquals("DELETED", AccountStatus.DELETED.name());
        assertEquals("PENDING_VERIFICATION", AccountStatus.PENDING_VERIFICATION.name());

        AccountStatus[] values = AccountStatus.values();
        assertEquals(5, values.length);
    }

    @Test
    void testSessionTypeEnum() {
        // Test all enum values
        assertEquals("WEB", SessionType.WEB.name());
        assertEquals("MOBILE", SessionType.MOBILE.name());
        assertEquals("API", SessionType.API.name());
        assertEquals("ADMIN", SessionType.ADMIN.name());

        SessionType[] values = SessionType.values();
        assertEquals(4, values.length);
    }

    @Test
    void testAddressTypeEnum() {
        // Test all enum values
        assertEquals("HOME", AddressType.HOME.name());
        assertEquals("WORK", AddressType.WORK.name());
        assertEquals("OTHER", AddressType.OTHER.name());

        AddressType[] values = AddressType.values();
        assertEquals(3, values.length);
    }

    // ========== ENTITY RELATIONSHIPS TESTS ==========

    @Test
    void testUserAddressRelationship() {
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .build();

        Address address1 = Address.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .name("Home Address")
                .addressType(AddressType.HOME)
                .build();

        Address address2 = Address.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .name("Work Address")
                .addressType(AddressType.WORK)
                .build();

        List<Address> addresses = List.of(address1, address2);
        user.setAddresses(addresses);

        // Test relationship
        assertEquals(2, user.getAddresses().size());
        assertEquals(user, address1.getUser());
        assertEquals(user, address2.getUser());
        assertTrue(user.getAddresses().contains(address1));
        assertTrue(user.getAddresses().contains(address2));
    }

    @Test
    void testUserSessionRelationship() {
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .build();

        UserSession session1 = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .sessionToken("token1")
                .sessionType(SessionType.WEB)
                .build();

        UserSession session2 = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .sessionToken("token2")
                .sessionType(SessionType.MOBILE)
                .build();

        List<UserSession> sessions = List.of(session1, session2);
        user.setSessions(sessions);

        // Test relationship
        assertEquals(2, user.getSessions().size());
        assertEquals(user, session1.getUser());
        assertEquals(user, session2.getUser());
        assertTrue(user.getSessions().contains(session1));
        assertTrue(user.getSessions().contains(session2));
    }

    // ========== ENTITY VALIDATION TESTS ==========

    @Test
    void testEntityEquality() {
        String userId = UUID.randomUUID().toString();
        
        User user1 = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        User user2 = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        // Test equality based on ID
        assertEquals(user1.getId(), user2.getId());
        
        // Test different IDs
        User user3 = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .email("test@example.com")
                .build();

        assertNotEquals(user1.getId(), user3.getId());
    }

    @Test
    void testEntityToString() {
        User user = User.builder()
                .id("user-123")
                .username("testuser")
                .email("test@example.com")
                .build();

        String userString = user.toString();
        assertNotNull(userString);
        assertTrue(userString.contains("testuser"));
    }

    @Test
    void testEntityHashCode() {
        String userId = UUID.randomUUID().toString();
        
        User user1 = User.builder()
                .id(userId)
                .username("testuser")
                .build();

        User user2 = User.builder()
                .id(userId)
                .username("testuser")
                .build();

        // Objects with same ID should have same hash code
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    // ========== BUILDER PATTERN TESTS ==========

    @Test
    void testUserBuilder() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals(UserType.CUSTOMER, user.getUserType());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
    }

    @Test
    void testUserSessionBuilder() {
        UserSession session = UserSession.builder()
                .sessionToken("token-123")
                .refreshToken("refresh-123")
                .sessionType(SessionType.WEB)
                .isActive(true)
                .build();

        assertNotNull(session);
        assertEquals("token-123", session.getSessionToken());
        assertEquals("refresh-123", session.getRefreshToken());
        assertEquals(SessionType.WEB, session.getSessionType());
        assertTrue(session.getIsActive());
    }

    @Test
    void testAddressBuilder() {
        Address address = Address.builder()
                .name("John Doe")
                .addressLine1("123 Main St")
                .city("New York")
                .state("NY")
                .addressType(AddressType.HOME)
                .isDefault(true)
                .build();

        assertNotNull(address);
        assertEquals("John Doe", address.getName());
        assertEquals("123 Main St", address.getAddressLine1());
        assertEquals("New York", address.getCity());
        assertEquals("NY", address.getState());
        assertEquals(AddressType.HOME, address.getAddressType());
        assertTrue(address.getIsDefault());
    }
}
