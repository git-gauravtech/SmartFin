// src/main/java/com/financeapp/model/Budget.java
package com.financeapp.model;

import java.time.LocalDateTime;

/**
 * Represents a monthly budget for a specific category.
 */
public class Budget {
    private int budgetId;
    private int userId;
    private int categoryId; // New: Foreign key to categories table
    private String categoryName; // To easily display category name in UI
    private double amountLimit;
    private int month;
    private int year;
    private LocalDateTime createdAt;

    // Constructor for adding new budgets (without ID, createdAt)
    public Budget(int userId, int categoryId, double amountLimit, int month, int year) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amountLimit = amountLimit;
        this.month = month;
        this.year = year;
    }

    // Full constructor for retrieving from database
    public Budget(int budgetId, int userId, int categoryId, double amountLimit, int month, int year, LocalDateTime createdAt) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amountLimit = amountLimit;
        this.month = month;
        this.year = year;
        this.createdAt = createdAt;
    }

    // Getters
    public int getBudgetId() {
        return budgetId;
    }

    public int getUserId() {
        return userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName; // This is a convenience field, typically set after fetching
    }

    public double getAmountLimit() {
        return amountLimit;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters (for updates)
    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAmountLimit(double amountLimit) {
        this.amountLimit = amountLimit;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
