package com.university.cartmanagement.repository.impl;

import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.OrderItem;
import com.university.cartmanagement.model.OrderStatus;
import com.university.cartmanagement.model.PaymentMethod;
import com.university.cartmanagement.repository.OrderRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring JDBC implementation of OrderRepository using JdbcTemplate and batch updates.
 */
@Repository
public class SpringJdbcOrderRepository implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public SpringJdbcOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setUserId(rs.getLong("user_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerEmail(rs.getString("customer_email"));
        order.setTotalItems(rs.getInt("total_items"));
        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        order.setTaxAmount(rs.getBigDecimal("tax_amount"));
        order.setShippingFee(rs.getBigDecimal("shipping_fee"));
        order.setFinalAmount(rs.getBigDecimal("final_amount"));
        order.setDiscountCode(rs.getString("discount_code"));

        String statusStr = rs.getString("order_status");
        if (statusStr != null) order.setOrderStatus(OrderStatus.valueOf(statusStr));

        String payMethodStr = rs.getString("payment_method");
        if (payMethodStr != null) order.setPaymentMethod(PaymentMethod.valueOf(payMethodStr));

        order.setPaymentStatus(rs.getString("payment_status"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setCustomerNotes(rs.getString("customer_notes"));

        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) order.setCreatedAt(cat.toLocalDateTime());

        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) order.setUpdatedAt(uat.toLocalDateTime());

        return order;
    };

    private final RowMapper<OrderItem> orderItemRowMapper = (rs, rowNum) -> {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setTotalPrice(rs.getBigDecimal("total_price"));
        return item;
    };

    private static final String BASE_ORDER_SELECT =
            "SELECT o.id, o.order_number, o.user_id, u.full_name AS customer_name, u.email AS customer_email, " +
            "o.total_items, o.subtotal, o.discount_amount, o.tax_amount, o.shipping_fee, o.final_amount, " +
            "o.discount_code, o.order_status, o.payment_method, o.payment_status, o.shipping_address, " +
            "o.customer_notes, o.created_at, o.updated_at " +
            "FROM orders o " +
            "JOIN users u ON o.user_id = u.id ";

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO orders (order_number, user_id, total_items, subtotal, discount_amount, " +
                     "tax_amount, shipping_fee, final_amount, discount_code, order_status, payment_method, " +
                     "payment_status, shipping_address, customer_notes, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, order.getOrderNumber());
            ps.setLong(2, order.getUserId());
            ps.setInt(3, order.getTotalItems());
            ps.setBigDecimal(4, order.getSubtotal());
            ps.setBigDecimal(5, order.getDiscountAmount());
            ps.setBigDecimal(6, order.getTaxAmount());
            ps.setBigDecimal(7, order.getShippingFee());
            ps.setBigDecimal(8, order.getFinalAmount());
            ps.setString(9, order.getDiscountCode());
            ps.setString(10, order.getOrderStatus() != null ? order.getOrderStatus().name() : OrderStatus.PENDING.name());
            ps.setString(11, order.getPaymentMethod() != null ? order.getPaymentMethod().name() : PaymentMethod.CARD.name());
            ps.setString(12, order.getPaymentStatus());
            ps.setString(13, order.getShippingAddress());
            ps.setString(14, order.getCustomerNotes());
            ps.setTimestamp(15, now);
            ps.setTimestamp(16, now);
            return ps;
        }, keyHolder);

        Number key = extractGeneratedId(keyHolder);
        if (key != null) {
            order.setId(key.longValue());
        }
        return order;
    }

    private Number extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder == null) return null;
        if (keyHolder.getKeys() != null) {
            for (java.util.Map.Entry<String, Object> entry : keyHolder.getKeys().entrySet()) {
                if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number num) {
                    return num;
                }
            }
        }
        try {
            return keyHolder.getKey();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void saveOrderItems(Long orderId, List<OrderItem> items) {
        String sql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrderItem item = items.get(i);
                ps.setLong(1, orderId);
                ps.setLong(2, item.getProductId());
                ps.setString(3, item.getProductName());
                ps.setInt(4, item.getQuantity());
                ps.setBigDecimal(5, item.getUnitPrice());
                ps.setBigDecimal(6, item.getTotalPrice());
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });
    }

    @Override
    public Optional<Order> findById(Long id) {
        String sql = BASE_ORDER_SELECT + "WHERE o.id = ?";
        try {
            Order order = jdbcTemplate.queryForObject(sql, orderRowMapper, id);
            if (order != null) {
                order.setItems(findItemsByOrderId(order.getId()));
            }
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        String sql = BASE_ORDER_SELECT + "WHERE o.order_number = ?";
        try {
            Order order = jdbcTemplate.queryForObject(sql, orderRowMapper, orderNumber);
            if (order != null) {
                order.setItems(findItemsByOrderId(order.getId()));
            }
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        String sql = BASE_ORDER_SELECT + "WHERE o.user_id = ? ORDER BY o.created_at DESC";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, userId);
        for (Order o : orders) {
            o.setItems(findItemsByOrderId(o.getId()));
        }
        return orders;
    }

    @Override
    public List<Order> findAll() {
        String sql = BASE_ORDER_SELECT + "ORDER BY o.created_at DESC";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper);
        for (Order o : orders) {
            o.setItems(findItemsByOrderId(o.getId()));
        }
        return orders;
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        String sql = "SELECT id, order_id, product_id, product_name, quantity, unit_price, total_price " +
                     "FROM order_items WHERE order_id = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, orderItemRowMapper, orderId);
    }

    @Override
    public void updateStatus(Long orderId, OrderStatus status) {
        String sql = "UPDATE orders SET order_status = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), Timestamp.valueOf(LocalDateTime.now()), orderId);
    }

    @Override
    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public BigDecimal calculateTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(final_amount), 0) FROM orders WHERE order_status != 'CANCELLED'";
        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return total != null ? total : BigDecimal.ZERO;
    }
}
