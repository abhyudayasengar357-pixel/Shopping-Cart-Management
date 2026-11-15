package com.university.cartmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shopping Cart entity representing an active session/user cart.
 * Demonstrates: Aggregation, Java Collections, and Stream API.
 */
public class Cart extends BaseEntity {

    private Long userId;
    private List<CartItem> items;

    public Cart() {
        super();
        this.items = new ArrayList<>();
    }

    public Cart(Long userId) {
        this();
        this.userId = userId;
    }

    public Cart(Long id, Long userId) {
        super(id);
        this.userId = userId;
        this.items = new ArrayList<>();
    }

    public Cart(Long id, Long userId, List<CartItem> items) {
        super(id);
        this.userId = userId;
        this.items = items != null ? items : new ArrayList<>();
    }

    public Cart(Long id, Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.userId = userId;
        this.items = new ArrayList<>();
    }

    // Business Methods on Cart Collection
    public void addItem(CartItem item) {
        if (item == null) return;
        // Check if item already exists in cart, then increment quantity
        for (CartItem existing : items) {
            if (existing.getProductId().equals(item.getProductId())) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                return;
            }
        }
        this.items.add(item);
    }

    public void removeItem(Long productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
    }

    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal calculateSubtotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        this.items.clear();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public void setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", userId=" + userId +
                ", totalItems=" + getTotalItemCount() +
                ", subtotal=" + calculateSubtotal() +
                '}';
    }
}
