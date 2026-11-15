package com.university.cartmanagement.service.discount;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service managing discount strategies and calculations.
 * Demonstrates: Strategy Pattern Manager, Method Overloading, and Collections.
 */
@Service
public class DiscountCalculator {

    private final Map<String, DiscountStrategy> strategies;

    public DiscountCalculator() {
        this.strategies = new HashMap<>();

        // Register default discount strategies
        registerStrategy(new PercentageDiscountStrategy("WELCOME10", "10% off on orders over $50 (up to $20)",
                BigDecimal.valueOf(50.00), BigDecimal.valueOf(10.0), BigDecimal.valueOf(20.00)));

        registerStrategy(new FlatDiscountStrategy("SAVE25", "$25 off on orders over $150",
                BigDecimal.valueOf(150.00), BigDecimal.valueOf(25.00)));

        registerStrategy(new PercentageDiscountStrategy("SUPER20", "20% festive discount on orders over $200 (up to $50)",
                BigDecimal.valueOf(200.00), BigDecimal.valueOf(20.0), BigDecimal.valueOf(50.00)));

        registerStrategy(new FlatDiscountStrategy("STUDENT15", "$15 university student discount on orders over $75",
                BigDecimal.valueOf(75.00), BigDecimal.valueOf(15.00)));
    }

    public void registerStrategy(DiscountStrategy strategy) {
        if (strategy != null && strategy.getCode() != null) {
            strategies.put(strategy.getCode().toUpperCase(), strategy);
        }
    }

    // Method Overloading 1: Calculate discount by promo code
    public BigDecimal calculateDiscount(String couponCode, BigDecimal subtotal) {
        if (couponCode == null || couponCode.isBlank() || subtotal == null) {
            return BigDecimal.ZERO;
        }

        DiscountStrategy strategy = strategies.get(couponCode.trim().toUpperCase());
        if (strategy != null && strategy.isApplicable(subtotal)) {
            return strategy.calculateDiscount(subtotal);
        }

        return BigDecimal.ZERO;
    }

    // Method Overloading 2: Automatically find and apply best available discount
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return strategies.values().stream()
                .filter(s -> s.isApplicable(subtotal))
                .map(s -> s.calculateDiscount(subtotal))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public Optional<DiscountStrategy> getStrategy(String couponCode) {
        if (couponCode == null) return Optional.empty();
        return Optional.ofNullable(strategies.get(couponCode.trim().toUpperCase()));
    }

    public Map<String, DiscountStrategy> getAllStrategies() {
        return Collections.unmodifiableMap(strategies);
    }
}
