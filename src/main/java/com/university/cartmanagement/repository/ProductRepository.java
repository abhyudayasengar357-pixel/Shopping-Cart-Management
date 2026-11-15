package com.university.cartmanagement.repository;

import com.university.cartmanagement.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product operations.
 * Demonstrates: Interface Abstraction and Method Overloading.
 */
public interface ProductRepository {

    Optional<Product> findById(Long id);

    List<Product> findAll();

    List<Product> findFeatured();

    List<Product> findByCategoryId(Long categoryId);

    // Method Overloading 1: Basic keyword search
    List<Product> search(String keyword);

    // Method Overloading 2: Multi-criteria faceted search and filtering
    List<Product> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy);

    Product save(Product product);

    void update(Product product);

    void updateStock(Long productId, int newStock);

    void reduceStock(Long productId, int quantity);

    void deleteById(Long id);

    int count();
}
