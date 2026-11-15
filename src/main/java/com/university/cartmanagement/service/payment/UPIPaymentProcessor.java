package com.university.cartmanagement.service.payment;

import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated UPI / Instant Transfer Payment Processor.
 * Demonstrates: Polymorphic processor implementation.
 */
@Component
public class UPIPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.UPI;
    }

    @Override
    public PaymentResult processPayment(Order order, BigDecimal amount) {
        String txnId = "UPI-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, txnId, "UPI payment received and verified instantly for $" + amount);
    }
}
