package com.university.cartmanagement.exception;

/**
 * Exception thrown when a user attempts to add or order more stock than is available.
 */
public class InsufficientStockException extends CartException {
    private static final long serialVersionUID = 1L;

    private final String productName;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for '%s'. Requested: %d, Available: %d",
                productName, requestedQuantity, availableQuantity));
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
