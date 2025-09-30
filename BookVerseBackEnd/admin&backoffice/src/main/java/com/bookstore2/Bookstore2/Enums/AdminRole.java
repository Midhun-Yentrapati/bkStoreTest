package com.bookstore2.Bookstore2.Enums;

public enum AdminRole {
    SUPER_ADMIN("Super Administrator"),
    ADMIN("Administrator"),
    MANAGER("Manager"),
    MODERATOR("Moderator"),
    SUPPORT("Support");

    private final String displayName;

    AdminRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
