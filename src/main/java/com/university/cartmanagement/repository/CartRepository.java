package com.university.cartmanagement.repository;

import com.university.cartmanagement.model.Cart;
import com.university.cartmanagement.model.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Shopping Cart operations.
 */
public interface CartRepository {

    Cart findOrCreateByUserId(Long userId);

    Optional<Cart> findByUserId(Long userId);

    List<CartItem> findItemsByCartId(Long cartId);

    void addItem(Long cartId, Long productId, int quantity, BigDecimal unitPrice);

    void updateItemQuantity(Long cartId, Long productId, int quantity);

    void removeItem(Long cartId, Long productId);

    void clearCart(Long cartId);

    int countItemsInCart(Long cartId);
}
