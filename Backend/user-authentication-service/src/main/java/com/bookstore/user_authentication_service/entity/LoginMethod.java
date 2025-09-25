package com.bookstore.user_authentication_service.entity;

import lombok.Getter;

@Getter
public enum LoginMethod {
    PASSWORD("Password", "Username/email and password login"),
    TWO_FACTOR("Two Factor", "Two-factor authentication login"),
    SOCIAL_GOOGLE("Google", "Google OAuth login"),
    SOCIAL_FACEBOOK("Facebook", "Facebook OAuth login"),
    SSO("Single Sign-On", "SSO authentication");
    
    private final String displayName;
    private final String description;
    
    LoginMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
