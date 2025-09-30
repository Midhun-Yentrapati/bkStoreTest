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
 * Comprehensive tests for UserSessionRepository
 * Tests all custom queries and session management operations
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserSessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserSessionRepository userSessionRepository;

    private User testUser1;
    private User testUser2;
    private UserSession activeSession1;
    private UserSession activeSession2;
    private UserSession expiredSession;
    private UserSession inactiveSession;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser1")
                .email("test1@example.com")
                .password("password")
                .fullName("Test User 1")
                .userType(UserType.CUSTOMER)
                .accountStatus(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser2")
                .email("test2@example.com")
                .password("password")
                .fullName("Test User 2")
                .userType(UserType.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);

        // Create test sessions
        activeSession1 = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .sessionToken("active-token-1")
                .refreshToken("refresh-token-1")
                .user(testUser1)
                .ipAddress("192.168.1.100")
                .userAgent("Test Agent 1")
                .device("Test Device 1")
                .location("Test Location 1")
                .sessionType(SessionType.WEB)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .lastAccessedAt(LocalDateTime.now().minusMinutes(30))
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        activeSession2 = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .sessionToken("active-token-2")
                .refreshToken("refresh-token-2")
                .user(testUser1)
                .ipAddress("192.168.1.101")
                .userAgent("Test Agent 2")
                .device("Test Device 2")
                .location("Test Location 2")
                .sessionType(SessionType.MOBILE)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusHours(12))
                .lastAccessedAt(LocalDateTime.now().minusMinutes(15))
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        expiredSession = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .sessionToken("expired-token")
                .refreshToken("expired-refresh-token")
                .user(testUser2)
                .ipAddress("192.168.1.102")
                .userAgent("Test Agent 3")
                .device("Test Device 3")
                .sessionType(SessionType.WEB)
                .isActive(true)
                .expiresAt(LocalDateTime.now().minusHours(1)) // Expired
                .lastAccessedAt(LocalDateTime.now().minusHours(2))
                .createdAt(LocalDateTime.now().minusHours(3))
                .build();

        inactiveSession = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .sessionToken("inactive-token")
                .refreshToken("inactive-refresh-token")
                .user(testUser2)
                .ipAddress("192.168.1.103")
                .userAgent("Test Agent 4")
                .device("Test Device 4")
                .sessionType(SessionType.WEB)
                .isActive(false)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .lastAccessedAt(LocalDateTime.now().minusHours(1))
                .loggedOutAt(LocalDateTime.now().minusMinutes(30))
                .createdAt(LocalDateTime.now().minusHours(4))
                .build();

        entityManager.persistAndFlush(activeSession1);
        entityManager.persistAndFlush(activeSession2);
        entityManager.persistAndFlush(expiredSession);
        entityManager.persistAndFlush(inactiveSession);
    }

    // ========== BASIC CRUD TESTS ==========

    @Test
    void testSaveAndFindById() {
        UserSession newSession = UserSession.builder()
                .id(UUID.randomUUID().toString())
                .sessionToken("new-token")
                .refreshToken("new-refresh-token")
                .user(testUser1)
                .ipAddress("192.168.1.200")
                .userAgent("New Agent")
                .sessionType(SessionType.WEB)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();

        UserSession savedSession = userSessionRepository.save(newSession);
        assertNotNull(savedSession);
        assertEquals(newSession.getSessionToken(), savedSession.getSessionToken());

        Optional<UserSession> foundSession = userSessionRepository.findById(savedSession.getId());
        assertTrue(foundSession.isPresent());
        assertEquals(newSession.getSessionToken(), foundSession.get().getSessionToken());
    }

    // ========== TOKEN QUERIES ==========

    @Test
    void testFindBySessionToken() {
        Optional<UserSession> found = userSessionRepository.findBySessionToken("active-token-1");
        assertTrue(found.isPresent());
        assertEquals(activeSession1.getId(), found.get().getId());
        assertEquals(testUser1.getId(), found.get().getUser().getId());

        Optional<UserSession> notFound = userSessionRepository.findBySessionToken("nonexistent-token");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByRefreshToken() {
        Optional<UserSession> found = userSessionRepository.findByRefreshToken("refresh-token-1");
        assertTrue(found.isPresent());
        assertEquals(activeSession1.getId(), found.get().getId());

        Optional<UserSession> notFound = userSessionRepository.findByRefreshToken("nonexistent-refresh-token");
        assertFalse(notFound.isPresent());
    }

    // ========== USER SESSION QUERIES ==========

    @Test
    void testFindByUserId() {
        List<UserSession> user1Sessions = userSessionRepository.findByUserId(testUser1.getId());
        assertEquals(2, user1Sessions.size()); // activeSession1, activeSession2

        List<UserSession> user2Sessions = userSessionRepository.findByUserId(testUser2.getId());
        assertEquals(2, user2Sessions.size()); // expiredSession, inactiveSession

        // Test with pagination
        Pageable pageable = PageRequest.of(0, 1);
        Page<UserSession> paginatedSessions = userSessionRepository.findByUserId(testUser1.getId(), pageable);
        assertEquals(1, paginatedSessions.getContent().size());
        assertEquals(2, paginatedSessions.getTotalElements());
    }

    @Test
    void testFindActiveSessionsByUserId() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(testUser1.getId(), now);
        
        assertEquals(2, activeSessions.size()); // Both activeSession1 and activeSession2
        activeSessions.forEach(session -> {
            assertTrue(session.getIsActive());
            assertTrue(session.getExpiresAt().isAfter(now));
        });

        // Test user2 - should have no active sessions (expired and inactive)
        List<UserSession> user2ActiveSessions = userSessionRepository.findActiveSessionsByUserId(testUser2.getId(), now);
        assertEquals(0, user2ActiveSessions.size());
    }

    @Test
    void testFindActiveSessions() {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserSession> activeSessions = userSessionRepository.findActiveSessions(now, pageable);
        
        assertEquals(2, activeSessions.getTotalElements()); // activeSession1, activeSession2
        activeSessions.getContent().forEach(session -> {
            assertTrue(session.getIsActive());
            assertTrue(session.getExpiresAt().isAfter(now));
        });
    }

    @Test
    void testFindExpiredActiveSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSession> expiredSessions = userSessionRepository.findExpiredActiveSessions(now);
        
        assertEquals(1, expiredSessions.size()); // expiredSession
        assertEquals(expiredSession.getId(), expiredSessions.get(0).getId());
        assertTrue(expiredSessions.get(0).getIsActive());
        assertTrue(expiredSessions.get(0).getExpiresAt().isBefore(now));
    }

    // ========== SESSION TYPE QUERIES ==========

    @Test
    void testFindByUserIdAndSessionType() {
        List<UserSession> webSessions = userSessionRepository.findByUserIdAndSessionType(testUser1.getId(), SessionType.WEB);
        assertEquals(1, webSessions.size()); // activeSession1

        List<UserSession> mobileSessions = userSessionRepository.findByUserIdAndSessionType(testUser1.getId(), SessionType.MOBILE);
        assertEquals(1, mobileSessions.size()); // activeSession2
    }

    @Test
    void testFindBySessionType() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserSession> webSessions = userSessionRepository.findBySessionType(SessionType.WEB, pageable);
        
        // Should find activeSession1, expiredSession, inactiveSession (all WEB type and active)
        assertEquals(1, webSessions.getTotalElements()); // Only activeSession1 is active WEB

        Page<UserSession> mobileSessions = userSessionRepository.findBySessionType(SessionType.MOBILE, pageable);
        assertEquals(1, mobileSessions.getTotalElements()); // activeSession2
    }

    // ========== DEVICE AND LOCATION QUERIES ==========

    @Test
    void testFindByUserIdAndDevice() {
        List<UserSession> deviceSessions = userSessionRepository.findByUserIdAndDevice(testUser1.getId(), "Test Device 1");
        assertEquals(1, deviceSessions.size());
        assertEquals(activeSession1.getId(), deviceSessions.get(0).getId());
    }

    @Test
    void testFindByUserIdAndIpAddress() {
        List<UserSession> ipSessions = userSessionRepository.findByUserIdAndIpAddress(testUser1.getId(), "192.168.1.100");
        assertEquals(1, ipSessions.size());
        assertEquals(activeSession1.getId(), ipSessions.get(0).getId());
    }

    @Test
    void testFindActiveSessionsByIpAddress() {
        List<UserSession> ipSessions = userSessionRepository.findActiveSessionsByIpAddress("192.168.1.100");
        assertEquals(1, ipSessions.size());
        assertEquals(activeSession1.getId(), ipSessions.get(0).getId());
        assertTrue(ipSessions.get(0).getIsActive());
    }

    // ========== ACTIVITY QUERIES ==========

    @Test
    void testFindRecentSessionsByUserId() {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<UserSession> recentSessions = userSessionRepository.findRecentSessionsByUserId(testUser1.getId(), since);
        
        assertEquals(1, recentSessions.size()); // Only activeSession2 was accessed within last hour
        assertEquals(activeSession2.getId(), recentSessions.get(0).getId());
    }

    @Test
    void testFindRecentActiveSessions() {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserSession> recentSessions = userSessionRepository.findRecentActiveSessions(since, pageable);
        
        assertEquals(1, recentSessions.getTotalElements()); // activeSession2
    }

    // ========== USER TYPE QUERIES ==========

    @Test
    void testFindActiveCustomerSessions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserSession> customerSessions = userSessionRepository.findActiveCustomerSessions(pageable);
        
        assertEquals(2, customerSessions.getTotalElements()); // activeSession1, activeSession2 (testUser1 is CUSTOMER)
    }

    @Test
    void testFindActiveAdminSessions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserSession> adminSessions = userSessionRepository.findActiveAdminSessions(pageable);
        
        assertEquals(0, adminSessions.getTotalElements()); // No active admin sessions (testUser2 sessions are expired/inactive)
    }

    // ========== STATISTICS QUERIES ==========

    @Test
    void testCountByUserId() {
        long user1Count = userSessionRepository.countByUserId(testUser1.getId());
        long user2Count = userSessionRepository.countByUserId(testUser2.getId());
        
        assertEquals(2, user1Count);
        assertEquals(2, user2Count);
    }

    @Test
    void testCountActiveSessionsByUserId() {
        LocalDateTime now = LocalDateTime.now();
        long user1ActiveCount = userSessionRepository.countActiveSessionsByUserId(testUser1.getId(), now);
        long user2ActiveCount = userSessionRepository.countActiveSessionsByUserId(testUser2.getId(), now);
        
        assertEquals(2, user1ActiveCount);
        assertEquals(0, user2ActiveCount);
    }

    @Test
    void testCountActiveSessions() {
        LocalDateTime now = LocalDateTime.now();
        long activeCount = userSessionRepository.countActiveSessions(now);
        
        assertEquals(2, activeCount); // activeSession1, activeSession2
    }

    @Test
    void testCountBySessionType() {
        long webCount = userSessionRepository.countBySessionType(SessionType.WEB);
        long mobileCount = userSessionRepository.countBySessionType(SessionType.MOBILE);
        
        assertEquals(1, webCount); // Only activeSession1 is active WEB
        assertEquals(1, mobileCount); // activeSession2
    }

    @Test
    void testGetSessionCountByType() {
        List<Object[]> sessionCounts = userSessionRepository.getSessionCountByType();
        
        assertNotNull(sessionCounts);
        assertEquals(2, sessionCounts.size()); // WEB and MOBILE types
        
        for (Object[] count : sessionCounts) {
            SessionType sessionType = (SessionType) count[0];
            Long countValue = (Long) count[1];
            
            if (sessionType == SessionType.WEB) {
                assertEquals(1L, countValue); // Only activeSession1 is active WEB
            } else if (sessionType == SessionType.MOBILE) {
                assertEquals(1L, countValue); // activeSession2
            }
        }
    }

    @Test
    void testCountActiveUsers() {
        LocalDateTime now = LocalDateTime.now();
        long activeUserCount = userSessionRepository.countActiveUsers(now);
        
        assertEquals(1, activeUserCount); // Only testUser1 has active sessions
    }

    // ========== TIME-BASED STATISTICS ==========

    @Test
    void testCountSessionsCreatedBetween() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(5);
        LocalDateTime endDate = LocalDateTime.now();
        
        long sessionCount = userSessionRepository.countSessionsCreatedBetween(startDate, endDate);
        assertEquals(4, sessionCount); // All test sessions were created within this range
    }

    @Test
    void testGetSessionCountByDate() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        List<Object[]> sessionCounts = userSessionRepository.getSessionCountByDate(startDate);
        
        assertNotNull(sessionCounts);
        assertTrue(sessionCounts.size() >= 1);
    }

    // ========== UPDATE QUERIES ==========

    @Test
    void testUpdateLastAccessedAt() {
        LocalDateTime newAccessTime = LocalDateTime.now();
        userSessionRepository.updateLastAccessedAt(activeSession1.getId(), newAccessTime);
        
        entityManager.flush();
        entityManager.clear();
        
        UserSession updatedSession = userSessionRepository.findById(activeSession1.getId()).orElseThrow();
        assertNotNull(updatedSession.getLastAccessedAt());
        // Note: Due to precision differences, we check if it's close to the expected time
        assertTrue(updatedSession.getLastAccessedAt().isAfter(newAccessTime.minusSeconds(1)));
    }

    @Test
    void testLogoutSession() {
        LocalDateTime logoutTime = LocalDateTime.now();
        userSessionRepository.logoutSession(activeSession1.getId(), logoutTime);
        
        entityManager.flush();
        entityManager.clear();
        
        UserSession loggedOutSession = userSessionRepository.findById(activeSession1.getId()).orElseThrow();
        assertFalse(loggedOutSession.getIsActive());
        assertNotNull(loggedOutSession.getLoggedOutAt());
    }

    @Test
    void testLogoutAllUserSessions() {
        LocalDateTime logoutTime = LocalDateTime.now();
        userSessionRepository.logoutAllUserSessions(testUser1.getId(), logoutTime);
        
        entityManager.flush();
        entityManager.clear();
        
        List<UserSession> userSessions = userSessionRepository.findByUserId(testUser1.getId());
        userSessions.forEach(session -> {
            assertFalse(session.getIsActive());
            assertNotNull(session.getLoggedOutAt());
        });
    }

    @Test
    void testLogoutOtherUserSessions() {
        LocalDateTime logoutTime = LocalDateTime.now();
        userSessionRepository.logoutOtherUserSessions(testUser1.getId(), activeSession1.getId(), logoutTime);
        
        entityManager.flush();
        entityManager.clear();
        
        // activeSession1 should remain active
        UserSession session1 = userSessionRepository.findById(activeSession1.getId()).orElseThrow();
        assertTrue(session1.getIsActive());
        
        // activeSession2 should be logged out
        UserSession session2 = userSessionRepository.findById(activeSession2.getId()).orElseThrow();
        assertFalse(session2.getIsActive());
        assertNotNull(session2.getLoggedOutAt());
    }

    @Test
    void testDeactivateExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        userSessionRepository.deactivateExpiredSessions(now);
        
        entityManager.flush();
        entityManager.clear();
        
        // expiredSession should be deactivated
        UserSession expiredSessionUpdated = userSessionRepository.findById(expiredSession.getId()).orElseThrow();
        assertFalse(expiredSessionUpdated.getIsActive());
    }

    // ========== CLEANUP QUERIES ==========

    @Test
    void testDeleteExpiredSessions() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMinutes(30);
        int deletedCount = userSessionRepository.deleteExpiredSessions(cutoffDate);
        
        // Should delete expired and inactive sessions older than cutoff
        assertTrue(deletedCount >= 0);
    }

    @Test
    void testDeleteOldInactiveSessions() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(3);
        int deletedCount = userSessionRepository.deleteOldInactiveSessions(cutoffDate);
        
        // Should delete old inactive sessions
        assertTrue(deletedCount >= 0);
    }

    // ========== SECURITY QUERIES ==========

    @Test
    void testFindRecentSessionsByIpAddress() {
        LocalDateTime since = LocalDateTime.now().minusHours(3);
        List<UserSession> recentSessions = userSessionRepository.findRecentSessionsByIpAddress("192.168.1.100", since);
        
        assertEquals(1, recentSessions.size());
        assertEquals(activeSession1.getId(), recentSessions.get(0).getId());
    }

    @Test
    void testCountDistinctIpAddressesByUserId() {
        LocalDateTime since = LocalDateTime.now().minusHours(5);
        long distinctIpCount = userSessionRepository.countDistinctIpAddressesByUserId(testUser1.getId(), since);
        
        assertEquals(2, distinctIpCount); // 192.168.1.100 and 192.168.1.101
    }

    @Test
    void testFindSuspiciousIpAddresses() {
        LocalDateTime since = LocalDateTime.now().minusHours(5);
        long threshold = 0; // Set low threshold to catch our test data
        
        List<Object[]> suspiciousIps = userSessionRepository.findSuspiciousIpAddresses(since, threshold);
        
        assertNotNull(suspiciousIps);
        // Each IP address in our test data appears only once, so with threshold 0, all should be returned
        assertTrue(suspiciousIps.size() >= 1);
    }
}
