package com.bookstore2.Bookstore2.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_notifications")
@Data
@NoArgsConstructor
public class AdminNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String type; // 'order', 'low_stock', 'system', etc.
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "is_read")
    private boolean isRead = false;
    
    @Column(nullable = false)
    private String priority; // 'low', 'medium', 'high'
    
    @Column(columnDefinition = "JSON")
    private String data; // JSON string for additional data
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public AdminNotification(String type, String title, String message, String priority, String data) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.data = data;
        this.isRead = false;
    }
} 