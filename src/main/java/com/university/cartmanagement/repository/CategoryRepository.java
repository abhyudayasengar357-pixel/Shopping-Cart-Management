package com.university.cartmanagement.repository;

import com.university.cartmanagement.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category persistence.
 * Demonstrates: Repository Pattern and Abstraction.
 */
public interface CategoryRepository {

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    List<Category> findAll();

    Category save(Category category);

    void update(Category category);

    void deleteById(Long id);

    int count();
}
