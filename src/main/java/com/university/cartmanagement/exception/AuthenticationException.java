package com.university.cartmanagement.exception;

/**
 * Exception thrown for authentication and authorization failures.
 */
public class AuthenticationException extends CartException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
