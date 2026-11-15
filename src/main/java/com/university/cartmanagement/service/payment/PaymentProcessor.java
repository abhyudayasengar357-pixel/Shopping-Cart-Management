package com.university.cartmanagement.service.payment;

import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.PaymentMethod;

import java.math.BigDecimal;

/**
 * Interface for simulated payment processing.
 * Demonstrates: Interface Abstraction and Polymorphism.
 */
public interface PaymentProcessor {

    PaymentMethod getSupportedMethod();

    PaymentResult processPayment(Order order, BigDecimal amount);

    record PaymentResult(boolean success, String transactionId, String message) {}
}
