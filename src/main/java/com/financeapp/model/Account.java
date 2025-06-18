// src/main/java/com/financeapp/model/Account.java
package com.financeapp.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user's financial account (e.g., Checking, Savings, Credit Card).
 */
public class Account {
    private int accountId;
    private int userId;
    private String accountName;
    private double initialBalance;
    private double currentBalance;
    private String accountType; // e.g., "Checking", "Savings", "Credit Card", "Cash"
    private LocalDateTime createdAt;

    // Constructor for adding new accounts (without ID, createdAt, currentBalance)
    public Account(int userId, String accountName, double initialBalance, String accountType) {
        this.userId = userId;
        this.accountName = accountName;
        this.initialBalance = initialBalance;
        this.currentBalance = initialBalance; // currentBalance starts as initialBalance
        this.accountType = accountType;
    }

    // Full constructor for retrieving from database
    public Account(int accountId, int userId, String accountName, double initialBalance, double currentBalance, String accountType, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountName = accountName;
        this.initialBalance = initialBalance;
        this.currentBalance = currentBalance;
        this.accountType = accountType;
        this.createdAt = createdAt;
    }

    // Getters
    public int getAccountId() {
        return accountId;
    }

    public int getUserId() {
        return userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public String getAccountType() {
        return accountType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters (for updating properties, especially currentBalance)
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return accountName + " (" + accountType + ")"; // For display in ComboBoxes
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountId == account.accountId; // Unique ID is sufficient for equality
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}
