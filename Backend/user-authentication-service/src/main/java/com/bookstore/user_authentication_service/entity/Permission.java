package com.bookstore.user_authentication_service.entity;

import lombok.Getter;

@Getter
public enum Permission {
    // User Management Permissions
    USER_CREATE("Create users", "Can create new user accounts"),
    USER_READ("Read users", "Can view user information"),
    USER_UPDATE("Update users", "Can modify user accounts"),
    USER_DELETE("Delete users", "Can delete user accounts"),
    USER_MANAGE("Manage users", "Full user management access"),
    
    // Book Management Permissions
    BOOK_CREATE("Create books", "Can add new books to catalog"),
    BOOK_READ("Read books", "Can view book information"),
    BOOK_UPDATE("Update books", "Can modify book details"),
    BOOK_DELETE("Delete books", "Can remove books from catalog"),
    BOOK_MANAGE("Manage books", "Full book management access"),
    
    // Order Management Permissions
    ORDER_CREATE("Create orders", "Can create new orders"),
    ORDER_READ("Read orders", "Can view order information"),
    ORDER_UPDATE("Update orders", "Can modify order status"),
    ORDER_DELETE("Delete orders", "Can cancel/delete orders"),
    ORDER_MANAGE("Manage orders", "Full order management access"),
    
    // Review Management Permissions
    REVIEW_CREATE("Create reviews", "Can create book reviews"),
    REVIEW_READ("Read reviews", "Can view reviews"),
    REVIEW_UPDATE("Update reviews", "Can modify reviews"),
    REVIEW_DELETE("Delete reviews", "Can remove reviews"),
    REVIEW_MODERATE("Moderate reviews", "Can moderate and approve reviews"),
    
    // Administrative Permissions
    ADMIN_MANAGE("Admin management", "Can manage admin users and settings"),
    ANALYTICS_VIEW("View analytics", "Can access analytics and reports"),
    SYSTEM_CONFIG("System configuration", "Can modify system settings"),
    AUDIT_LOG("Audit logs", "Can view audit logs and system activities"),
    
    // Financial Permissions
    PAYMENT_MANAGE("Manage payments", "Can handle payment processing"),
    REFUND_PROCESS("Process refunds", "Can process customer refunds"),
    FINANCIAL_REPORTS("Financial reports", "Can access financial reports"),
    
    // Content Management Permissions
    CONTENT_MANAGE("Manage content", "Can manage website content"),
    CATEGORY_MANAGE("Manage categories", "Can manage book categories"),
    PROMOTION_MANAGE("Manage promotions", "Can create and manage promotions");
    
    private final String displayName;
    private final String description;
    
    Permission(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getCategory() {
        String name = this.name();
        if (name.startsWith("USER_")) return "User Management";
        if (name.startsWith("BOOK_")) return "Book Management";
        if (name.startsWith("ORDER_")) return "Order Management";
        if (name.startsWith("REVIEW_")) return "Review Management";
        if (name.startsWith("PAYMENT_") || name.startsWith("REFUND_") || name.startsWith("FINANCIAL_")) return "Financial";
        if (name.startsWith("CONTENT_") || name.startsWith("CATEGORY_") || name.startsWith("PROMOTION_")) return "Content Management";
        return "Administrative";
    }
}
