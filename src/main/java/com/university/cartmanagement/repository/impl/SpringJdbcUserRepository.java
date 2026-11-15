package com.university.cartmanagement.repository.impl;

import com.university.cartmanagement.model.Role;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.repository.UserRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring JDBC implementation of UserRepository using JdbcTemplate and RowMapper.
 */
@Repository
public class SpringJdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public SpringJdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        String roleStr = rs.getString("role");
        user.setRole(roleStr != null ? Role.valueOf(roleStr) : Role.CUSTOMER);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) user.setCreatedAt(createdAt.toLocalDateTime());

        return user;
    };

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, username, email, password, full_name, phone, address, role, created_at FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, email, password, full_name, phone, address, role, created_at FROM users WHERE LOWER(username) = LOWER(?)";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email, password, full_name, phone, address, role, created_at FROM users WHERE LOWER(email) = LOWER(?)";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, email, password, full_name, phone, address, role, created_at FROM users ORDER BY id ASC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public List<User> findByRole(Role role) {
        String sql = "SELECT id, username, email, password, full_name, phone, address, role, created_at FROM users WHERE role = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, userRowMapper, role.name());
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, email, password, full_name, phone, address, role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getAddress());
            ps.setString(7, user.getRole() != null ? user.getRole().name() : Role.CUSTOMER.name());
            ps.setTimestamp(8, now);
            return ps;
        }, keyHolder);

        Long newId = null;
        if (keyHolder.getKeys() != null) {
            for (java.util.Map.Entry<String, Object> entry : keyHolder.getKeys().entrySet()) {
                if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number num) {
                    newId = num.longValue();
                    break;
                }
            }
        }
        if (newId == null) {
            try {
                newId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
            } catch (Exception ignored) {}
        }
        if (newId != null) {
            user.setId(newId);
        }
        return user;
    }

    @Override
    public void updateProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getFullName(), user.getEmail(), user.getPhone(), user.getAddress(), user.getId());
    }

    @Override
    public void updatePassword(Long userId, String newHashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, newHashedPassword, userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)", Integer.class, username);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)", Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return count != null ? count : 0;
    }
}
