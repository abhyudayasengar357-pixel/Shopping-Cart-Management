package com.university.cartmanagement.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Base User entity representing all system users.
 * Demonstrates: Encapsulation, Constructor Overloading, and Inheritance root.
 */
public class User extends BaseEntity {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;
    private String address;
    private Role role;

    // Default Constructor
    public User() {
        super();
        this.role = Role.CUSTOMER;
    }

    // Parameterized Constructor 1: ID only
    public User(Long id) {
        super(id);
        this.role = Role.CUSTOMER;
    }

    // Parameterized Constructor 2: Core credentials (Constructor Chaining)
    public User(String username, String email, String password, String fullName) {
        this(null, username, email, password, fullName, null, null, Role.CUSTOMER);
    }

    // Parameterized Constructor 3: Core credentials + Role
    public User(String username, String email, String password, String fullName, Role role) {
        this(null, username, email, password, fullName, null, null, role);
    }

    // Parameterized Constructor 4: Full Constructor
    public User(Long id, String username, String email, String password, String fullName,
                String phone, String address, Role role) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.role = role != null ? role : Role.CUSTOMER;
    }

    // Full constructor with timestamps
    public User(Long id, String username, String email, String password, String fullName,
                String phone, String address, Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.role = role != null ? role : Role.CUSTOMER;
    }

    // Business Methods demonstrating Polymorphic capability
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isCustomer() {
        return Role.CUSTOMER.equals(this.role);
    }

    public String getRoleDescription() {
        return "Standard User with role: " + this.role.getDisplayName();
    }

    // Getters and Setters (Encapsulation)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                '}';
    }
}
