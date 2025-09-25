package com.bookstore.user_authentication_service.entity;

import lombok.Getter;

@Getter
public enum UserType {
    CUSTOMER("Customer", "Regular customer"),
    VENDOR("Vendor", "Book vendor/supplier"),
    ADMIN("Admin", "Administrative user");
    
    private final String displayName;
    private final String description;
    
    UserType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
