package com.university.cartmanagement.service.payment;

import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated Credit/Debit Card Payment Processor.
 * Demonstrates: Polymorphic processor implementation.
 */
@Component
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentResult processPayment(Order order, BigDecimal amount) {
        String txnId = "CARD-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, txnId, "Credit/Debit Card payment authorized successfully for $" + amount);
    }
}
