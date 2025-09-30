package com.bookstore2.Bookstore2.Repository;

import com.bookstore2.Bookstore2.Models.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, String> {
    
    List<AdminNotification> findAllByOrderByCreatedAtDesc();
    
    List<AdminNotification> findByIsReadFalseOrderByCreatedAtDesc();
    
    List<AdminNotification> findByTypeOrderByCreatedAtDesc(String type);
    
    List<AdminNotification> findByPriorityOrderByCreatedAtDesc(String priority);
    
    List<AdminNotification> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(n) FROM AdminNotification n WHERE n.isRead = false")
    long countUnreadNotifications();
    
    void deleteByIsReadTrue();
} 