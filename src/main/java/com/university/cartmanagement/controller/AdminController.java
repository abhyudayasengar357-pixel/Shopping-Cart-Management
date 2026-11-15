package com.university.cartmanagement.controller;

import com.university.cartmanagement.model.Category;
import com.university.cartmanagement.model.Order;
import com.university.cartmanagement.model.OrderStatus;
import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.model.User;
import com.university.cartmanagement.service.CategoryService;
import com.university.cartmanagement.service.OrderService;
import com.university.cartmanagement.service.ProductService;
import com.university.cartmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller managing Admin Dashboard, Product Inventory CRUD, Category CRUD,
 * Order Management, and Customer Records.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;

    public AdminController(ProductService productService,
                           CategoryService categoryService,
                           OrderService orderService,
                           UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
    }

    // =========================================================================
    // 1. DASHBOARD
    // =========================================================================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        OrderService.DashboardStats stats = orderService.getDashboardStatistics();
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    // =========================================================================
    // 2. PRODUCT MANAGEMENT (CRUD)
    // =========================================================================

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String showCreateProductForm(Model model) {
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new Product());
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Add New Product");
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Edit Product: " + product.getName());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("pageTitle", product.getId() == null ? "Add New Product" : "Edit Product");
            return "admin/product-form";
        }

        try {
            if (product.getId() == null) {
                productService.createProduct(product);
                redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
            } else {
                productService.updateProduct(product);
                redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving product: " + ex.getMessage());
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/products/stock")
    public String quickUpdateStock(@RequestParam("productId") Long productId,
                                   @RequestParam("stockQuantity") int stockQuantity,
                                   RedirectAttributes redirectAttributes) {
        try {
            productService.updateStock(productId, stockQuantity);
            redirectAttributes.addFlashAttribute("successMessage", "Stock updated for product #" + productId);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update stock: " + ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete product: " + ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    // =========================================================================
    // 3. CATEGORY MANAGEMENT (CRUD)
    // =========================================================================

    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new Category());
        }
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid category data provided.");
            return "redirect:/admin/categories";
        }

        try {
            if (category.getId() == null) {
                categoryService.createCategory(category);
                redirectAttributes.addFlashAttribute("successMessage", "Category created successfully!");
            } else {
                categoryService.updateCategory(category);
                redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save category: " + ex.getMessage());
        }

        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete category: " + ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // =========================================================================
    // 4. ORDER MANAGEMENT
    // =========================================================================

    @GetMapping("/orders")
    public String listOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String showOrderDetail(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Long id,
                                    @RequestParam("status") String statusStr,
                                    RedirectAttributes redirectAttributes) {
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr);
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + id + " status updated to " + status.getDisplayName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update order status: " + ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // =========================================================================
    // 5. CUSTOMER MANAGEMENT
    // =========================================================================

    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<User> customers = userService.getCustomers();
        model.addAttribute("customers", customers);
        return "admin/customers";
    }
}
