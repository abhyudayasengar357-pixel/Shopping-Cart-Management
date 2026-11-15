package com.university.cartmanagement.model;

/**
 * Admin entity demonstrating OOP Inheritance and Polymorphic behavior.
 * Extends the User class with administrative capabilities.
 */
public class Admin extends User {

    private String department;
    private boolean superAdmin;

    public Admin() {
        super();
        setRole(Role.ADMIN);
        this.department = "Operations";
        this.superAdmin = false;
    }

    public Admin(String username, String email, String password, String fullName) {
        super(username, email, password, fullName, Role.ADMIN);
        this.department = "Operations";
        this.superAdmin = false;
    }

    public Admin(User user) {
        super(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(),
                user.getFullName(), user.getPhone(), user.getAddress(), Role.ADMIN,
                user.getCreatedAt(), user.getUpdatedAt());
        this.department = "Operations";
        this.superAdmin = false;
    }

    // Overriding method to demonstrate Runtime Polymorphism
    @Override
    public String getRoleDescription() {
        return "System Administrator (Department: " + department + (superAdmin ? ", Super Admin" : "") + ")";
    }

    public boolean canManageInventory() {
        return true;
    }

    public boolean canManageOrders() {
        return true;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }
}
