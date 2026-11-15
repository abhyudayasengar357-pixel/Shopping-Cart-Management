package com.university.cartmanagement.repository;

import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.OrderItem;
import com.university.cartmanagement.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order persistence and analytics.
 */
public interface OrderRepository {

    Order save(Order order);

    void saveOrderItems(Long orderId, List<OrderItem> items);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);

    List<Order> findAll();

    List<OrderItem> findItemsByOrderId(Long orderId);

    void updateStatus(Long orderId, OrderStatus status);

    int count();

    BigDecimal calculateTotalRevenue();
}
