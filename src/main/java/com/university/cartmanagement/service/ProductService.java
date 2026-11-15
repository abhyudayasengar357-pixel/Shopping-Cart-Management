package com.university.cartmanagement.service;

import com.university.cartmanagement.exception.InsufficientStockException;
import com.university.cartmanagement.exception.ResourceNotFoundException;
import com.university.cartmanagement.model.Product;
import com.university.cartmanagement.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service managing product catalog, inventory, and searches.
 * Demonstrates: Method Overloading, Exception Handling, and Transaction Management.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductRepository traditionalProductRepository;

    public ProductService(
            @Qualifier("springJdbcProductRepository") ProductRepository productRepository,
            @Qualifier("traditionalJdbcProductRepository") ProductRepository traditionalProductRepository) {
        this.productRepository = productRepository;
        this.traditionalProductRepository = traditionalProductRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findFeatured();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // Method Overloading 1: Basic keyword search
    public List<Product> searchProducts(String keyword) {
        return productRepository.search(keyword);
    }

    // Method Overloading 2: Faceted multi-parameter search & filter
    public List<Product> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        return productRepository.search(keyword, categoryId, minPrice, maxPrice, sortBy);
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            product.setStockQuantity(0);
        }
        return productRepository.save(product);
    }

    @Transactional
    public void updateProduct(Product product) {
        getProductById(product.getId()); // verify exists
        productRepository.update(product);
    }

    @Transactional
    public void updateStock(Long productId, int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        getProductById(productId);
        productRepository.updateStock(productId, newStock);
    }

    @Transactional
    public void validateAndReduceStock(Long productId, int quantity) {
        Product product = getProductById(productId);
        if (!product.hasStock(quantity)) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStockQuantity());
        }
        productRepository.reduceStock(productId, quantity);
    }

    @Transactional
    public void deleteProduct(Long id) {
        getProductById(id);
        productRepository.deleteById(id);
    }

    public int countProducts() {
        return productRepository.count();
    }

    /**
     * Helper for side-by-side comparison with Traditional JDBC.
     */
    public ProductRepository getTraditionalProductRepository() {
        return traditionalProductRepository;
    }
}
