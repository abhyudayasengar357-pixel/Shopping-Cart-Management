package com.university.cartmanagement.model;

/**
 * Enumeration representing user security roles.
 */
public enum Role {
    CUSTOMER("Customer"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
