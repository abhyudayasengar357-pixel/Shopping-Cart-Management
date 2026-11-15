package com.university.cartmanagement.model;

/**
 * Customer entity demonstrating OOP Inheritance and Polymorphism.
 * Extends the User class with customer-specific attributes and behaviors.
 */
public class Customer extends User {

    private int loyaltyPoints;
    private boolean vipStatus;

    public Customer() {
        super();
        setRole(Role.CUSTOMER);
        this.loyaltyPoints = 0;
        this.vipStatus = false;
    }

    public Customer(String username, String email, String password, String fullName) {
        super(username, email, password, fullName, Role.CUSTOMER);
        this.loyaltyPoints = 0;
        this.vipStatus = false;
    }

    public Customer(User user) {
        super(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(),
                user.getFullName(), user.getPhone(), user.getAddress(), Role.CUSTOMER,
                user.getCreatedAt(), user.getUpdatedAt());
        this.loyaltyPoints = 0;
        this.vipStatus = false;
    }

    // Overriding method to demonstrate Runtime Polymorphism
    @Override
    public String getRoleDescription() {
        return "Retail Customer (VIP: " + (vipStatus ? "Yes" : "Standard") + ", Loyalty: " + loyaltyPoints + " pts)";
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
        if (this.loyaltyPoints >= 500) {
            this.vipStatus = true;
        }
    }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
        if (this.loyaltyPoints >= 500) {
            this.vipStatus = true;
        }
    }

    public boolean isVipStatus() {
        return vipStatus;
    }

    public void setVipStatus(boolean vipStatus) {
        this.vipStatus = vipStatus;
    }
}
