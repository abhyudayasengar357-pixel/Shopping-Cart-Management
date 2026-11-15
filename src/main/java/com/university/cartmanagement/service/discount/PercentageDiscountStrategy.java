package com.university.cartmanagement.service.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates a percentage discount with an optional maximum cap.
 * Demonstrates: Inheritance, Polymorphism, and Method Overriding.
 */
public class PercentageDiscountStrategy extends AbstractDiscountStrategy {

    private final BigDecimal percentage;
    private final BigDecimal maxDiscount;

    public PercentageDiscountStrategy(String code, String description, BigDecimal minimumSubtotal,
                                      BigDecimal percentage, BigDecimal maxDiscount) {
        super(code, description, minimumSubtotal);
        this.percentage = percentage;
        this.maxDiscount = maxDiscount;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (!isApplicable(subtotal)) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = subtotal.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
            return maxDiscount.setScale(2, RoundingMode.HALF_UP);
        }

        return discount;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }
}
