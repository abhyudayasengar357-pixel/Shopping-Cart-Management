package com.university.cartmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order entity representing a customer's purchase order.
 * Demonstrates: Composition, Encapsulation, and Business calculations.
 */
public class Order extends BaseEntity {

    private String orderNumber;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private int totalItems;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private String discountCode;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private String paymentStatus;
    private String shippingAddress;
    private String customerNotes;
    private List<OrderItem> items;

    public Order() {
        super();
        this.totalItems = 0;
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.shippingFee = BigDecimal.ZERO;
        this.finalAmount = BigDecimal.ZERO;
        this.orderStatus = OrderStatus.PENDING;
        this.paymentMethod = PaymentMethod.CARD;
        this.paymentStatus = "PAID";
        this.items = new ArrayList<>();
    }

    public Order(Long id) {
        super(id);
        this.items = new ArrayList<>();
    }

    public Order(Long id, String orderNumber, Long userId, int totalItems, BigDecimal subtotal,
                 BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal shippingFee,
                 BigDecimal finalAmount, String discountCode, OrderStatus orderStatus,
                 PaymentMethod paymentMethod, String paymentStatus, String shippingAddress,
                 String customerNotes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalItems = totalItems;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.shippingFee = shippingFee;
        this.finalAmount = finalAmount;
        this.discountCode = discountCode;
        this.orderStatus = orderStatus;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.shippingAddress = shippingAddress;
        this.customerNotes = customerNotes;
        this.items = new ArrayList<>();
    }

    // Business Methods
    public void calculateTotals() {
        if (items != null) {
            this.totalItems = items.stream().mapToInt(OrderItem::getQuantity).sum();
            this.subtotal = items.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (shippingFee == null) shippingFee = BigDecimal.ZERO;

        BigDecimal net = subtotal.subtract(discountAmount);
        if (net.compareTo(BigDecimal.ZERO) < 0) net = BigDecimal.ZERO;

        this.finalAmount = net.add(taxAmount).add(shippingFee);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public List<OrderItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", userId=" + userId +
                ", finalAmount=" + finalAmount +
                ", status=" + orderStatus +
                '}';
    }
}
