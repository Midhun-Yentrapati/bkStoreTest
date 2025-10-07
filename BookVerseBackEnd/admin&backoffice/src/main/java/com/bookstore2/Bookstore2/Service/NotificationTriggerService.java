package com.bookstore2.Bookstore2.Service;

import com.bookstore2.Bookstore2.Models.AdminNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationTriggerService {

    @Autowired
    private NotificationService notificationService;

    public void createLowStockNotification(String bookId, String bookTitle, int currentStock) {
        try {
            notificationService.createNotification(
                "low_stock",
                "Low Stock Alert",
                String.format("Book \"%s\" (ID: %s) has only %d units remaining", bookTitle, bookId, currentStock),
                "high",
                String.format("{\"bookId\":\"%s\",\"bookTitle\":\"%s\",\"currentStock\":%d}", bookId, bookTitle, currentStock)
            );
        } catch (Exception e) {
            System.err.println("Failed to create low stock notification: " + e.getMessage());
        }
    }

    public void createOrderNotification(String orderId, String customerName, double totalAmount) {
        try {
            notificationService.createNotification(
                "order",
                "New Order Received",
                String.format("New order #%s from %s for ₹%.2f", orderId.substring(0, Math.min(8, orderId.length())), customerName, totalAmount),
                "medium",
                String.format("{\"orderId\":\"%s\",\"customerName\":\"%s\",\"totalAmount\":%.2f}", orderId, customerName, totalAmount)
            );
        } catch (Exception e) {
            System.err.println("Failed to create order notification: " + e.getMessage());
        }
    }
}