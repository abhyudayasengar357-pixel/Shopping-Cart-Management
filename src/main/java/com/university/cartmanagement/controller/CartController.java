package com.university.cartmanagement.controller;

import com.university.cartmanagement.exception.InsufficientStockException;
import com.university.cartmanagement.model.Cart;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Controller for managing shopping cart interactions.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            session.setAttribute("returnUrl", "/cart");
            return "redirect:/login";
        }

        Cart cart = cartService.getCartForUser(currentUser.getId());
        String appliedCoupon = (String) session.getAttribute("appliedCoupon");

        CartService.CartSummary summary = cartService.calculateCartSummary(cart, appliedCoupon);

        model.addAttribute("cart", cart);
        model.addAttribute("summary", summary);
        model.addAttribute("appliedCoupon", appliedCoupon);

        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpServletRequest request,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            session.setAttribute("returnUrl", "/products/" + productId);
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to add products to your cart.");
            return "redirect:/login";
        }

        try {
            cartService.addToCart(currentUser.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to your cart successfully!");
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not add item to cart: " + ex.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && !referer.contains("/cart") ? referer : "/cart");
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            cartService.updateQuantity(currentUser.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully.");
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update quantity: " + ex.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/remove/{productId}")
    public String removeItem(@PathVariable("productId") Long productId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        cartService.removeFromCart(currentUser.getId(), productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        cartService.clearCart(currentUser.getId());
        session.removeAttribute("appliedCoupon");
        redirectAttributes.addFlashAttribute("successMessage", "Cart cleared.");
        return "redirect:/cart";
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam("couponCode") String couponCode,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (couponCode == null || couponCode.isBlank()) {
            session.removeAttribute("appliedCoupon");
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid coupon code.");
            return "redirect:/cart";
        }

        Cart cart = cartService.getCartForUser(currentUser.getId());
        CartService.CartSummary summary = cartService.calculateCartSummary(cart, couponCode.trim());

        if (summary.discountAmount().compareTo(BigDecimal.ZERO) > 0) {
            session.setAttribute("appliedCoupon", couponCode.trim().toUpperCase());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Promo code " + couponCode.trim().toUpperCase() + " applied! You saved $" + summary.discountAmount());
        } else {
            session.removeAttribute("appliedCoupon");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Promo code '" + couponCode.trim() + "' is invalid or does not meet minimum order requirements.");
        }

        return "redirect:/cart";
    }
}
