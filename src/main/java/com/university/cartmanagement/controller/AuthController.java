package com.university.cartmanagement.controller;

import com.university.cartmanagement.exception.AuthenticationException;
import com.university.cartmanagement.model.Role;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller managing customer registration, session login, logout, and profile management.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                HttpSession session,
                                Model model) {
        // If already logged in, redirect
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            return currentUser.isAdmin() ? "redirect:/admin/dashboard" : "redirect:/products";
        }

        if ("admin_required".equals(error)) {
            model.addAttribute("errorMessage", "Administrator privileges required. Please sign in as an Admin.");
        } else if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }

        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.authenticate(username, password);
            session.setAttribute("currentUser", user);

            String returnUrl = (String) session.getAttribute("returnUrl");
            session.removeAttribute("returnUrl");

            if (returnUrl != null && !returnUrl.isBlank()) {
                return "redirect:" + returnUrl;
            }

            if (user.isAdmin()) {
                redirectAttributes.addFlashAttribute("successMessage", "Welcome back, Administrator " + user.getFullName() + "!");
                return "redirect:/admin/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + user.getFullName() + "!");
                return "redirect:/products";
            }
        } catch (AuthenticationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            return "redirect:/products";
        }
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") User user,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            User saved = userService.registerCustomer(user);
            session.setAttribute("currentUser", saved);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Welcome to Online Shopping, " + saved.getFullName() + ".");
            return "redirect:/products";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout=true";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        // Fetch fresh user record
        User user = userService.getUserById(currentUser.getId());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User formUser,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            formUser.setId(currentUser.getId());
            formUser.setUsername(currentUser.getUsername());
            formUser.setRole(currentUser.getRole());
            userService.updateProfile(formUser);

            // Update in session
            User updated = userService.getUserById(currentUser.getId());
            session.setAttribute("currentUser", updated);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + ex.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "New password and confirmation do not match.");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(currentUser.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
