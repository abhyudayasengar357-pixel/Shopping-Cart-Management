package com.university.cartmanagement.repository.impl;

import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.repository.ProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Spring JDBC implementation of ProductRepository using JdbcTemplate and RowMapper.
 * Annotated with @Primary to designate it as the default implementation for the application.
 */
@Primary
@Repository("springJdbcProductRepository")
public class SpringJdbcProductRepository implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public SpringJdbcProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setImageUrl(rs.getString("image_url"));
        p.setFeatured(rs.getBoolean("featured"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) p.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) p.setUpdatedAt(updatedAt.toLocalDateTime());

        return p;
    };

    private static final String BASE_SELECT =
            "SELECT p.id, p.name, p.description, p.price, p.stock_quantity, p.category_id, " +
            "c.name AS category_name, p.image_url, p.featured, p.created_at, p.updated_at " +
            "FROM products p " +
            "LEFT JOIN categories c ON p.category_id = c.id ";

    @Override
    public Optional<Product> findById(Long id) {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql, productRowMapper, id);
            return Optional.ofNullable(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = BASE_SELECT + "ORDER BY p.id ASC";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    @Override
    public List<Product> findFeatured() {
        String sql = BASE_SELECT + "WHERE p.featured = true ORDER BY p.id ASC";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        String sql = BASE_SELECT + "WHERE p.category_id = ? ORDER BY p.name ASC";
        return jdbcTemplate.query(sql, productRowMapper, categoryId);
    }

    // Method Overloading 1: Basic keyword search
    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = BASE_SELECT +
                "WHERE LOWER(p.name) LIKE LOWER(?) OR LOWER(p.description) LIKE LOWER(?) OR LOWER(c.name) LIKE LOWER(?) " +
                "ORDER BY p.name ASC";
        String pattern = "%" + keyword.trim() + "%";
        return jdbcTemplate.query(sql, productRowMapper, pattern, pattern, pattern);
    }

    // Method Overloading 2: Multi-criteria faceted search and filtering
    @Override
    public List<Product> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE LOWER(?) OR LOWER(p.description) LIKE LOWER(?)) ");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (minPrice != null) {
            sql.append("AND p.price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND p.price <= ? ");
            params.add(maxPrice);
        }

        // Sorting
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.price ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.price DESC ");
        } else if ("name_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.name ASC ");
        } else if ("name_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.name DESC ");
        } else {
            sql.append("ORDER BY p.featured DESC, p.id ASC ");
        }

        return jdbcTemplate.query(sql.toString(), productRowMapper, params.toArray());
    }

    @Override
    public Product save(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, featured, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity() != null ? product.getStockQuantity() : 0);
            ps.setLong(5, product.getCategoryId());
            ps.setString(6, product.getImageUrl());
            ps.setBoolean(7, product.isFeatured());
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
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
            product.setId(newId);
        }
        return product;
    }

    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_quantity = ?, " +
                     "category_id = ?, image_url = ?, featured = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategoryId(),
                product.getImageUrl(),
                product.isFeatured(),
                Timestamp.valueOf(LocalDateTime.now()),
                product.getId()
        );
    }

    @Override
    public void updateStock(Long productId, int newStock) {
        String sql = "UPDATE products SET stock_quantity = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, newStock, Timestamp.valueOf(LocalDateTime.now()), productId);
    }

    @Override
    public void reduceStock(Long productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ?, updated_at = ? WHERE id = ? AND stock_quantity >= ?";
        int rowsUpdated = jdbcTemplate.update(sql, quantity, Timestamp.valueOf(LocalDateTime.now()), productId, quantity);
        if (rowsUpdated == 0) {
            throw new IllegalStateException("Failed to decrement stock for product ID: " + productId + ". Insufficient stock available.");
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        return count != null ? count : 0;
    }
}
