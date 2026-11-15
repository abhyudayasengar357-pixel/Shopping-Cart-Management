package com.university.cartmanagement.controller;

import com.university.cartmanagement.model.Category;
import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.service.CategoryService;
import com.university.cartmanagement.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller for the public landing homepage and about pages.
 */
@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Product> featuredProducts = productService.getFeaturedProducts();
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("categories", categories);
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }
}
