package com.bookstore2.Bookstore2.Controller;

import com.bookstore2.Bookstore2.Models.AdminNotification;
import com.bookstore2.Bookstore2.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<AdminNotification> createNotification(@RequestBody Map<String, Object> notificationData) {
        try {
            AdminNotification notification = notificationService.createNotification(
                (String) notificationData.get("type"),
                (String) notificationData.get("title"),
                (String) notificationData.get("message"),
                (String) notificationData.get("priority"),
                notificationData.get("data")
            );
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<AdminNotification>> getAllNotifications() {
        List<AdminNotification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminNotification> getNotificationById(@PathVariable String id) {
        AdminNotification notification = notificationService.getNotificationById(id);
        return notification != null ? ResponseEntity.ok(notification) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<AdminNotification> markAsRead(@PathVariable String id) {
        AdminNotification notification = notificationService.markAsRead(id);
        return notification != null ? ResponseEntity.ok(notification) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        boolean deleted = notificationService.deleteNotification(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAllNotifications() {
        notificationService.clearAllNotifications();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
} 