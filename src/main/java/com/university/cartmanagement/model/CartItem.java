package com.university.cartmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an individual line item in a shopping cart.
 */
public class CartItem extends BaseEntity {

    private Long cartId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private int stockAvailable;

    public CartItem() {
        super();
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
    }

    public CartItem(Long productId, int quantity, BigDecimal unitPrice) {
        this(null, null, productId, quantity, unitPrice);
    }

    public CartItem(Long id, Long cartId, Long productId, int quantity, BigDecimal unitPrice) {
        super(id);
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public CartItem(Long id, Long cartId, Long productId, int quantity, BigDecimal unitPrice,
                    LocalDateTime createdAt) {
        super(id, createdAt, createdAt);
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Business method to calculate item subtotal
    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(int stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
