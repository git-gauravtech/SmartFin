// src/main/java/com/financeapp/model/Transaction.java
package com.financeapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a financial transaction within the system.
 */
public class Transaction {
    private int transactionId;
    private int userId;
    private int accountId;
    private int categoryId;
    private double amount;
    private String type; // "Income" or "Expense"
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;

    // Additional fields for displaying names in UI tables without extra lookups in FXML
    private String categoryName;
    private String accountName;

    /**
     * Full constructor for retrieving existing transactions from the database.
     */
    public Transaction(int transactionId, int userId, int accountId, int categoryId, double amount, String type, String description, LocalDate transactionDate, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
    }

    /**
     * Constructor for creating new transactions (ID will be auto-generated).
     */
    public Transaction(int userId, int accountId, int categoryId, double amount, String type, String description, LocalDate transactionDate) {
        this(-1, userId, accountId, categoryId, amount, type, description, transactionDate, LocalDateTime.now());
    }

    /**
     * Minimal constructor for DAO validation (e.g., checking if transactions exist for an account/category).
     */
    public Transaction(int transactionId) {
        this.transactionId = transactionId;
    }


    // --- Getters and Setters ---

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Getters and Setters for UI display names ---
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
