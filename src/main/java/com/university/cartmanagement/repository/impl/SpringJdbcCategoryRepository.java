package com.university.cartmanagement.repository.impl;

import com.university.cartmanagement.model.Category;
import com.university.cartmanagement.repository.CategoryRepository;
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
 * Spring JDBC implementation of CategoryRepository using JdbcTemplate and RowMapper.
 */
@Repository
public class SpringJdbcCategoryRepository implements CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public SpringJdbcCategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Category> categoryRowMapper = (rs, rowNum) -> {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setIcon(rs.getString("icon"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            category.setCreatedAt(ts.toLocalDateTime());
        }
        return category;
    };

    @Override
    public Optional<Category> findById(Long id) {
        String sql = "SELECT id, name, description, icon, created_at FROM categories WHERE id = ?";
        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, id);
            return Optional.ofNullable(category);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Category> findByName(String name) {
        String sql = "SELECT id, name, description, icon, created_at FROM categories WHERE LOWER(name) = LOWER(?)";
        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, name);
            return Optional.ofNullable(category);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Category> findAll() {
        // Includes product count for modern category badge display
        String sql = "SELECT c.id, c.name, c.description, c.icon, c.created_at, COUNT(p.id) AS product_count " +
                     "FROM categories c " +
                     "LEFT JOIN products p ON c.id = p.category_id " +
                     "GROUP BY c.id, c.name, c.description, c.icon, c.created_at " +
                     "ORDER BY c.name ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Category cat = categoryRowMapper.mapRow(rs, rowNum);
            if (cat != null) {
                cat.setProductCount(rs.getInt("product_count"));
            }
            return cat;
        });
    }

    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO categories (name, description, icon, created_at) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getIcon() != null ? category.getIcon() : "bi-tag");
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
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
            category.setId(newId);
        }
        return category;
    }

    @Override
    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, icon = ? WHERE id = ?";
        jdbcTemplate.update(sql, category.getName(), category.getDescription(), category.getIcon(), category.getId());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Integer.class);
        return count != null ? count : 0;
    }
}
