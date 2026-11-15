package com.university.cartmanagement;

import com.university.cartmanagement.service.discount.DiscountCalculator;
import com.university.cartmanagement.service.discount.FlatDiscountStrategy;
import com.university.cartmanagement.service.discount.PercentageDiscountStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying OOP Polymorphism and Strategy Pattern implementation for discounts.
 */
class DiscountStrategyTests {

    @Test
    @DisplayName("PercentageDiscountStrategy calculates 10% discount properly up to cap")
    void testPercentageDiscount() {
        PercentageDiscountStrategy strategy = new PercentageDiscountStrategy(
                "TEST10", "10% off over $50",
                BigDecimal.valueOf(50.00), BigDecimal.valueOf(10.0), BigDecimal.valueOf(20.00));

        // Below threshold
        assertFalse(strategy.isApplicable(BigDecimal.valueOf(40.00)));
        assertEquals(BigDecimal.ZERO, strategy.calculateDiscount(BigDecimal.valueOf(40.00)));

        // At threshold ($100 order -> 10% = $10.00)
        assertTrue(strategy.isApplicable(BigDecimal.valueOf(100.00)));
        assertEquals(new BigDecimal("10.00"), strategy.calculateDiscount(BigDecimal.valueOf(100.00)));

        // Above max cap ($300 order -> 10% is $30, but capped at $20.00)
        assertEquals(0, new BigDecimal("20.00").compareTo(strategy.calculateDiscount(BigDecimal.valueOf(300.00))));
    }

    @Test
    @DisplayName("FlatDiscountStrategy deducts flat amount without exceeding subtotal")
    void testFlatDiscount() {
        FlatDiscountStrategy strategy = new FlatDiscountStrategy(
                "FLAT25", "$25 off over $100",
                BigDecimal.valueOf(100.00), BigDecimal.valueOf(25.00));

        assertFalse(strategy.isApplicable(BigDecimal.valueOf(80.00)));
        assertTrue(strategy.isApplicable(BigDecimal.valueOf(120.00)));
        assertEquals(BigDecimal.valueOf(25.00), strategy.calculateDiscount(BigDecimal.valueOf(120.00)));
    }

    @Test
    @DisplayName("DiscountCalculator resolves promo codes and method overloading")
    void testDiscountCalculatorService() {
        DiscountCalculator calculator = new DiscountCalculator();

        // Overloaded Method 1: By Code
        BigDecimal discountWelcome = calculator.calculateDiscount("WELCOME10", BigDecimal.valueOf(100.00));
        assertEquals(new BigDecimal("10.00"), discountWelcome);

        BigDecimal discountStudent = calculator.calculateDiscount("STUDENT15", BigDecimal.valueOf(100.00));
        assertEquals(BigDecimal.valueOf(15.00), discountStudent);

        // Invalid code
        BigDecimal discountInvalid = calculator.calculateDiscount("NON_EXISTENT", BigDecimal.valueOf(100.00));
        assertEquals(BigDecimal.ZERO, discountInvalid);

        // Overloaded Method 2: Auto Best Available
        BigDecimal bestDiscount = calculator.calculateDiscount(BigDecimal.valueOf(250.00));
        assertTrue(bestDiscount.compareTo(BigDecimal.ZERO) > 0);
    }
}
