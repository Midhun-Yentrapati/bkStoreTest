package com.bookstore2.Bookstore2.Repository;

import com.bookstore2.Bookstore2.Models.AdminActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, String> {

    List<AdminActivityLog> findAllByOrderByCreatedAtDesc();
    List<AdminActivityLog> findByAdminIdOrderByCreatedAtDesc(String adminId);
    Page<AdminActivityLog> findByAdminIdOrderByCreatedAtDesc(String adminId, Pageable pageable);
    List<AdminActivityLog> findByActionTypeOrderByCreatedAtDesc(String actionType);
    List<AdminActivityLog> findByStatusOrderByCreatedAtDesc(String status);
    List<AdminActivityLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT a FROM AdminActivityLog a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AdminActivityLog> findRecentActivities(@Param("since") LocalDateTime since);

    @Query("SELECT a.adminId, a.adminUsername, COUNT(a) as activityCount " +
           "FROM AdminActivityLog a " +
           "WHERE a.createdAt >= :since " +
           "GROUP BY a.adminId, a.adminUsername " +
           "ORDER BY activityCount DESC")
    List<Object[]> findTopActiveAdmins(@Param("since") LocalDateTime since);

    @Query("SELECT a.actionType, COUNT(a) as actionCount " +
           "FROM AdminActivityLog a " +
           "WHERE a.createdAt >= :since " +
           "GROUP BY a.actionType " +
           "ORDER BY actionCount DESC")
    List<Object[]> findMostCommonActions(@Param("since") LocalDateTime since);

    @Query("SELECT a FROM AdminActivityLog a WHERE a.errorMessage IS NOT NULL ORDER BY a.createdAt DESC")
    List<AdminActivityLog> findLogsWithErrors();

    @Query("SELECT a FROM AdminActivityLog a WHERE a.executionTimeMs >= :minDuration ORDER BY a.executionTimeMs DESC")
    List<AdminActivityLog> findSlowOperations(@Param("minDuration") Long minDuration);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByStatus(String status);
    
    @Query("SELECT AVG(a.executionTimeMs) FROM AdminActivityLog a")
    Double findAverageExecutionTime();
} 