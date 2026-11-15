package com.university.cartmanagement.service;

import com.university.cartmanagement.exception.CartException;
import com.university.cartmanagement.exception.InsufficientStockException;
import com.university.cartmanagement.exception.ResourceNotFoundException;
import com.university.cartmanagement.model.AuditLog;
import com.university.cartmanagement.model.Cart;
import com.university.cartmanagement.model.CartItem;
import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.OrderItem;
import com.university.cartmanagement.model.OrderStatus;
import com.university.cartmanagement.model.PaymentMethod;
import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.repository.AuditRepository;
import com.university.cartmanagement.repository.OrderRepository;
import com.university.cartmanagement.service.payment.PaymentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service managing customer checkout, database transactions, inventory deductions,
 * and order lifecycle operations.
 *
 * Demonstrates: Spring @Transactional, Inventory consistency, Payment Polymorphism,
 * and Traditional JDBC audit logging.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;
    private final AuditRepository traditionalAuditRepository;
    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;

    public OrderService(
            OrderRepository orderRepository,
            CartService cartService,
            ProductService productService,
            UserService userService,
            @Qualifier("traditionalJdbcAuditRepository") AuditRepository traditionalAuditRepository,
            List<PaymentProcessor> processors) {

        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.userService = userService;
        this.traditionalAuditRepository = traditionalAuditRepository;

        // Populate polymorphic payment processor map
        this.paymentProcessors = new java.util.EnumMap<>(PaymentMethod.class);
        for (PaymentProcessor processor : processors) {
            this.paymentProcessors.put(processor.getSupportedMethod(), processor);
        }
    }

    /**
     * Executes the checkout transaction:
     * 1. Validates cart content and inventory availability
     * 2. Calculates totals, taxes, discounts
     * 3. Executes simulated payment via polymorphic processor
     * 4. Persists Order and OrderItems in DB
     * 5. Atomically reduces product inventory
     * 6. Clears the user's shopping cart
     * 7. Logs the transaction via Traditional JDBC AuditRepository
     */
    @Transactional(rollbackFor = Exception.class)
    public Order placeOrder(Long userId, String shippingAddress, PaymentMethod paymentMethod,
                            String couponCode, String customerNotes) {

        User user = userService.getUserById(userId);
        Cart cart = cartService.getCartForUser(userId);

        if (cart.isEmpty()) {
            throw new CartException("Cannot checkout with an empty shopping cart.");
        }

        // 1. Validate Stock for all cart items first
        for (CartItem item : cart.getItems()) {
            Product product = productService.getProductById(item.getProductId());
            if (!product.hasStock(item.getQuantity())) {
                throw new InsufficientStockException(product.getName(), item.getQuantity(), product.getStockQuantity());
            }
        }

        // 2. Compute final order amounts
        CartService.CartSummary summary = cartService.calculateCartSummary(cart, couponCode);

        // 3. Generate unique order number
        String orderNumber = generateOrderNumber();

        // 4. Construct Order entity
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setCustomerName(user.getFullName());
        order.setCustomerEmail(user.getEmail());
        order.setTotalItems(summary.totalItems());
        order.setSubtotal(summary.subtotal());
        order.setDiscountAmount(summary.discountAmount());
        order.setTaxAmount(summary.taxAmount());
        order.setShippingFee(summary.shippingFee());
        order.setFinalAmount(summary.finalAmount());
        order.setDiscountCode(summary.discountCode());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.CARD);
        order.setShippingAddress(shippingAddress != null && !shippingAddress.isBlank() ? shippingAddress : user.getAddress());
        order.setCustomerNotes(customerNotes);
        order.setPaymentStatus("PENDING");

        // 5. Execute simulated payment processor (Polymorphism)
        PaymentProcessor processor = paymentProcessors.get(order.getPaymentMethod());
        if (processor != null) {
            PaymentProcessor.PaymentResult result = processor.processPayment(order, summary.finalAmount());
            if (!result.success()) {
                throw new CartException("Payment simulation failed: " + result.message());
            }
            order.setPaymentStatus("PAID");
            order.setOrderStatus(OrderStatus.PROCESSING);
            log.info("Payment succeeded for order {}: Txn={}", orderNumber, result.transactionId());
        }

        // 6. Save Order record in database
        Order savedOrder = orderRepository.save(order);

        // 7. Convert CartItems to OrderItems and persist
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem(
                    ci.getProductId(),
                    ci.getProductName(),
                    ci.getQuantity(),
                    ci.getUnitPrice()
            );
            oi.setOrderId(savedOrder.getId());
            orderItems.add(oi);
        }
        orderRepository.saveOrderItems(savedOrder.getId(), orderItems);
        savedOrder.setItems(orderItems);

        // 8. Atomically reduce product inventory for each item
        for (CartItem ci : cart.getItems()) {
            productService.validateAndReduceStock(ci.getProductId(), ci.getQuantity());
        }

        // 9. Clear the customer's cart
        cartService.clearCart(userId);

        // 10. Traditional JDBC Transaction Audit Log (Requirement #6)
        try {
            AuditLog audit = new AuditLog(
                    "ORDER_PLACED",
                    "ORDER",
                    savedOrder.getId(),
                    String.format("Order %s placed by %s for $%s via %s",
                            orderNumber, user.getUsername(), savedOrder.getFinalAmount(), savedOrder.getPaymentMethod()),
                    user.getUsername()
            );
            traditionalAuditRepository.logAction(audit);
        } catch (Exception ex) {
            log.warn("Non-fatal: Traditional JDBC audit log failed: {}", ex.getMessage());
        }

        log.info("Order placed successfully with ID: {} and Order Number: {}", savedOrder.getId(), orderNumber);
        return savedOrder;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order Number", orderNumber));
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        orderRepository.updateStatus(orderId, status);

        // Audit update via Traditional JDBC
        try {
            AuditLog audit = new AuditLog(
                    "ORDER_STATUS_UPDATED",
                    "ORDER",
                    orderId,
                    "Order " + order.getOrderNumber() + " status updated from " + order.getOrderStatus() + " to " + status,
                    "ADMIN"
            );
            traditionalAuditRepository.logAction(audit);
        } catch (Exception ex) {
            log.warn("Traditional JDBC audit log update failed: {}", ex.getMessage());
        }
    }

    public int countOrders() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        return orderRepository.calculateTotalRevenue();
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = 1000 + new Random().nextInt(9000);
        return "ORD-" + datePart + "-" + randomPart;
    }

    /**
     * Dashboard statistics summary record.
     */
    public record DashboardStats(
            int totalOrders,
            BigDecimal totalRevenue,
            int totalProducts,
            int totalCustomers,
            int totalCategories,
            List<Order> recentOrders,
            List<AuditLog> recentAuditLogs
    ) {}

    public DashboardStats getDashboardStatistics() {
        int totalOrders = countOrders();
        BigDecimal totalRevenue = getTotalRevenue();
        int totalProducts = productService.countProducts();
        int totalCustomers = userService.countUsers();
        List<Order> allOrders = getAllOrders();
        List<Order> recentOrders = allOrders.stream().limit(5).toList();
        List<AuditLog> recentAuditLogs = traditionalAuditRepository.findRecent(8);

        return new DashboardStats(
                totalOrders,
                totalRevenue,
                totalProducts,
                totalCustomers,
                5,
                recentOrders,
                recentAuditLogs
        );
    }
}
