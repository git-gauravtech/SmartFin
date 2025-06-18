// src/main/java/com/financeapp/model/Transaction.java
package com.financeapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single financial transaction (income or expense).
 */
public class Transaction {
    private int transactionId;
    private int userId;
    private int accountId; // New: to link to an Account (Phase 3)
    private double amount;
    private String type; // "Income" or "Expense"
    private int categoryId; // New: Foreign key to categories table
    private String categoryName; // To easily display category name in UI without fetching Category object always
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;

    // Constructor for adding new transactions (without ID, createdAt)
    public Transaction(int userId, int accountId, double amount, String type, int categoryId, String description, LocalDate transactionDate) {
        this.userId = userId;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.description = description;
        this.transactionDate = transactionDate;
        // categoryName will be fetched/set by DAO or logic that knows the categoryId
    }

    // Full constructor for retrieving from database
    public Transaction(int transactionId, int userId, int accountId, double amount, String type, int categoryId,
                       String description, LocalDate transactionDate, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
        // categoryName will be fetched/set by DAO or logic that knows the categoryId
    }

    // Getters
    public int getTransactionId() {
        return transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public int getAccountId() {
        return accountId;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName; // This is a convenience field, typically set after fetching
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters (for updates)
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
