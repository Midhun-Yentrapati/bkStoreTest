package com.bookstore.user_authentication_service.entity;

import lombok.Getter;

@Getter
public enum UserRole {
    CUSTOMER("Customer", "Regular customer account"),
    SUPPORT("Support Agent", "Customer support representative"),
    MODERATOR("Moderator", "Content and review moderator"),
    MANAGER("Manager", "Department manager"),
    ADMIN("Administrator", "System administrator"),
    SUPER_ADMIN("Super Administrator", "Super administrator with full access");
    
    private final String displayName;
    private final String description;
    
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public boolean hasAdminPrivileges() {
        return this == ADMIN || this == SUPER_ADMIN || this == MANAGER;
    }
    
    public boolean canManageUsers() {
        return this == SUPER_ADMIN || this == ADMIN || this == MANAGER;
    }
    
    public boolean canAccessAdminPanel() {
        return this != CUSTOMER;
    }
}
