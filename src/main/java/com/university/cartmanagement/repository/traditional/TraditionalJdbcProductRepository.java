package com.university.cartmanagement.repository.traditional;

import com.university.cartmanagement.exception.DatabaseOperationException;
import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Traditional JDBC Implementation of ProductRepository.
 * Explicitly demonstrates the use of:
 *  - java.sql.DriverManager
 *  - java.sql.Connection
 *  - java.sql.PreparedStatement
 *  - java.sql.ResultSet
 *  - Manual try-with-resources resource management
 *  - Checked SQLException handling and manual exception translation
 *
 * This serves as an educational benchmark and direct comparison against Spring JDBC (JdbcTemplate).
 */
@Repository("traditionalJdbcProductRepository")
public class TraditionalJdbcProductRepository implements ProductRepository {

    private static final Logger log = LoggerFactory.getLogger(TraditionalJdbcProductRepository.class);

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String dbDriver;

    public TraditionalJdbcProductRepository(
            @Value("${app.traditional.jdbc.url:${spring.datasource.url}}") String dbUrl,
            @Value("${app.traditional.jdbc.username:${spring.datasource.username}}") String dbUser,
            @Value("${app.traditional.jdbc.password:${spring.datasource.password:}}") String dbPassword,
            @Value("${app.traditional.jdbc.driver:${spring.datasource.driver-class-name:org.h2.Driver}}") String dbDriver) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbDriver = dbDriver;

        try {
            Class.forName(this.dbDriver);
        } catch (ClassNotFoundException e) {
            log.warn("JDBC Driver class not found: {}", this.dbDriver);
        }
    }

    /**
     * Helper to obtain a raw JDBC Connection via DriverManager.
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Helper to map a raw ResultSet row to a Product entity manually.
     */
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
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

        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) p.setCreatedAt(cat.toLocalDateTime());

        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) p.setUpdatedAt(uat.toLocalDateTime());

        return p;
    }

    private static final String BASE_SELECT =
            "SELECT p.id, p.name, p.description, p.price, p.stock_quantity, p.category_id, " +
            "c.name AS category_name, p.image_url, p.featured, p.created_at, p.updated_at " +
            "FROM products p " +
            "LEFT JOIN categories c ON p.category_id = c.id ";

    @Override
    public Optional<Product> findById(Long id) {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProduct(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in findById", sql, e);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = BASE_SELECT + "ORDER BY p.id ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToProduct(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in findAll", sql, e);
        }
    }

    @Override
    public List<Product> findFeatured() {
        String sql = BASE_SELECT + "WHERE p.featured = true ORDER BY p.id ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToProduct(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in findFeatured", sql, e);
        }
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        String sql = BASE_SELECT + "WHERE p.category_id = ? ORDER BY p.name ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in findByCategoryId", sql, e);
        }
    }

    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = BASE_SELECT +
                "WHERE LOWER(p.name) LIKE LOWER(?) OR LOWER(p.description) LIKE LOWER(?) OR LOWER(c.name) LIKE LOWER(?) " +
                "ORDER BY p.name ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in search", sql, e);
        }
    }

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

        List<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToProduct(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in faceted search", sql.toString(), e);
        }
    }

    @Override
    public Product save(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, featured, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity() != null ? product.getStockQuantity() : 0);
            ps.setLong(5, product.getCategoryId());
            ps.setString(6, product.getImageUrl());
            ps.setBoolean(7, product.isFeatured());
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getLong(1));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in save", sql, e);
        }
    }

    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_quantity = ?, " +
                     "category_id = ?, image_url = ?, featured = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setLong(5, product.getCategoryId());
            ps.setString(6, product.getImageUrl());
            ps.setBoolean(7, product.isFeatured());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(9, product.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in update", sql, e);
        }
    }

    @Override
    public void updateStock(Long productId, int newStock) {
        String sql = "UPDATE products SET stock_quantity = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newStock);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in updateStock", sql, e);
        }
    }

    @Override
    public void reduceStock(Long productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ?, updated_at = ? WHERE id = ? AND stock_quantity >= ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, productId);
            ps.setInt(4, quantity);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalStateException("Traditional JDBC: Insufficient inventory to deduct for product ID: " + productId);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in reduceStock", sql, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in deleteById", sql, e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Traditional JDBC error in count", sql, e);
        }
    }
}
