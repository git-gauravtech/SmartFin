// src/main/java/com/financeapp/model/Account.java
package com.financeapp.model;

import java.time.LocalDateTime;

/**
 * Represents a financial account (e.g., Checking, Savings, Credit Card).
 */
public class Account {
    private int accountId;
    private int userId;
    private String accountName;
    private double initialBalance;
    private double currentBalance;
    private String accountType; // e.g., "Checking", "Savings", "Credit Card", "Cash"
    private LocalDateTime createdAt;

    /**
     * Full constructor for retrieving accounts from the database.
     */
    public Account(int accountId, int userId, String accountName, double initialBalance, double currentBalance, String accountType, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountName = accountName;
        this.initialBalance = initialBalance;
        this.currentBalance = currentBalance;
        this.accountType = accountType;
        this.createdAt = createdAt;
    }

    /**
     * Constructor for creating a new account (ID will be auto-generated).
     * currentBalance is typically initialized with initialBalance.
     */
    public Account(int userId, String accountName, double initialBalance, String accountType) {
        this(-1, userId, accountName, initialBalance, initialBalance, accountType, LocalDateTime.now());
    }

    // --- Getters and Setters ---

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
