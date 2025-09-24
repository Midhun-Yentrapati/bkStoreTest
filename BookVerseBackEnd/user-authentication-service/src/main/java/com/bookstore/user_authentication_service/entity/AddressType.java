package com.bookstore.user_authentication_service.entity;

import lombok.Getter;

@Getter
public enum AddressType {
    HOME("Home", "Home address"),
    WORK("Work", "Work/Office address"),
    OTHER("Other", "Other address type");
    
    private final String displayName;
    private final String description;
    
    AddressType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
