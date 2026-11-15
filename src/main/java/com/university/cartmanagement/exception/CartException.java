package com.university.cartmanagement.exception;

/**
 * Base custom unchecked exception for the online cart application.
 * Demonstrates: OOP Exception Hierarchy.
 */
public class CartException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }
}
