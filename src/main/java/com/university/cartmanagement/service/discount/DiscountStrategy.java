package com.university.cartmanagement.service.discount;

import java.math.BigDecimal;

/**
 * Strategy interface for coupon/order discount calculations.
 * Demonstrates: Interface Abstraction and Strategy Pattern (Polymorphism).
 */
public interface DiscountStrategy {

    String getCode();

    String getDescription();

    boolean isApplicable(BigDecimal subtotal);

    BigDecimal calculateDiscount(BigDecimal subtotal);
}
