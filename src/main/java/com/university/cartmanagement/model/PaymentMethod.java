package com.university.cartmanagement.model;

/**
 * Enumeration representing available payment channels.
 */
public enum PaymentMethod {
    CARD("Credit / Debit Card", "bi-credit-card"),
    UPI("UPI / Instant Transfer", "bi-qr-code-scan"),
    CASH_ON_DELIVERY("Cash on Delivery (COD)", "bi-cash-coin");

    private final String displayName;
    private final String icon;

    PaymentMethod(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
