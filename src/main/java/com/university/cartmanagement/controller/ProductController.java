package com.university.cartmanagement.controller;

import com.university.cartmanagement.model.Category;
import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.service.CategoryService;
import com.university.cartmanagement.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for product browsing, searching, and product detail viewing.
 */
@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "sortBy", required = false, defaultValue = "featured") String sortBy,
            Model model) {

        List<Product> products = productService.searchProducts(keyword, categoryId, minPrice, maxPrice, sortBy);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("totalFound", products.size());

        // Category banner if category selected
        if (categoryId != null && categoryId > 0) {
            try {
                Category selectedCategory = categoryService.getCategoryById(categoryId);
                model.addAttribute("currentCategory", selectedCategory);
            } catch (Exception ignored) {}
        }

        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String showProductDetails(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id);
        List<Product> relatedProducts = productService.getProductsByCategory(product.getCategoryId())
                .stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .toList();

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        return "products/detail";
    }
}
