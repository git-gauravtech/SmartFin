// src/main/java/com/financeapp/model/User.java
package com.financeapp.model;

import java.time.LocalDateTime;

/**
 * Represents a User in the finance management system.
 * This class holds user data and acts as a simple POJO (Plain Old Java Object).
 */
public class User {
    private int userId;
    private String username;
    private String passwordHash; // Hashed password
    private String passwordSalt; // Salt used for hashing
    private boolean isAdmin;     // Flag to identify admin users
    private LocalDateTime createdAt;

    // Constructor for creating a new user (before saving to DB, id is 0)
    public User(String username, String passwordHash, String passwordSalt, boolean isAdmin) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.isAdmin = isAdmin;
        this.createdAt = LocalDateTime.now(); // Set current time on creation
    }

    // Constructor for retrieving user from DB (id is known)
    public User(int userId, String username, String passwordHash, String passwordSalt, boolean isAdmin, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.isAdmin = isAdmin;
        this.createdAt = createdAt;
    }

    // --- Getters ---
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // --- Setters ---
    // Note: userId, passwordHash, passwordSalt, createdAt are typically not set after object creation
    // from outside, but may be needed internally by DAOs.
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", isAdmin=" + isAdmin +
                ", createdAt=" + createdAt +
                '}';
    }
}
