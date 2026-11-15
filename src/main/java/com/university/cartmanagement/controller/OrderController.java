package com.university.cartmanagement.controller;

import com.university.cartmanagement.model.Cart;
import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.PaymentMethod;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.service.CartService;
import com.university.cartmanagement.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for checkout flow, simulated payment, order placement, and customer order history.
 */
@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            session.setAttribute("returnUrl", "/checkout");
            return "redirect:/login";
        }

        Cart cart = cartService.getCartForUser(currentUser.getId());
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        String coupon = (String) session.getAttribute("appliedCoupon");
        CartService.CartSummary summary = cartService.calculateCartSummary(cart, coupon);

        model.addAttribute("cart", cart);
        model.addAttribute("summary", summary);
        model.addAttribute("user", currentUser);
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "checkout/checkout";
    }

    @PostMapping("/checkout/place")
    public String placeOrder(@RequestParam("shippingAddress") String shippingAddress,
                             @RequestParam("paymentMethod") String paymentMethodStr,
                             @RequestParam(value = "customerNotes", required = false) String customerNotes,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodStr);
            String coupon = (String) session.getAttribute("appliedCoupon");

            Order order = orderService.placeOrder(
                    currentUser.getId(),
                    shippingAddress,
                    paymentMethod,
                    coupon,
                    customerNotes
            );

            // Clean up session coupon
            session.removeAttribute("appliedCoupon");

            return "redirect:/checkout/success/" + order.getOrderNumber();

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order placement failed: " + ex.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/checkout/success/{orderNumber}")
    public String orderSuccess(@PathVariable("orderNumber") String orderNumber,
                               HttpSession session,
                               Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderByNumber(orderNumber);
        // Authorization check: only order owner or admin can view
        if (!order.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            return "redirect:/";
        }

        model.addAttribute("order", order);
        return "checkout/success";
    }

    @GetMapping("/orders")
    public String orderHistory(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            session.setAttribute("returnUrl", "/orders");
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersByUser(currentUser.getId());
        model.addAttribute("orders", orders);
        return "orders/history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable("id") Long id,
                              HttpSession session,
                              Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(id);
        if (!order.getUserId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "orders/detail";
    }
}
