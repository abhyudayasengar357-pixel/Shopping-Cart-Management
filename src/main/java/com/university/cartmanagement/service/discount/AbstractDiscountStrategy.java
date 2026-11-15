package com.university.cartmanagement.service.discount;

import java.math.BigDecimal;

/**
 * Abstract class providing base validation and formatting for discount strategies.
 * Demonstrates: Abstraction and Polymorphism.
 */
public abstract class AbstractDiscountStrategy implements DiscountStrategy {

    protected final String code;
    protected final String description;
    protected final BigDecimal minimumSubtotal;

    public AbstractDiscountStrategy(String code, String description, BigDecimal minimumSubtotal) {
        this.code = code;
        this.description = description;
        this.minimumSubtotal = minimumSubtotal != null ? minimumSubtotal : BigDecimal.ZERO;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isApplicable(BigDecimal subtotal) {
        if (subtotal == null) return false;
        return subtotal.compareTo(minimumSubtotal) >= 0;
    }

    @Override
    public abstract BigDecimal calculateDiscount(BigDecimal subtotal);
}
