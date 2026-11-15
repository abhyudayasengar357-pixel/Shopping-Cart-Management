package com.university.cartmanagement.service.discount;

import java.math.BigDecimal;

/**
 * Calculates a flat fixed discount amount on qualifying orders.
 * Demonstrates: Inheritance, Polymorphism, and Method Overriding.
 */
public class FlatDiscountStrategy extends AbstractDiscountStrategy {

    private final BigDecimal flatAmount;

    public FlatDiscountStrategy(String code, String description, BigDecimal minimumSubtotal, BigDecimal flatAmount) {
        super(code, description, minimumSubtotal);
        this.flatAmount = flatAmount;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (!isApplicable(subtotal)) {
            return BigDecimal.ZERO;
        }

        // Discount cannot exceed subtotal
        if (flatAmount.compareTo(subtotal) > 0) {
            return subtotal;
        }

        return flatAmount;
    }

    public BigDecimal getFlatAmount() {
        return flatAmount;
    }
}
