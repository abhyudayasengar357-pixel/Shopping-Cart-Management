package com.university.cartmanagement.repository;

import com.university.cartmanagement.model.Role;
import com.university.cartmanagement.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User management.
 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(Role role);

    User save(User user);

    void updateProfile(User user);

    void updatePassword(Long userId, String newHashedPassword);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    int count();
}
