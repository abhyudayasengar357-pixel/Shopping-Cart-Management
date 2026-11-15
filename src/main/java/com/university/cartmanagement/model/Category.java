package com.university.cartmanagement.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Category entity representing product groupings.
 */
public class Category extends BaseEntity {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    private String description;
    private String icon;
    private int productCount;

    public Category() {
        super();
        this.icon = "bi-tag";
    }

    public Category(Long id) {
        super(id);
        this.icon = "bi-tag";
    }

    public Category(String name, String description) {
        this(null, name, description, "bi-tag");
    }

    public Category(String name, String description, String icon) {
        this(null, name, description, icon);
    }

    public Category(Long id, String name, String description, String icon) {
        super(id);
        this.name = name;
        this.description = description;
        this.icon = icon != null ? icon : "bi-tag";
    }

    public Category(Long id, String name, String description, String icon, LocalDateTime createdAt) {
        super(id, createdAt, createdAt);
        this.name = name;
        this.description = description;
        this.icon = icon != null ? icon : "bi-tag";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
