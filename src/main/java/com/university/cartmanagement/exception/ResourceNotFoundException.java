package com.university.cartmanagement.exception;

/**
 * Exception thrown when a requested resource (product, user, order, category) is not found.
 */
public class ResourceNotFoundException extends CartException {
    private static final long serialVersionUID = 1L;

    private final String resourceName;
    private final Object identifier;

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = "Resource";
        this.identifier = null;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Object getIdentifier() {
        return identifier;
    }
}
