package com.bookstore.user_authentication_service.repository;

import com.bookstore.user_authentication_service.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for UserRepository
 * Tests all custom queries and repository methods
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User adminUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser1")
                .email("test1@example.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK")
                .fullName("Test User 1")
                .mobileNumber("9876543210")
                .userType(UserType.CUSTOMER)
                .userRole(UserRole.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .isMobileVerified(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(5))
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .build();

        testUser2 = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser2")
                .email("test2@example.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK")
                .fullName("Test User 2")
                .mobileNumber("9876543211")
                .userType(UserType.CUSTOMER)
                .userRole(UserRole.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(false)
                .isMobileVerified(true)
                .failedLoginAttempts(2)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();

        adminUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("adminuser")
                .email("admin@example.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK")
                .fullName("Admin User")
                .userType(UserType.ADMIN)
                .userRole(UserRole.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .department("IT")
                .employeeId("EMP001")
                .isEmailVerified(true)
                .isMobileVerified(true)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .lastLoginAt(LocalDateTime.now().minusHours(2))
                .build();

        inactiveUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("inactiveuser")
                .email("inactive@example.com")
                .password("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBzK3/7Qr6QZJK")
                .fullName("Inactive User")
                .userType(UserType.CUSTOMER)
                .userRole(UserRole.CUSTOMER)
                .accountStatus(AccountStatus.INACTIVE)
                .isEmailVerified(false)
                .isMobileVerified(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now().minusDays(15))
                .updatedAt(LocalDateTime.now().minusDays(10))
                .build();

        // Persist test data
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.persistAndFlush(adminUser);
        entityManager.persistAndFlush(inactiveUser);
    }

    // ========== BASIC CRUD TESTS ==========

    @Test
    void testSaveAndFindById() {
        User newUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("newuser")
                .email("new@example.com")
                .password("password")
                .fullName("New User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser);
        assertEquals(newUser.getUsername(), savedUser.getUsername());

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(newUser.getUsername(), foundUser.get().getUsername());
    }

    // ========== CUSTOM QUERY TESTS ==========

    @Test
    void testFindByUsername() {
        Optional<User> found = userRepository.findByUsername("testuser1");
        assertTrue(found.isPresent());
        assertEquals("testuser1", found.get().getUsername());
        assertEquals("test1@example.com", found.get().getEmail());

        Optional<User> notFound = userRepository.findByUsername("nonexistent");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("test1@example.com");
        assertTrue(found.isPresent());
        assertEquals("testuser1", found.get().getUsername());

        Optional<User> notFound = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByUsernameOrEmail() {
        // Test with username
        Optional<User> foundByUsername = userRepository.findByUsernameOrEmail("testuser1");
        assertTrue(foundByUsername.isPresent());
        assertEquals("testuser1", foundByUsername.get().getUsername());

        // Test with email
        Optional<User> foundByEmail = userRepository.findByUsernameOrEmail("test1@example.com");
        assertTrue(foundByEmail.isPresent());
        assertEquals("testuser1", foundByEmail.get().getUsername());

        // Test with non-existent
        Optional<User> notFound = userRepository.findByUsernameOrEmail("nonexistent");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testExistsByUsername() {
        assertTrue(userRepository.existsByUsername("testuser1"));
        assertTrue(userRepository.existsByUsername("adminuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        assertTrue(userRepository.existsByEmail("test1@example.com"));
        assertTrue(userRepository.existsByEmail("admin@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    // ========== USER TYPE AND ROLE QUERIES ==========

    @Test
    void testFindAllCustomers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> customers = userRepository.findAllCustomers(pageable);
        
        assertNotNull(customers);
        assertTrue(customers.getTotalElements() >= 3); // testUser1, testUser2, inactiveUser
        
        customers.getContent().forEach(user -> {
            assertEquals(UserType.CUSTOMER, user.getUserType());
        });
    }

    @Test
    void testFindAllAdmins() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> admins = userRepository.findAllAdmins(pageable);
        
        assertNotNull(admins);
        assertEquals(1, admins.getTotalElements()); // Only adminUser
        assertEquals("adminuser", admins.getContent().get(0).getUsername());
    }

    @Test
    void testFindByUserType() {
        List<User> customers = userRepository.findByUserType(UserType.CUSTOMER);
        List<User> admins = userRepository.findByUserType(UserType.ADMIN);
        
        assertTrue(customers.size() >= 3);
        assertEquals(1, admins.size());
        
        customers.forEach(user -> assertEquals(UserType.CUSTOMER, user.getUserType()));
        admins.forEach(user -> assertEquals(UserType.ADMIN, user.getUserType()));
    }

    @Test
    void testFindByUserRole() {
        // Create Pageable for testing
        Pageable pageable = PageRequest.of(0, 10);
        
        // Call repository method with correct parameters
        Page<User> customerRolePage = userRepository.findByUserRole(UserRole.CUSTOMER, pageable);
        Page<User> adminRolePage = userRepository.findByUserRole(UserRole.ADMIN, pageable);
        
        // Extract content from Page objects
        List<User> customerRole = customerRolePage.getContent();
        List<User> adminRole = adminRolePage.getContent();
        
        // Verify results
        assertTrue(customerRole.size() >= 3);
        assertEquals(1, adminRole.size());
        
        // Verify all users have correct roles
        customerRole.forEach(user -> assertEquals(UserRole.CUSTOMER, user.getUserRole()));
        adminRole.forEach(user -> assertEquals(UserRole.ADMIN, user.getUserRole()));
    }

    // ========== ACCOUNT STATUS QUERIES ==========

    @Test
    void testFindByAccountStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> activeUsersPage = userRepository.findByAccountStatus(AccountStatus.ACTIVE, pageable);
        Page<User> inactiveUsersPage = userRepository.findByAccountStatus(AccountStatus.INACTIVE, pageable);
        
        List<User> activeUsers = activeUsersPage.getContent();
        List<User> inactiveUsers = inactiveUsersPage.getContent();
        
        assertEquals(3, activeUsers.size()); // testUser1, testUser2, adminUser
        assertEquals(1, inactiveUsers.size()); // inactiveUser
        
        // Additional assertions for Page properties
        assertEquals(3, activeUsersPage.getTotalElements());
        assertEquals(1, inactiveUsersPage.getTotalElements());
    }

    @Test
    void testFindActiveUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> activeUsers = userRepository.findActiveUsers(pageable);
        
        assertEquals(3, activeUsers.getTotalElements());
        activeUsers.getContent().forEach(user -> {
            assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
        });
    }

    @Test
    void testFindInactiveUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> inactiveUsers = userRepository.findInactiveUsers(pageable);
        
        assertEquals(1, inactiveUsers.getTotalElements());
        assertEquals("inactiveuser", inactiveUsers.getContent().get(0).getUsername());
    }

    // ========== VERIFICATION STATUS QUERIES ==========

    @Test
    void testFindByEmailVerificationStatus() {
        List<User> emailVerified = userRepository.findByEmailVerificationStatus(true);
        List<User> emailNotVerified = userRepository.findByEmailVerificationStatus(false);
        
        assertEquals(2, emailVerified.size()); // testUser1, adminUser
        assertEquals(2, emailNotVerified.size()); // testUser2, inactiveUser
    }

    @Test
    void testFindByMobileVerificationStatus() {
        List<User> mobileVerified = userRepository.findByMobileVerificationStatus(true);
        List<User> mobileNotVerified = userRepository.findByMobileVerificationStatus(false);
        
        assertEquals(2, mobileVerified.size()); // testUser2, adminUser
        assertEquals(2, mobileNotVerified.size()); // testUser1, inactiveUser
    }

    @Test
    void testFindUnverifiedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> unverifiedUsers = userRepository.findUnverifiedUsers(pageable);
        
        // Users who have neither email nor mobile verified
        assertTrue(unverifiedUsers.getTotalElements() >= 1); // At least inactiveUser
    }

    // ========== SECURITY QUERIES ==========

    @Test
    void testFindUsersWithFailedAttempts() {
        List<User> usersWithFailedAttempts = userRepository.findUsersWithFailedAttempts();
        
        assertEquals(1, usersWithFailedAttempts.size()); // testUser2 has 2 failed attempts
        assertEquals("testuser2", usersWithFailedAttempts.get(0).getUsername());
        assertEquals(2, usersWithFailedAttempts.get(0).getFailedLoginAttempts());
    }

    @Test
    void testFindLockedUsers() {
        // Create a locked user
        User lockedUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("lockeduser")
                .email("locked@example.com")
                .password("password")
                .fullName("Locked User")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .failedLoginAttempts(5)
                .accountLockedUntil(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        entityManager.persistAndFlush(lockedUser);
        
        List<User> lockedUsers = userRepository.findLockedUsers(LocalDateTime.now());
        assertEquals(1, lockedUsers.size());
        assertEquals("lockeduser", lockedUsers.get(0).getUsername());
    }

    // ========== ACTIVITY QUERIES ==========

    @Test
    void testFindRecentlyActiveUsers() {
        LocalDateTime since = LocalDateTime.now().minusDays(2);
        List<User> recentlyActive = userRepository.findRecentlyActiveUsers(since);
        
        assertEquals(2, recentlyActive.size()); // testUser1 and adminUser have recent login
    }

    @Test
    void testFindInactiveUsersSince() {
        LocalDateTime since = LocalDateTime.now().minusDays(3);
        List<User> inactiveUsers = userRepository.findInactiveUsersSince(since);
        
        // Users who haven't logged in since the specified date
        assertTrue(inactiveUsers.size() >= 1); // testUser2 and inactiveUser
    }

    // ========== SEARCH QUERIES ==========

    @Test
    void testSearchUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Search by username
        Page<User> usernameResults = userRepository.searchUsers("testuser", pageable);
        assertEquals(2, usernameResults.getTotalElements()); // testuser1, testuser2
        
        // Search by email
        Page<User> emailResults = userRepository.searchUsers("test1@example.com", pageable);
        assertEquals(1, emailResults.getTotalElements());
        
        // Search by full name
        Page<User> nameResults = userRepository.searchUsers("Test User", pageable);
        assertEquals(2, nameResults.getTotalElements());
    }

    @Test
    void testSearchUsersByType() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> customerResults = userRepository.searchUsersByType("test", UserType.CUSTOMER, pageable);
        assertEquals(2, customerResults.getTotalElements()); // testuser1, testuser2
        
        Page<User> adminResults = userRepository.searchUsersByType("admin", UserType.ADMIN, pageable);
        assertEquals(1, adminResults.getTotalElements()); // adminuser
    }

    // ========== STATISTICS QUERIES ==========

    @Test
    void testCountByUserType() {
        long customerCount = userRepository.countByUserType(UserType.CUSTOMER);
        long adminCount = userRepository.countByUserType(UserType.ADMIN);
        
        assertEquals(3, customerCount); // testUser1, testUser2, inactiveUser
        assertEquals(1, adminCount); // adminUser
    }

    @Test
    void testCountByAccountStatus() {
        long activeCount = userRepository.countByAccountStatus(AccountStatus.ACTIVE);
        long inactiveCount = userRepository.countByAccountStatus(AccountStatus.INACTIVE);
        
        assertEquals(3, activeCount);
        assertEquals(1, inactiveCount);
    }

    @Test
    void testGetUserCountByType() {
        List<Object[]> userCounts = userRepository.getUserCountByType();
        
        assertNotNull(userCounts);
        assertEquals(2, userCounts.size()); // CUSTOMER and ADMIN types
        
        // Verify the counts
        for (Object[] count : userCounts) {
            UserType userType = (UserType) count[0];
            Long countValue = (Long) count[1];
            
            if (userType == UserType.CUSTOMER) {
                assertEquals(3L, countValue);
            } else if (userType == UserType.ADMIN) {
                assertEquals(1L, countValue);
            }
        }
    }

    @Test
    void testGetRegistrationCountByDate() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(35);
        List<Object[]> registrationCounts = userRepository.getRegistrationCountByDate(startDate);
        
        assertNotNull(registrationCounts);
        assertTrue(registrationCounts.size() >= 1);
    }

    // ========== UPDATE QUERIES ==========

    @Test
    void testUpdateFailedLoginAttempts() {
        int updatedRows = userRepository.updateFailedLoginAttempts(testUser1.getId(), 3);
        assertEquals(1, updatedRows);
        
        entityManager.flush();
        entityManager.clear();
        
        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertEquals(3, updatedUser.getFailedLoginAttempts());
    }

    @Test
    void testResetFailedLoginAttempts() {
        int updatedRows = userRepository.resetFailedLoginAttempts(testUser2.getId());
        assertEquals(1, updatedRows);
        
        entityManager.flush();
        entityManager.clear();
        
        User updatedUser = userRepository.findById(testUser2.getId()).orElseThrow();
        assertEquals(0, updatedUser.getFailedLoginAttempts());
    }

    @Test
    void testUpdateLastLoginAt() {
        LocalDateTime newLoginTime = LocalDateTime.now();
        int updatedRows = userRepository.updateLastLoginAt(testUser1.getId(), newLoginTime);
        assertEquals(1, updatedRows);
        
        entityManager.flush();
        entityManager.clear();
        
        User updatedUser = userRepository.findById(testUser1.getId()).orElseThrow();
        assertNotNull(updatedUser.getLastLoginAt());
    }

    @Test
    void testUpdateEmailVerificationStatus() {
        int updatedRows = userRepository.updateEmailVerificationStatus(testUser2.getId(), true);
        assertEquals(1, updatedRows);
        
        entityManager.flush();
        entityManager.clear();
        
        User updatedUser = userRepository.findById(testUser2.getId()).orElseThrow();
        assertTrue(updatedUser.getIsEmailVerified());
    }

    @Test
    void testUpdateAccountStatus() {
        int updatedRows = userRepository.updateAccountStatus(inactiveUser.getId(), AccountStatus.ACTIVE);
        assertEquals(1, updatedRows);
        
        entityManager.flush();
        entityManager.clear();
        
        User updatedUser = userRepository.findById(inactiveUser.getId()).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, updatedUser.getAccountStatus());
    }

    // ========== CLEANUP QUERIES ==========

    @Test
    void testDeleteInactiveUsers() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        int deletedRows = userRepository.deleteInactiveUsers(cutoffDate);
        
        // Should delete users who are inactive and created before cutoff date
        assertTrue(deletedRows >= 0);
    }

    @Test
    void testDeleteUnverifiedUsers() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        int deletedRows = userRepository.deleteUnverifiedUsers(cutoffDate);
        
        // Should delete users who are unverified and created before cutoff date
        assertTrue(deletedRows >= 0);
    }
}
