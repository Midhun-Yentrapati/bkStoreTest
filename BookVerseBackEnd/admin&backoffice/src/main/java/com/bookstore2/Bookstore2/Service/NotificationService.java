package com.bookstore2.Bookstore2.Service;

import com.bookstore2.Bookstore2.Models.AdminNotification;
import com.bookstore2.Bookstore2.Repository.AdminNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private AdminNotificationRepository notificationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminNotification createNotification(String type, String title, String message, String priority, Object data) {
        String dataJson = null;
        if (data != null) {
            try {
                dataJson = objectMapper.writeValueAsString(data);
            } catch (Exception e) {
                // Log error but continue
                System.err.println("Error serializing notification data: " + e.getMessage());
            }
        }
        
        AdminNotification notification = new AdminNotification(
            type != null ? type : "system",
            title,
            message,
            priority != null ? priority : "low",
            dataJson
        );
        
        return notificationRepository.save(notification);
    }

    public List<AdminNotification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public AdminNotification getNotificationById(String id) {
        Optional<AdminNotification> notification = notificationRepository.findById(id);
        return notification.orElse(null);
    }

    public AdminNotification markAsRead(String id) {
        Optional<AdminNotification> optionalNotification = notificationRepository.findById(id);
        if (optionalNotification.isPresent()) {
            AdminNotification notification = optionalNotification.get();
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        return null;
    }

    public boolean deleteNotification(String id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public void clearAllNotifications() {
        notificationRepository.deleteAll();
    }

    public long getUnreadCount() {
        return notificationRepository.countUnreadNotifications();
    }

    public List<AdminNotification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    public List<AdminNotification> getNotificationsByType(String type) {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    public List<AdminNotification> getNotificationsByPriority(String priority) {
        return notificationRepository.findByPriorityOrderByCreatedAtDesc(priority);
    }

    public List<AdminNotification> getNotificationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return notificationRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    @Transactional
    public void clearReadNotifications() {
        notificationRepository.deleteByIsReadTrue();
    }
} 