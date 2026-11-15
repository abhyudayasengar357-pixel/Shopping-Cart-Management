package com.university.cartmanagement.repository.impl;

import com.university.cartmanagement.model.Cart;
import com.university.cartmanagement.model.CartItem;
import com.university.cartmanagement.repository.CartRepository;
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
import java.util.List;
import java.util.Optional;

/**
 * Spring JDBC implementation of CartRepository using JdbcTemplate.
 */
@Repository
public class SpringJdbcCartRepository implements CartRepository {

    private final JdbcTemplate jdbcTemplate;

    public SpringJdbcCartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CartItem> cartItemRowMapper = (rs, rowNum) -> {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setCartId(rs.getLong("cart_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setProductImageUrl(rs.getString("image_url"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setStockAvailable(rs.getInt("stock_quantity"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) item.setCreatedAt(ts.toLocalDateTime());

        return item;
    };

    @Override
    public Cart findOrCreateByUserId(Long userId) {
        Optional<Cart> existing = findByUserId(userId);
        if (existing.isPresent()) {
            Cart cart = existing.get();
            cart.setItems(findItemsByCartId(cart.getId()));
            return cart;
        }

        // Create new cart for user
        String sql = "INSERT INTO cart (user_id, created_at, updated_at) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, userId);
            ps.setTimestamp(2, now);
            ps.setTimestamp(3, now);
            return ps;
        }, keyHolder);

        Long cartId = null;
        if (keyHolder.getKeys() != null) {
            for (java.util.Map.Entry<String, Object> entry : keyHolder.getKeys().entrySet()) {
                if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number num) {
                    cartId = num.longValue();
                    break;
                }
            }
        }
        if (cartId == null) {
            try {
                cartId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
            } catch (Exception ignored) {}
        }

        Cart newCart = new Cart(cartId, userId);
        return newCart;
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, created_at, updated_at FROM cart WHERE user_id = ?";
        try {
            Cart cart = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Cart c = new Cart();
                c.setId(rs.getLong("id"));
                c.setUserId(rs.getLong("user_id"));
                Timestamp cat = rs.getTimestamp("created_at");
                if (cat != null) c.setCreatedAt(cat.toLocalDateTime());
                Timestamp uat = rs.getTimestamp("updated_at");
                if (uat != null) c.setUpdatedAt(uat.toLocalDateTime());
                return c;
            }, userId);
            return Optional.ofNullable(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<CartItem> findItemsByCartId(Long cartId) {
        String sql = "SELECT ci.id, ci.cart_id, ci.product_id, ci.quantity, ci.unit_price, ci.created_at, " +
                     "p.name AS product_name, p.image_url, p.stock_quantity " +
                     "FROM cart_items ci " +
                     "JOIN products p ON ci.product_id = p.id " +
                     "WHERE ci.cart_id = ? ORDER BY ci.id ASC";
        return jdbcTemplate.query(sql, cartItemRowMapper, cartId);
    }

    @Override
    public void addItem(Long cartId, Long productId, int quantity, BigDecimal unitPrice) {
        // Check if product already exists in cart
        String checkSql = "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
        List<CartItem> existing = jdbcTemplate.query(checkSql, (rs, rowNum) -> {
            CartItem ci = new CartItem();
            ci.setId(rs.getLong("id"));
            ci.setQuantity(rs.getInt("quantity"));
            return ci;
        }, cartId, productId);

        if (!existing.isEmpty()) {
            int newQuantity = existing.get(0).getQuantity() + quantity;
            String updateSql = "UPDATE cart_items SET quantity = ?, unit_price = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, newQuantity, unitPrice, existing.get(0).getId());
        } else {
            String insertSql = "INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, created_at) " +
                               "VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, cartId, productId, quantity, unitPrice, Timestamp.valueOf(LocalDateTime.now()));
        }

        // Update cart timestamp
        jdbcTemplate.update("UPDATE cart SET updated_at = ? WHERE id = ?", Timestamp.valueOf(LocalDateTime.now()), cartId);
    }

    @Override
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        if (quantity <= 0) {
            removeItem(cartId, productId);
        } else {
            String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?";
            jdbcTemplate.update(sql, quantity, cartId, productId);
            jdbcTemplate.update("UPDATE cart SET updated_at = ? WHERE id = ?", Timestamp.valueOf(LocalDateTime.now()), cartId);
        }
    }

    @Override
    public void removeItem(Long cartId, Long productId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        jdbcTemplate.update(sql, cartId, productId);
        jdbcTemplate.update("UPDATE cart SET updated_at = ? WHERE id = ?", Timestamp.valueOf(LocalDateTime.now()), cartId);
    }

    @Override
    public void clearCart(Long cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        jdbcTemplate.update(sql, cartId);
        jdbcTemplate.update("UPDATE cart SET updated_at = ? WHERE id = ?", Timestamp.valueOf(LocalDateTime.now()), cartId);
    }

    @Override
    public int countItemsInCart(Long cartId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE cart_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cartId);
        return count != null ? count : 0;
    }
}
