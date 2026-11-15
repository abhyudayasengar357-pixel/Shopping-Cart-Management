package com.university.cartmanagement.service;

import com.university.cartmanagement.exception.AuthenticationException;
import com.university.cartmanagement.exception.ResourceNotFoundException;
import com.university.cartmanagement.model.Admin;
import com.university.cartmanagement.model.Customer;
import com.university.cartmanagement.model.Role;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.repository.UserRepository;
import com.university.cartmanagement.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing user accounts, authentication, and security.
 * Demonstrates: BCrypt Hashing, Encapsulation, and Polymorphism.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    public UserService(UserRepository userRepository, PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.passwordUtil = passwordUtil;
    }

    @Transactional
    public User registerCustomer(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username is already taken: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + user.getEmail());
        }

        // Hash password securely with BCrypt
        user.setPassword(passwordUtil.hashPassword(user.getPassword()));
        user.setRole(Role.CUSTOMER);

        return userRepository.save(user);
    }

    public User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new AuthenticationException("Username and password are required");
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordUtil.verifyPassword(rawPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Return polymorphic subclass instance
        if (user.isAdmin()) {
            return new Admin(user);
        } else {
            return new Customer(user);
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateProfile(User user) {
        getUserById(user.getId());
        userRepository.updateProfile(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);
        if (!passwordUtil.verifyPassword(currentPassword, user.getPassword())) {
            throw new AuthenticationException("Current password does not match");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        userRepository.updatePassword(userId, passwordUtil.hashPassword(newPassword));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getCustomers() {
        return userRepository.findByRole(Role.CUSTOMER);
    }

    public int countUsers() {
        return userRepository.count();
    }
}
