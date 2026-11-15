package com.university.cartmanagement.service;

import com.university.cartmanagement.exception.InsufficientStockException;
import com.university.cartmanagement.model.Cart;
import com.university.cartmanagement.model.CartItem;
import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.repository.CartRepository;
import com.university.cartmanagement.service.discount.DiscountCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service managing user shopping cart operations and computations.
 * Demonstrates: Method Overloading, Stock Validation, and Business Calculations.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;
    private final DiscountCalculator discountCalculator;

    public CartService(CartRepository cartRepository, ProductService productService, DiscountCalculator discountCalculator) {
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.discountCalculator = discountCalculator;
    }

    public Cart getCartForUser(Long userId) {
        Cart cart = cartRepository.findOrCreateByUserId(userId);
        List<CartItem> items = cartRepository.findItemsByCartId(cart.getId());
        cart.setItems(items);
        return cart;
    }

    // Method Overloading 1: Add default 1 item
    @Transactional
    public void addToCart(Long userId, Long productId) {
        addToCart(userId, productId, 1);
    }

    // Method Overloading 2: Add specific quantity with stock validation
    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be at least 1");
        }

        Product product = productService.getProductById(productId);
        Cart cart = cartRepository.findOrCreateByUserId(userId);
        List<CartItem> items = cartRepository.findItemsByCartId(cart.getId());

        int currentInCart = items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .mapToInt(CartItem::getQuantity)
                .findFirst()
                .orElse(0);

        int totalRequested = currentInCart + quantity;
        if (!product.hasStock(totalRequested)) {
            throw new InsufficientStockException(product.getName(), totalRequested, product.getStockQuantity());
        }

        cartRepository.addItem(cart.getId(), productId, quantity, product.getPrice());
    }

    @Transactional
    public void updateQuantity(Long userId, Long productId, int newQuantity) {
        Cart cart = cartRepository.findOrCreateByUserId(userId);

        if (newQuantity <= 0) {
            cartRepository.removeItem(cart.getId(), productId);
            return;
        }

        Product product = productService.getProductById(productId);
        if (!product.hasStock(newQuantity)) {
            throw new InsufficientStockException(product.getName(), newQuantity, product.getStockQuantity());
        }

        cartRepository.updateItemQuantity(cart.getId(), productId, newQuantity);
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findOrCreateByUserId(userId);
        cartRepository.removeItem(cart.getId(), productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findOrCreateByUserId(userId);
        cartRepository.clearCart(cart.getId());
    }

    public int getCartItemCount(Long userId) {
        Cart cart = cartRepository.findOrCreateByUserId(userId);
        return cartRepository.countItemsInCart(cart.getId());
    }

    /**
     * Data Transfer Record representing complete order/cart calculation breakdown.
     */
    public record CartSummary(
            int totalItems,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            String discountCode,
            BigDecimal taxAmount,
            BigDecimal shippingFee,
            BigDecimal finalAmount
    ) {}

    public CartSummary calculateCartSummary(Cart cart, String couponCode) {
        int totalItems = cart.getTotalItemCount();
        BigDecimal subtotal = cart.calculateSubtotal();

        // Calculate discount using strategy pattern
        BigDecimal discount = BigDecimal.ZERO;
        String appliedCode = null;

        if (couponCode != null && !couponCode.isBlank()) {
            discount = discountCalculator.calculateDiscount(couponCode.trim(), subtotal);
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                appliedCode = couponCode.trim().toUpperCase();
            }
        }

        BigDecimal netSubtotal = subtotal.subtract(discount);
        if (netSubtotal.compareTo(BigDecimal.ZERO) < 0) netSubtotal = BigDecimal.ZERO;

        // 5% standard sales tax
        BigDecimal tax = netSubtotal.multiply(BigDecimal.valueOf(0.05))
                .setScale(2, RoundingMode.HALF_UP);

        // Free shipping for orders $100 and above, otherwise $10
        BigDecimal shipping = BigDecimal.ZERO;
        if (totalItems > 0 && subtotal.compareTo(BigDecimal.valueOf(100.00)) < 0) {
            shipping = BigDecimal.valueOf(10.00);
        }

        BigDecimal finalTotal = netSubtotal.add(tax).add(shipping);

        return new CartSummary(totalItems, subtotal, discount, appliedCode, tax, shipping, finalTotal);
    }
}
