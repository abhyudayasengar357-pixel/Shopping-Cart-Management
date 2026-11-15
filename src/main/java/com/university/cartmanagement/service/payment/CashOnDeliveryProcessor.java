package com.university.cartmanagement.service.payment;

import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated Cash On Delivery Payment Processor.
 * Demonstrates: Polymorphic processor implementation.
 */
@Component
public class CashOnDeliveryProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CASH_ON_DELIVERY;
    }

    @Override
    public PaymentResult processPayment(Order order, BigDecimal amount) {
        String txnId = "COD-AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, txnId, "Order placed on Cash on Delivery. Payment will be collected at delivery.");
    }
}
