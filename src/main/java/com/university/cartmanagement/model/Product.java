package com.university.cartmanagement.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity representing merchandise in the catalog.
 * Demonstrates: Encapsulation, Interface Implementation (Comparable), and Business Logic.
 */
public class Product extends BaseEntity implements Comparable<Product> {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String categoryName;
    private String imageUrl;
    private boolean featured;

    public Product() {
        super();
        this.stockQuantity = 0;
        this.price = BigDecimal.ZERO;
        this.featured = false;
        this.imageUrl = "/images/products/laptop.svg";
    }

    public Product(Long id) {
        super(id);
        this.stockQuantity = 0;
        this.price = BigDecimal.ZERO;
    }

    // Overloaded Constructor: Core Product Data
    public Product(String name, BigDecimal price, Integer stockQuantity, Long categoryId) {
        this(null, name, "", price, stockQuantity, categoryId, null, false);
    }

    // Overloaded Constructor: Full Product Data
    public Product(Long id, String name, String description, BigDecimal price,
                   Integer stockQuantity, Long categoryId, String imageUrl, boolean featured) {
        super(id);
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl != null ? imageUrl : "/images/products/laptop.svg";
        this.featured = featured;
    }

    // Overloaded Constructor with timestamps
    public Product(Long id, String name, String description, BigDecimal price,
                   Integer stockQuantity, Long categoryId, String imageUrl, boolean featured,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl != null ? imageUrl : "/images/products/laptop.svg";
        this.featured = featured;
    }

    // Domain / Business Methods
    public boolean isAvailable() {
        return this.stockQuantity != null && this.stockQuantity > 0;
    }

    public boolean hasStock(int requestedQuantity) {
        return this.stockQuantity != null && this.stockQuantity >= requestedQuantity;
    }

    public void reduceStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalArgumentException("Insufficient inventory for product: " + this.name);
        }
        this.stockQuantity -= quantity;
    }

    public void addStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cannot add negative stock");
        }
        this.stockQuantity += quantity;
    }

    public String getStockStatusBadge() {
        if (stockQuantity == null || stockQuantity <= 0) {
            return "bg-danger"; // Out of stock
        } else if (stockQuantity <= 5) {
            return "bg-warning text-dark"; // Low stock
        } else {
            return "bg-success"; // In stock
        }
    }

    public String getStockStatusLabel() {
        if (stockQuantity == null || stockQuantity <= 0) {
            return "Out of Stock";
        } else if (stockQuantity <= 5) {
            return "Only " + stockQuantity + " left!";
        } else {
            return "In Stock (" + stockQuantity + ")";
        }
    }

    // Interface Implementation: Comparable (natural order by price ascending)
    @Override
    public int compareTo(Product other) {
        if (other == null) return 1;
        if (this.price == null && other.price == null) return 0;
        if (this.price == null) return -1;
        if (other.price == null) return 1;
        return this.price.compareTo(other.price);
    }

    // Getters and Setters
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", categoryId=" + categoryId +
                '}';
    }
}
