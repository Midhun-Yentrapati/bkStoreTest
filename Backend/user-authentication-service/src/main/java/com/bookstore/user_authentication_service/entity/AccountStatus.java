package com.bookstore.user_authentication_service.entity;

import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE("Active", "Account is active and can be used"),
    INACTIVE("Inactive", "Account is temporarily inactive"),
    SUSPENDED("Suspended", "Account is suspended due to policy violation"),
    LOCKED("Locked", "Account is locked due to security reasons"),
    PENDING_VERIFICATION("Pending Verification", "Account is pending email/mobile verification"),
    DELETED("Deleted", "Account has been deleted");
    
    private final String displayName;
    private final String description;
    
    AccountStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public boolean canLogin() {
        return this == ACTIVE;
    }
    
    public boolean isTemporary() {
        return this == INACTIVE || this == LOCKED || this == PENDING_VERIFICATION;
    }
    
    public boolean isPermanent() {
        return this == SUSPENDED || this == DELETED;
    }
}
