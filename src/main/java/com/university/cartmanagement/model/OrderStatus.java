package com.university.cartmanagement.model;

/**
 * Enumeration representing order lifecycle states.
 */
public enum OrderStatus {
    PENDING("Pending", "warning"),
    PROCESSING("Processing", "info"),
    SHIPPED("Shipped", "primary"),
    DELIVERED("Delivered", "success"),
    CANCELLED("Cancelled", "danger");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
