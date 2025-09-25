package com.bookstore.user_authentication_service.entity;

import lombok.Getter;

@Getter
public enum SessionType {
    WEB("Web Browser", "Web browser session"),
    MOBILE("Mobile App", "Mobile application session"),
    API("API Client", "API client session"),
    ADMIN("Admin Panel", "Admin panel session");
    
    private final String displayName;
    private final String description;
    
    SessionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
