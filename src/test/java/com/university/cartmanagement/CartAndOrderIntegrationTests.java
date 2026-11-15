package com.university.cartmanagement;

import com.university.cartmanagement.exception.InsufficientStockException;
import com.university.cartmanagement.model.Cart;
import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.OrderStatus;
import com.university.cartmanagement.model.PaymentMethod;
import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.model.Role;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.repository.AuditRepository;
import com.university.cartmanagement.service.CartService;
import com.university.cartmanagement.service.OrderService;
import com.university.cartmanagement.service.ProductService;
import com.university.cartmanagement.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartAndOrderIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    @Qualifier("traditionalJdbcAuditRepository")
    private AuditRepository traditionalAuditRepository;

    @Test
    @DisplayName("Context loads and seeded entities are verified")
    void testContextLoadsAndSeedData() {
        assertNotNull(userService);
        assertNotNull(productService);
        assertNotNull(cartService);
        assertNotNull(orderService);

        int count = productService.countProducts();
        assertTrue(count >= 12, "Should have at least 12 seeded products");

        Product p1 = productService.getProductById(1L);
        assertNotNull(p1);
        assertEquals("UltraBook Pro 15", p1.getName());
    }

    @Test
    @DisplayName("User Authentication and BCrypt Verification")
    void testUserAuthentication() {
        // Authenticate admin
        User admin = userService.authenticate("admin", "admin123");
        assertNotNull(admin);
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isAdmin());

        // Authenticate standard customer
        User customer = userService.authenticate("john_doe", "password123");
        assertNotNull(customer);
        assertEquals(Role.CUSTOMER, customer.getRole());
        assertTrue(customer.isCustomer());
    }

    @Test
    @DisplayName("Full Checkout Transaction: Stock Decrement, Cart Cleared, Order Created, and Traditional JDBC Audited")
    void testEndToEndCheckoutTransaction() {
        // 1. Create a distinct customer for isolation
        String testUser = "test_user_" + System.currentTimeMillis();
        User user = new User(testUser, testUser + "@test.com", "secretPass123", "Test Student");
        user.setAddress("404 Campus Way, Suite 101");
        User savedUser = userService.registerCustomer(user);
        assertNotNull(savedUser.getId());

        // 2. Fetch a product with known stock
        Product product = productService.getProductById(3L); // Smart Fitness Watch
        int originalStock = product.getStockQuantity();
        assertTrue(originalStock >= 5, "Product should have enough stock");

        // 3. Add 2 units to shopping cart
        cartService.addToCart(savedUser.getId(), product.getId(), 2);
        Cart cart = cartService.getCartForUser(savedUser.getId());
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getTotalItemCount());

        // 4. Place order with coupon code and Card payment method
        Order order = orderService.placeOrder(
                savedUser.getId(),
                "404 Campus Way, Suite 101",
                PaymentMethod.CARD,
                "WELCOME10",
                "Please deliver after 2 PM"
        );

        // 5. Verify Order Creation
        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals("PAID", order.getPaymentStatus());
        assertEquals(OrderStatus.PROCESSING, order.getOrderStatus());
        assertTrue(order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0);

        // 6. Verify Product Inventory was decremented atomically
        Product updatedProduct = productService.getProductById(product.getId());
        assertEquals(originalStock - 2, updatedProduct.getStockQuantity());

        // 7. Verify Cart was cleared
        Cart clearedCart = cartService.getCartForUser(savedUser.getId());
        assertTrue(clearedCart.isEmpty());

        // 8. Verify Traditional JDBC Audit Trail captured the transaction
        var recentLogs = traditionalAuditRepository.findRecent(5);
        assertFalse(recentLogs.isEmpty());
        assertTrue(recentLogs.stream().anyMatch(l -> l.getAction().equals("ORDER_PLACED")));
    }

    @Test
    @DisplayName("Inventory Safety: Attempting to order beyond available stock throws InsufficientStockException")
    void testInsufficientStockException() {
        Product product = productService.getProductById(11L); // Office Chair
        int available = product.getStockQuantity();

        assertThrows(InsufficientStockException.class, () -> {
            cartService.addToCart(2L, product.getId(), available + 100);
        });
    }
}
