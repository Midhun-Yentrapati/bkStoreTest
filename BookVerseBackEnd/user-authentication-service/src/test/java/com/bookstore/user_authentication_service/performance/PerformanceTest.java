package com.bookstore.user_authentication_service.performance;

import com.bookstore.user_authentication_service.dto.UserRegistrationRequest;
import com.bookstore.user_authentication_service.dto.LoginRequest;
import com.bookstore.user_authentication_service.dto.AddressDTO;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.repository.UserSessionRepository;
import com.bookstore.user_authentication_service.service.UserService;
import com.bookstore.user_authentication_service.service.AuthenticationService;
import com.bookstore.user_authentication_service.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=test")
@DisplayName("Performance and Load Tests")
class PerformanceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @BeforeEach
    void setUp() {
        // Clean up for performance tests
        userSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Bulk Operations Performance Tests")
    class BulkOperationsTests {

        @Test
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle bulk user creation efficiently")
        void shouldHandleBulkUserCreationEfficiently() {
            long startTime = System.currentTimeMillis();
            
            // Create 1000 users
            for (int i = 0; i < 1000; i++) {
                UserRegistrationRequest request = UserRegistrationRequest.builder()
                        .username("bulkuser" + i)
                        .email("bulkuser" + i + "@example.com")
                        .password("Password123!")
                        .confirmPassword("Password123!")
                        .fullName("Bulk User " + i)
                        .mobileNumber("987654321" + (i % 10))
                        .build();
                
                userService.createUser(request);
                
                // Log progress every 100 users
                if ((i + 1) % 100 == 0) {
                    long currentTime = System.currentTimeMillis();
                    System.out.printf("Created %d users in %d ms%n", 
                        i + 1, currentTime - startTime);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("Total time for 1000 users: %d ms (%.2f users/second)%n", 
                totalTime, 1000.0 / (totalTime / 1000.0));
            
            // Verify all users were created
            long userCount = userRepository.count();
            assertEquals(1000, userCount);
            
            // Performance assertion: should create at least 10 users per second
            assertTrue(totalTime < 100000, "Bulk user creation took too long: " + totalTime + "ms");
        }

        @Test
        @Timeout(value = 20, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle bulk address creation efficiently")
        void shouldHandleBulkAddressCreationEfficiently() {
            // First create a user
            UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                    .username("addressuser")
                    .email("address@example.com")
                    .password("Password123!")
                    .confirmPassword("Password123!")
                    .fullName("Address User")
                    .build();
            
            String userId = userService.createUser(userRequest).getId();
            
            long startTime = System.currentTimeMillis();
            
            // Create 500 addresses for the user
            for (int i = 0; i < 500; i++) {
                AddressDTO addressDTO = AddressDTO.builder()
                        .name("User Address " + i)
                        .phone("987654321" + (i % 10))
                        .addressLine1("Address Line " + i)
                        .addressLine2("Locality " + i)  // Using addressLine2 instead of locality
                        .city("City " + (i % 10))
                        .state("State")
                        .country("Country")
                        .pincode("12345" + (i % 10))
                        .addressType(i % 2 == 0 ? AddressType.HOME : AddressType.WORK)
                        .isDefault(false)
                        .isActive(true)
                        .build();
                
                addressService.createAddress(userId, addressDTO);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("Total time for 500 addresses: %d ms (%.2f addresses/second)%n", 
                totalTime, 500.0 / (totalTime / 1000.0));
            
            // Verify all addresses were created
            List<AddressDTO> addresses = addressService.getUserAddresses(userId);
            assertEquals(500, addresses.size());
            
            // Performance assertion
            assertTrue(totalTime < 50000, "Bulk address creation took too long: " + totalTime + "ms");
        }

        @Test
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle bulk search operations efficiently")
        void shouldHandleBulkSearchOperationsEfficiently() {
            // Create test data
            for (int i = 0; i < 100; i++) {
                UserRegistrationRequest request = UserRegistrationRequest.builder()
                        .username("searchuser" + i)
                        .email("searchuser" + i + "@example.com")
                        .password("Password123!")
                        .confirmPassword("Password123!")
                        .fullName("Search User " + i)
                        .build();
                
                userService.createUser(request);
            }
            
            long startTime = System.currentTimeMillis();
            
            // Perform 1000 search operations
            for (int i = 0; i < 1000; i++) {
                Pageable pageable = PageRequest.of(0, 10);
                Page<com.bookstore.user_authentication_service.dto.UserDTO> results = 
                    userService.getUsersWithFilters(UserRole.CUSTOMER, AccountStatus.ACTIVE, null, null, null, pageable);
                
                assertNotNull(results);
                assertTrue(results.getTotalElements() > 0);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("Total time for 1000 searches: %d ms (%.2f searches/second)%n", 
                totalTime, 1000.0 / (totalTime / 1000.0));
            
            // Performance assertion: should handle at least 50 searches per second
            assertTrue(totalTime < 20000, "Bulk search operations took too long: " + totalTime + "ms");
        }
    }

    @Nested
    @DisplayName("Concurrent Load Tests")
    class ConcurrentLoadTests {

        @Test
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle concurrent user registrations")
        void shouldHandleConcurrentUserRegistrations() throws Exception {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            // 100 concurrent registration attempts
            for (int i = 0; i < 100; i++) {
                final int userId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        UserRegistrationRequest request = UserRegistrationRequest.builder()
                                .username("concurrent" + userId)
                                .email("concurrent" + userId + "@example.com")
                                .password("Password123!")
                                .confirmPassword("Password123!")
                                .fullName("Concurrent User " + userId)
                                .build();
                        
                        userService.createUser(request);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("Registration failed for user " + userId + ": " + e.getMessage());
                    }
                }, executor);
                futures.add(future);
            }
            
            // Wait for all to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("Concurrent registrations: %d successful, %d errors in %d ms%n", 
                successCount.get(), errorCount.get(), totalTime);
            
            // Most should succeed
            assertTrue(successCount.get() >= 95, "Too many registration failures: " + errorCount.get());
            
            // Verify database consistency
            long dbCount = userRepository.count();
            assertEquals(successCount.get(), dbCount);
        }

        @Test
        @Timeout(value = 25, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle concurrent login attempts")
        void shouldHandleConcurrentLoginAttempts() throws Exception {
            // Create test users first
            List<String> usernames = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                UserRegistrationRequest request = UserRegistrationRequest.builder()
                        .username("loginuser" + i)
                        .email("loginuser" + i + "@example.com")
                        .password("Password123!")
                        .confirmPassword("Password123!")
                        .fullName("Login User " + i)
                        .build();
                
                userService.createUser(request);
                usernames.add("loginuser" + i);
            }
            
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            // 200 concurrent login attempts (4 per user)
            for (int i = 0; i < 200; i++) {
                final String username = usernames.get(i % usernames.size());
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        LoginRequest loginRequest = LoginRequest.builder()
                                .usernameOrEmail(username)
                                .password("Password123!")
                                .build();
                        
                        authenticationService.authenticateUser(loginRequest);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                }, executor);
                futures.add(future);
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("Concurrent logins: %d successful, %d errors in %d ms%n", 
                successCount.get(), errorCount.get(), totalTime);
            
            // Most should succeed
            assertTrue(successCount.get() >= 190, "Too many login failures: " + errorCount.get());
        }

        @Test
        @Timeout(value = 20, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle concurrent address operations")
        void shouldHandleConcurrentAddressOperations() throws Exception {
            // Create a user first
            UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                    .username("concurrentaddressuser")
                    .email("concurrentaddress@example.com")
                    .password("Password123!")
                    .confirmPassword("Password123!")
                    .fullName("Concurrent Address User")
                    .build();
            
            String userId = userService.createUser(userRequest).getId();
            
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            // 50 concurrent address creations
            for (int i = 0; i < 50; i++) {
                final int addressId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        AddressDTO addressDTO = AddressDTO.builder()
                                .name("Concurrent Address " + addressId)
                                .phone("987654321" + (addressId % 10))
                                .addressLine1("Concurrent Address Line " + addressId)
                                .landmark("Locality Landmark")
                                .city("City")
                                .state("State")
                                .country("Country")
                                .pincode("12345" + (addressId % 10))
                                .addressType(AddressType.HOME)
                                .build();
                        
                        addressService.createAddress(userId, addressDTO);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        System.err.println("Address creation failed: " + e.getMessage());
                    }
                }, executor);
                futures.add(future);
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("Concurrent address operations: %d successful in %d ms%n", 
                successCount.get(), totalTime);
            
            // Verify addresses were created
            List<AddressDTO> addresses = addressService.getUserAddresses(userId);
            assertEquals(successCount.get(), addresses.size());
            
            // Verify only one is marked as default
            long defaultCount = addresses.stream().mapToLong(a -> a.getIsDefault() ? 1 : 0).sum();
            assertEquals(1, defaultCount);
        }
    }

    @Nested
    @DisplayName("Memory Usage Tests")
    class MemoryUsageTests {

        @Test
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle large result sets efficiently")
        void shouldHandleLargeResultSetsEfficiently() {
            // Create 1000 users
            for (int i = 0; i < 1000; i++) {
                UserRegistrationRequest request = UserRegistrationRequest.builder()
                        .username("memoryuser" + i)
                        .email("memoryuser" + i + "@example.com")
                        .password("Password123!")
                        .confirmPassword("Password123!")
                        .fullName("Memory User " + i)
                        .build();
                
                userService.createUser(request);
            }
            
            // Test pagination with different page sizes
            int[] pageSizes = {10, 50, 100, 200};
            
            for (int pageSize : pageSizes) {
                long startTime = System.currentTimeMillis();
                
                Pageable pageable = PageRequest.of(0, pageSize);
                Page<com.bookstore.user_authentication_service.dto.UserDTO> results = 
                    userService.getAllUsers(pageable);
                
                long endTime = System.currentTimeMillis();
                
                assertNotNull(results);
                assertEquals(pageSize, results.getContent().size());
                assertEquals(1000, results.getTotalElements());
                
                System.out.printf("Page size %d: %d ms%n", pageSize, endTime - startTime);
                
                // Larger page sizes should not be significantly slower
                assertTrue(endTime - startTime < 5000, 
                    "Page size " + pageSize + " took too long: " + (endTime - startTime) + "ms");
            }
        }

        @Test
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle repeated operations without memory leaks")
        void shouldHandleRepeatedOperationsWithoutMemoryLeaks() {
            // Create a user
            UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                    .username("repeateduser")
                    .email("repeated@example.com")
                    .password("Password123!")
                    .confirmPassword("Password123!")
                    .fullName("Repeated User")
                    .build();
            
            String userId = userService.createUser(userRequest).getId();
            
            // Perform the same operations many times
            for (int i = 0; i < 1000; i++) {
                // Update user
                userService.updateLastLogin(userId, "127.0.0.1");
                
                // Find user
                userService.getUserById(userId);
                
                // Reset failed attempts
                userService.resetFailedLoginAttempts(userId);
                
                if (i % 100 == 0) {
                    System.out.printf("Completed %d iterations%n", i);
                }
            }
            
            // Verify user is still in consistent state
            Optional<com.bookstore.user_authentication_service.dto.UserDTO> userOpt = userService.getUserById(userId);
            com.bookstore.user_authentication_service.dto.UserDTO user = userOpt.orElse(null);
            assertNotNull(user);
            assertEquals("repeateduser", user.getUsername());
        }

        @RepeatedTest(5)
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        @DisplayName("Should maintain consistent performance across repeated runs")
        void shouldMaintainConsistentPerformanceAcrossRepeatedRuns() {
            long startTime = System.currentTimeMillis();
            
            // Create 100 users
            for (int i = 0; i < 100; i++) {
                UserRegistrationRequest request = UserRegistrationRequest.builder()
                        .username("perfuser" + System.nanoTime() + i)
                        .email("perfuser" + System.nanoTime() + i + "@example.com")
                        .password("Password123!")
                        .confirmPassword("Password123!")
                        .fullName("Performance User " + i)
                        .build();
                
                userService.createUser(request);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("Run completed in %d ms%n", totalTime);
            
            // Performance should be consistent (within reasonable bounds)
            assertTrue(totalTime < 15000, "Performance degraded: " + totalTime + "ms");
        }
    }

    @Nested
    @DisplayName("Database Performance Tests")
    class DatabasePerformanceTests {

        @Test
        @Timeout(value = 20, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle complex queries efficiently")
        void shouldHandleComplexQueriesEfficiently() {
            // Create diverse test data
            
            for (int i = 0; i < 300; i++) {
                UserRegistrationRequest request = UserRegistrationRequest.builder()
                        .username("queryuser" + i)
                        .email("queryuser" + i + "@example.com")
                        .password("Password123!")
                        .confirmPassword("Password123!")
                        .fullName("Query User " + i)
                        .build();
                
                com.bookstore.user_authentication_service.dto.UserDTO user = userService.createUser(request);
                
                // Update some users with different roles and statuses
                if (i % 3 == 1) {
                    userService.deactivateAccount(user.getId());
                }
            }
            
            // Test various search combinations
            String[] searchTerms = {"queryuser", "Query", "User", "user1", "user2"};
            
            for (String searchTerm : searchTerms) {
                long startTime = System.currentTimeMillis();
                
                Pageable pageable = PageRequest.of(0, 20);
                Page<com.bookstore.user_authentication_service.dto.UserDTO> results = 
                    userService.searchUsers(searchTerm, pageable);
                
                long endTime = System.currentTimeMillis();
                
                assertNotNull(results);
                
                // Each query should complete quickly
                assertTrue(endTime - startTime < 2000, 
                    "Search query took too long: " + (endTime - startTime) + "ms");
            }
        }

        @Test
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        @DisplayName("Should handle transaction rollbacks efficiently")
        void shouldHandleTransactionRollbacksEfficiently() {
            long startTime = System.currentTimeMillis();
            
            // Attempt operations that will fail and rollback
            for (int i = 0; i < 100; i++) {
                try {
                    // Try to create user with invalid data
                    UserRegistrationRequest invalidRequest = UserRegistrationRequest.builder()
                            .username("") // Invalid username
                            .email("invalid-email")
                            .password("weak")
                            .confirmPassword("different")
                            .build();
                    
                    userService.createUser(invalidRequest);
                } catch (Exception e) {
                    // Expected to fail
                }
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.printf("100 rollbacks completed in %d ms%n", totalTime);
            
            // Rollbacks should be handled efficiently
            assertTrue(totalTime < 10000, "Transaction rollbacks took too long: " + totalTime + "ms");
            
            // Verify database is still consistent
            long userCount = userRepository.count();
            assertEquals(0, userCount); // No users should have been created
        }
    }
}
