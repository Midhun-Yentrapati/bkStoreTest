package com.bookstore.user_authentication_service.repository;

import com.bookstore.user_authentication_service.entity.SessionType;
import com.bookstore.user_authentication_service.entity.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    
    // Basic Session Queries
    @Query("SELECT s FROM UserSession s WHERE s.sessionToken = :sessionToken")
    Optional<UserSession> findBySessionToken(@Param("sessionToken") String sessionToken);
    
    @Query("SELECT s FROM UserSession s WHERE s.refreshToken = :refreshToken")
    Optional<UserSession> findByRefreshToken(@Param("refreshToken") String refreshToken);
    
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<UserSession> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    Page<UserSession> findByUserId(@Param("userId") String userId, Pageable pageable);
    
    // Active Session Queries
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true AND s.expiresAt > :now ORDER BY s.lastAccessedAt DESC")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM UserSession s WHERE s.isActive = true AND s.expiresAt > :now")
    Page<UserSession> findActiveSessions(@Param("now") LocalDateTime now, Pageable pageable);
    
    @Query("SELECT s FROM UserSession s WHERE s.isActive = true AND s.expiresAt <= :now")
    List<UserSession> findExpiredActiveSessions(@Param("now") LocalDateTime now);
    
    // Session Type Queries
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.sessionType = :sessionType ORDER BY s.createdAt DESC")
    List<UserSession> findByUserIdAndSessionType(@Param("userId") String userId, @Param("sessionType") SessionType sessionType);
    
    @Query("SELECT s FROM UserSession s WHERE s.sessionType = :sessionType AND s.isActive = true")
    Page<UserSession> findBySessionType(@Param("sessionType") SessionType sessionType, Pageable pageable);
    
    // Device and Location Queries
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.device = :device ORDER BY s.createdAt DESC")
    List<UserSession> findByUserIdAndDevice(@Param("userId") String userId, @Param("device") String device);
    
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.ipAddress = :ipAddress ORDER BY s.createdAt DESC")
    List<UserSession> findByUserIdAndIpAddress(@Param("userId") String userId, @Param("ipAddress") String ipAddress);
    
    @Query("SELECT s FROM UserSession s WHERE s.ipAddress = :ipAddress AND s.isActive = true")
    List<UserSession> findActiveSessionsByIpAddress(@Param("ipAddress") String ipAddress);
    
    // Recent Activity Queries
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.lastAccessedAt >= :since ORDER BY s.lastAccessedAt DESC")
    List<UserSession> findRecentSessionsByUserId(@Param("userId") String userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT s FROM UserSession s WHERE s.lastAccessedAt >= :since AND s.isActive = true ORDER BY s.lastAccessedAt DESC")
    Page<UserSession> findRecentActiveSessions(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Customer vs Admin Session Queries
    @Query("SELECT s FROM UserSession s WHERE s.user.userRole = 'CUSTOMER' AND s.isActive = true")
    Page<UserSession> findActiveCustomerSessions(Pageable pageable);
    
    @Query("SELECT s FROM UserSession s WHERE s.user.userRole != 'CUSTOMER' AND s.isActive = true")
    Page<UserSession> findActiveAdminSessions(Pageable pageable);
    
    // Statistics Queries
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.id = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true AND s.expiresAt > :now")
    long countActiveSessionsByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.isActive = true AND s.expiresAt > :now")
    long countActiveSessions(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.sessionType = :sessionType AND s.isActive = true")
    long countBySessionType(@Param("sessionType") SessionType sessionType);
    
    @Query("SELECT s.sessionType, COUNT(s) FROM UserSession s WHERE s.isActive = true GROUP BY s.sessionType")
    List<Object[]> getSessionCountByType();
    
    @Query("SELECT COUNT(DISTINCT s.user.id) FROM UserSession s WHERE s.isActive = true AND s.expiresAt > :now")
    long countActiveUsers(@Param("now") LocalDateTime now);
    
    // Time-based Statistics
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate")
    long countSessionsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT DATE(s.createdAt), COUNT(s) FROM UserSession s WHERE s.createdAt >= :startDate GROUP BY DATE(s.createdAt) ORDER BY DATE(s.createdAt)")
    List<Object[]> getSessionCountByDate(@Param("startDate") LocalDateTime startDate);
    
    // Update Queries
    @Modifying
    @Query("UPDATE UserSession s SET s.lastAccessedAt = :accessTime WHERE s.id = :sessionId")
    void updateLastAccessedAt(@Param("sessionId") String sessionId, @Param("accessTime") LocalDateTime accessTime);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.loggedOutAt = :logoutTime WHERE s.id = :sessionId")
    void logoutSession(@Param("sessionId") String sessionId, @Param("logoutTime") LocalDateTime logoutTime);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.loggedOutAt = :logoutTime WHERE s.user.id = :userId AND s.isActive = true")
    void logoutAllUserSessions(@Param("userId") String userId, @Param("logoutTime") LocalDateTime logoutTime);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.loggedOutAt = :logoutTime WHERE s.user.id = :userId AND s.id != :excludeSessionId AND s.isActive = true")
    void logoutOtherUserSessions(@Param("userId") String userId, @Param("excludeSessionId") String excludeSessionId, @Param("logoutTime") LocalDateTime logoutTime);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expiresAt <= :now AND s.isActive = true")
    void deactivateExpiredSessions(@Param("now") LocalDateTime now);
    
    // Cleanup Queries
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt <= :cutoffDate AND s.isActive = false")
    int deleteExpiredSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.createdAt <= :cutoffDate AND s.isActive = false")
    int deleteOldInactiveSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Security Queries
    @Query("SELECT s FROM UserSession s WHERE s.ipAddress = :ipAddress AND s.createdAt >= :since")
    List<UserSession> findRecentSessionsByIpAddress(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(DISTINCT s.ipAddress) FROM UserSession s WHERE s.user.id = :userId AND s.createdAt >= :since")
    long countDistinctIpAddressesByUserId(@Param("userId") String userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT s.ipAddress, COUNT(s) FROM UserSession s WHERE s.createdAt >= :since GROUP BY s.ipAddress HAVING COUNT(s) > :threshold ORDER BY COUNT(s) DESC")
    List<Object[]> findSuspiciousIpAddresses(@Param("since") LocalDateTime since, @Param("threshold") long threshold);
}
