package com.university.cartmanagement.service;

import com.university.cartmanagement.exception.ResourceNotFoundException;
import com.university.cartmanagement.model.Category;
import com.university.cartmanagement.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing product categories.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category name", name));
    }

    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new IllegalArgumentException("A category with this name already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Category category) {
        getCategoryById(category.getId()); // verify existence
        categoryRepository.update(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        getCategoryById(id);
        categoryRepository.deleteById(id);
    }

    public int countCategories() {
        return categoryRepository.count();
    }
}
