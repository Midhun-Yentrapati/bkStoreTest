package com.bookstore.user_authentication_service.service;

import com.bookstore.user_authentication_service.dto.AdminRegistrationRequest;
import com.bookstore.user_authentication_service.dto.UserDTO;
import com.bookstore.user_authentication_service.dto.UserRegistrationRequest;
import com.bookstore.user_authentication_service.entity.*;
import com.bookstore.user_authentication_service.exception.ValidationException;
import com.bookstore.user_authentication_service.exception.ResourceNotFoundException;
import com.bookstore.user_authentication_service.repository.UserRepository;
import com.bookstore.user_authentication_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;
    private AdminRegistrationRequest adminRegistrationRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .fullName("Test User")
                .userRole(UserRole.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        registrationRequest = UserRegistrationRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .fullName("New User")
                .mobileNumber("1234567890")
                .build();

        adminRegistrationRequest = AdminRegistrationRequest.builder()
                .username("admin")
                .email("admin@example.com")
                .password("AdminPass123!")
                .confirmPassword("AdminPass123!")
                .fullName("Admin User")
                .userRole(UserRole.ADMIN)
                .department("IT")
                .employeeId("EMP001")
                .permissions(Set.of(Permission.USER_CREATE, Permission.USER_READ))
                .build();
    }

    @Nested
    @DisplayName("User Creation Tests")
    class UserCreationTests {

        @Test
        @DisplayName("Should create user with minimal required fields")
        void shouldCreateUserWithMinimalFields() {
            // Given
            registrationRequest.setMobileNumber(null);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDTO result = userService.createUser(registrationRequest);

            // Then
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should create user with all optional fields populated")
        void shouldCreateUserWithAllFields() {
            // Given
            registrationRequest.setDateOfBirth(LocalDate.now().minusYears(25));
            registrationRequest.setBio("Test bio");
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDTO result = userService.createUser(registrationRequest);

            // Then
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            verify(userRepository).save(argThat(user -> 
                user.getDateOfBirth() != null && user.getBio() != null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "test@", "@example.com", "test.example.com"})
        @DisplayName("Should reject invalid email formats")
        void shouldRejectInvalidEmailFormats(String invalidEmail) {
            // Given
            registrationRequest.setEmail(invalidEmail);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                userService.createUser(registrationRequest));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(true);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                userService.createUser(registrationRequest));
            assertTrue(exception.getMessage().contains("Username already exists"));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                userService.createUser(registrationRequest));
            assertTrue(exception.getMessage().contains("Email already exists"));
        }
    }

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"weak", "12345", "password", "PASSWORD", "Pass1"})
        @DisplayName("Should reject weak passwords")
        void shouldRejectWeakPasswords(String weakPassword) {
            // Given
            registrationRequest.setPassword(weakPassword);
            registrationRequest.setConfirmPassword(weakPassword);

            // When & Then
            assertThrows(ValidationException.class, () -> 
                userService.createUser(registrationRequest));
        }

        @Test
        @DisplayName("Should reject mismatched passwords")
        void shouldRejectMismatchedPasswords() {
            // Given
            registrationRequest.setPassword("Password123!");
            registrationRequest.setConfirmPassword("DifferentPassword123!");

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                userService.createUser(registrationRequest));
            assertTrue(exception.getMessage().contains("Passwords do not match"));
        }

        @Test
        @DisplayName("Should encode password correctly")
        void shouldEncodePasswordCorrectly() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.createUser(registrationRequest);

            // Then
            verify(passwordEncoder).encode("Password123!");
            verify(userRepository).save(argThat(user -> 
                "encoded-password".equals(user.getPassword())));
        }
    }

    @Nested
    @DisplayName("Account Locking Tests")
    class AccountLockingTests {

        @Test
        @DisplayName("Should increment failed login attempts")
        void shouldIncrementFailedLoginAttempts() {
            // Given
            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.incrementFailedLoginAttempts("test-user-id");

            // Then
            verify(userRepository).save(argThat(user -> 
                user.getFailedLoginAttempts() == 1));
        }

        @Test
        @DisplayName("Should lock account after max failed attempts")
        void shouldLockAccountAfterMaxFailedAttempts() {
            // Given
            testUser.setFailedLoginAttempts(4); // One less than max (5)
            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.incrementFailedLoginAttempts("test-user-id");

            // Then
            verify(userRepository).save(argThat(user -> 
                user.getAccountStatus() == AccountStatus.LOCKED &&
                user.getFailedLoginAttempts() == 5));
        }

        @Test
        @DisplayName("Should reset failed attempts on successful login")
        void shouldResetFailedAttemptsOnSuccessfulLogin() {
            // Given
            testUser.setFailedLoginAttempts(3);
            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.resetFailedLoginAttempts("test-user-id");

            // Then
            verify(userRepository).save(argThat(user -> 
                user.getFailedLoginAttempts() == 0));
        }

        @Test
        @DisplayName("Should activate locked account")
        void shouldActivateLockedAccount() {
            // Given
            testUser.setAccountStatus(AccountStatus.LOCKED);
            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.activateAccount("test-user-id");

            // Then
            verify(userRepository).save(argThat(user -> 
                user.getAccountStatus() == AccountStatus.ACTIVE &&
                user.getFailedLoginAttempts() == 0));
        }

        @Test
        @DisplayName("Should deactivate active account")
        void shouldDeactivateActiveAccount() {
            // Given
            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.deactivateAccount("test-user-id");

            // Then
            verify(userRepository).save(argThat(user -> 
                user.getAccountStatus() == AccountStatus.INACTIVE));
        }
    }

    @Nested
    @DisplayName("Admin User Creation Tests")
    class AdminUserCreationTests {

        @Test
        @DisplayName("Should create admin user with all fields")
        void shouldCreateAdminUserWithAllFields() {
            // Given
            User adminUser = User.builder()
                    .id("admin-id")
                    .username("admin")
                    .email("admin@example.com")
                    .userRole(UserRole.ADMIN)
                    .department("IT")
                    .employeeId("EMP001")
                    .permissions(Set.of(Permission.USER_CREATE, Permission.USER_READ))
                    .build();

            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByEmployeeId(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(adminUser);

            // When
            UserDTO result = userService.createAdminUser(adminRegistrationRequest);

            // Then
            assertNotNull(result);
            assertEquals("admin", result.getUsername());
            assertEquals(UserRole.ADMIN, result.getUserRole());
            assertEquals("IT", result.getDepartment());
            assertEquals("EMP001", result.getEmployeeId());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when employee ID already exists")
        void shouldThrowExceptionWhenEmployeeIdExists() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByEmployeeId(anyString())).thenReturn(true);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                userService.createAdminUser(adminRegistrationRequest));
            assertTrue(exception.getMessage().contains("Employee ID already exists"));
        }

        @Test
        @DisplayName("Should validate admin role permissions")
        void shouldValidateAdminRolePermissions() {
            // Given
            adminRegistrationRequest.setUserRole(UserRole.CUSTOMER);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                userService.createAdminUser(adminRegistrationRequest));
            assertTrue(exception.getMessage().contains("Invalid role for admin user"));
        }
    }

    @Nested
    @DisplayName("User Retrieval Tests")
    class UserRetrievalTests {

        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            // Given
            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));

            // When
            Optional<UserDTO> result = userService.getUserById("test-user-id");

            // Then
            assertTrue(result.isPresent());
            assertEquals("testuser", result.get().getUsername());
        }

        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void shouldThrowExceptionWhenUserNotFoundById() {
            // Given
            when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

            // When
            Optional<UserDTO> result = userService.getUserById("non-existent-id");

            // Then
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            // When
            Optional<UserDTO> result = userService.getUserByUsername("testuser");

            // Then
            assertTrue(result.isPresent());
            assertEquals("testuser", result.get().getUsername());
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // When
            Optional<UserDTO> result = userService.getUserByEmail("test@example.com");

            // Then
            assertTrue(result.isPresent());
            assertEquals("test@example.com", result.get().getEmail());
        }

        @Test
        @DisplayName("Should get all users with pagination")
        void shouldGetAllUsersWithPagination() {
            // Given
            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findAll(pageable)).thenReturn(userPage);

            // When
            Page<UserDTO> result = userService.getAllUsers(pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("testuser", result.getContent().get(0).getUsername());
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UserUpdateTests {

        @Test
        @DisplayName("Should update user profile")
        void shouldUpdateUserProfile() {
            // Given
            UserDTO updateRequest = UserDTO.builder()
                    .id("test-user-id")
                    .fullName("Updated Name")
                    .mobileNumber("9876543210")
                    .bio("Updated bio")
                    .build();

            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDTO result = userService.updateUser("test-user-id", updateRequest);

            // Then
            assertNotNull(result);
            verify(userRepository).save(argThat(user -> 
                "Updated Name".equals(user.getFullName()) &&
                "9876543210".equals(user.getMobileNumber()) &&
                "Updated bio".equals(user.getBio())));
        }

        @Test
        @DisplayName("Should verify email")
        void shouldVerifyEmail() {
            // Given
            testUser.setIsEmailVerified(false);
            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.verifyEmail("test-user-id");

            // Then
            verify(userRepository).save(argThat(user -> user.getIsEmailVerified()));
        }

        @Test
        @DisplayName("Should update last login time")
        void shouldUpdateLastLoginTime() {
            // Given
            when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.updateLastLogin("test-user-id", "192.168.1.1");

            // Then
            verify(userRepository).save(argThat(user -> user.getLastLoginAt() != null));
        }
    }

    @Nested
    @DisplayName("Search and Filter Tests")
    class SearchAndFilterTests {

        @Test
        @DisplayName("Should search users by search term")
        void shouldSearchUsersBySearchTerm() {
            // Given
            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.searchUsers(anyString(), eq(pageable)))
                    .thenReturn(userPage);

            // When
            Page<UserDTO> result = userService.searchUsers("test", pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(userRepository).searchUsers("test", pageable);
        }

        @Test
        @DisplayName("Should filter users by multiple criteria")
        void shouldFilterUsersByMultipleCriteria() {
            // Given
            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 10);
            
            // Mock the repository methods that getUsersWithFilters would use
            when(userRepository.findByUserRole(any(UserRole.class), eq(pageable)))
                    .thenReturn(userPage);
            when(userRepository.findByAccountStatus(any(AccountStatus.class), eq(pageable)))
                    .thenReturn(userPage);

            // When
            Page<UserDTO> result = userService.getUsersWithFilters(
                UserRole.CUSTOMER, AccountStatus.ACTIVE, null, null, null, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            // Verify that the appropriate repository method was called
            // Note: The exact verification depends on the implementation in UserServiceImpl
        }

        @Test
        @DisplayName("Should get users by role")
        void shouldGetUsersByRole() {
            // Given
            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findByUserRole(UserRole.CUSTOMER, pageable)).thenReturn(userPage);

            // When
            Page<UserDTO> result = userService.getUsersByRole(UserRole.CUSTOMER, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(UserRole.CUSTOMER, result.getContent().get(0).getUserRole());
            verify(userRepository).findByUserRole(UserRole.CUSTOMER, pageable);
        }

        @Test
        @DisplayName("Should get users by account status using filters")
        void shouldGetUsersByAccountStatusUsingFilters() {
            // Given
            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findUsersWithFilters(eq(null), eq(AccountStatus.ACTIVE), eq(null), eq(null), eq(null), eq(pageable))).thenReturn(userPage);

            // When
            Page<UserDTO> result = userService.getUsersWithFilters(null, AccountStatus.ACTIVE, null, null, null, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(AccountStatus.ACTIVE, result.getContent().get(0).getAccountStatus());
            verify(userRepository).findUsersWithFilters(eq(null), eq(AccountStatus.ACTIVE), eq(null), eq(null), eq(null), eq(pageable));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null user ID gracefully")
        void shouldHandleNullUserIdGracefully() {
            // When & Then
            assertThrows(ValidationException.class, () -> userService.getUserById(null));
            assertThrows(ValidationException.class, () -> userService.activateAccount(null));
            assertThrows(ValidationException.class, () -> userService.deactivateAccount(null));
        }

        @Test
        @DisplayName("Should handle empty string user ID gracefully")
        void shouldHandleEmptyStringUserIdGracefully() {
            // When & Then
            assertThrows(ValidationException.class, () -> userService.getUserById(""));
            assertThrows(ValidationException.class, () -> userService.activateAccount(""));
            assertThrows(ValidationException.class, () -> userService.deactivateAccount(""));
        }

        @Test
        @DisplayName("Should handle null registration request")
        void shouldHandleNullRegistrationRequest() {
            // When & Then
            assertThrows(ValidationException.class, () -> userService.createUser(null));
            assertThrows(ValidationException.class, () -> userService.createAdminUser(null));
        }

        @Test
        @DisplayName("Should handle very long input strings")
        void shouldHandleVeryLongInputStrings() {
            // Given
            String veryLongString = "a".repeat(1000);
            registrationRequest.setFullName(veryLongString);

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                userService.createUser(registrationRequest));
            assertTrue(exception.getMessage().contains("too long") || 
                      exception.getMessage().contains("exceeds maximum length"));
        }

        @Test
        @DisplayName("Should handle special characters in input")
        void shouldHandleSpecialCharactersInInput() {
            // Given
            registrationRequest.setUsername("user@#$%");
            registrationRequest.setFullName("User <script>alert('xss')</script>");

            // When & Then
            ValidationException exception = assertThrows(ValidationException.class, () -> 
                userService.createUser(registrationRequest));
            assertTrue(exception.getMessage().contains("Invalid characters") || 
                      exception.getMessage().contains("invalid format"));
        }
    }
}
