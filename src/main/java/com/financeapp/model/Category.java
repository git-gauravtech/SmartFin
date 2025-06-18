// src/main/java/com/financeapp/model/Category.java
package com.financeapp.model;

import java.time.LocalDateTime;

/**
 * Represents an income or expense category.
 */
public class Category {
    private int categoryId;
    private int userId;
    private String categoryName;
    private String categoryType; // "Income" or "Expense"
    private boolean isDefault; // true if it's a system-provided default category
    private LocalDateTime createdAt;

    /**
     * Full constructor for retrieving categories from the database.
     */
    public Category(int categoryId, int userId, String categoryName, String categoryType, boolean isDefault, LocalDateTime createdAt) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    /**
     * Constructor for creating a new custom category (ID will be auto-generated).
     */
    public Category(int userId, String categoryName, String categoryType) {
        this(-1, userId, categoryName, categoryType, false, LocalDateTime.now()); // New categories are not default
    }

    /**
     * Constructor used by DAOs/Forms that might not have userId readily available or don't need createdAt yet.
     * This is typically for updates or temporary objects.
     * Assuming categoryId might be known if editing, userId might be 0 for a dummy value if not needed immediately.
     */
    public Category(int categoryId, String categoryName, String categoryType, boolean isDefault) {
        this(categoryId, 0, categoryName, categoryType, isDefault, LocalDateTime.now()); // Default userId to 0 or another placeholder
    }

    // --- Getters and Setters ---

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
