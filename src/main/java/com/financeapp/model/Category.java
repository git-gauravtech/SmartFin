// src/main/java/com/financeapp/model/Category.java
package com.financeapp.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a financial category (e.g., "Food", "Rent", "Salary").
 * Can be a default system category or a custom user-defined category.
 */
public class Category {
    private int categoryId;
    private int userId; // User who owns this category (0 for global defaults, though we're doing user-specific defaults)
    private String categoryName;
    private String categoryType; // "Income" or "Expense"
    private boolean isDefault; // True if it's a system-provided default category
    private LocalDateTime createdAt;

    // Constructor for adding new categories (without ID, createdAt)
    public Category(int userId, String categoryName, String categoryType, boolean isDefault) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.isDefault = isDefault;
    }

    // Full constructor for retrieving from database
    public Category(int categoryId, int userId, String categoryName, String categoryType, boolean isDefault, LocalDateTime createdAt) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    // Getters
    public int getCategoryId() {
        return categoryId;
    }

    public int getUserId() {
        return userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters (for updating properties)
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return categoryName; // For display in ComboBoxes
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return categoryId == category.categoryId; // Unique ID is sufficient for equality
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }
}
