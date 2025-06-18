// src/main/java/com/financeapp/dao/UserDAO.java
package com.financeapp.dao;

import com.financeapp.model.User;
import com.financeapp.utils.PasswordHasher; // Used for password hashing

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) for the User model.
 * Handles database operations related to user accounts (signup, login, retrieval).
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Registers a new user in the database.
     * Passwords are hashed and salted before storage.
     *
     * @param username The desired username.
     * @param password The plain-text password.
     * @param isAdmin  True if the user should be an admin, false otherwise.
     * @return True if registration is successful, false if username already exists or an error occurs.
     */
    public boolean registerUser(String username, String password, boolean isAdmin) {
        // Check if username already exists
        if (getUserByUsername(username) != null) {
            LOGGER.log(Level.WARNING, "Registration failed: Username '" + username + "' already exists.");
            return false;
        }

        // Hash the password
        String salt = PasswordHasher.generateSalt();
        String hashedPassword = PasswordHasher.hashPassword(password, salt);

        String sql = "INSERT INTO users (username, password_hash, password_salt, is_admin) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);
            pstmt.setBoolean(4, isAdmin);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        LOGGER.log(Level.INFO, "User '" + username + "' registered successfully with ID: " + generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering user '" + username + "'", e);
        }
        return false;
    }

    /**
     * Authenticates a user by checking their username and password.
     *
     * @param username The username provided by the user.
     * @param password The plain-text password provided by the user.
     * @return The User object if authentication is successful, null otherwise.
     */
    public User loginUser(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null) {
            // Verify the provided password against the stored hash and salt
            String storedPasswordHash = user.getPasswordHash();
            String storedPasswordSalt = user.getPasswordSalt();

            if (PasswordHasher.verifyPassword(password, storedPasswordHash, storedPasswordSalt)) {
                LOGGER.log(Level.INFO, "User '" + username + "' logged in successfully.");
                return user;
            } else {
                LOGGER.log(Level.WARNING, "Login failed for user '" + username + "': Incorrect password.");
            }
        } else {
            LOGGER.log(Level.WARNING, "Login failed: Username '" + username + "' not found.");
        }
        return null;
    }

    /**
     * Retrieves a user from the database by their username.
     *
     * @param username The username to search for.
     * @return The User object if found, null otherwise.
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, password_salt, is_admin, created_at FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("password_salt"),
                            rs.getBoolean("is_admin"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user by username '" + username + "'", e);
        }
        return null;
    }

    /**
     * Retrieves a user from the database by their user ID.
     *
     * @param userId The user ID to search for.
     * @return The User object if found, null otherwise.
     */
    public User getUserByUserId(int userId) {
        String sql = "SELECT user_id, username, password_hash, password_salt, is_admin, created_at FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("password_salt"),
                            rs.getBoolean("is_admin"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user by user ID '" + userId + "'", e);
        }
        return null;
    }


    /**
     * Retrieves all users from the database.
     * This method is primarily for the Admin Dashboard.
     *
     * @return A list of all User objects.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, password_salt, is_admin, created_at FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("password_salt"),
                        rs.getBoolean("is_admin"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all users", e);
        }
        return users;
    }

    /**
     * Deletes a user from the database by their user ID.
     * This will also trigger CASCADE DELETE on related transactions and budgets.
     *
     * @param userId The ID of the user to delete.
     * @return True if deletion is successful, false otherwise.
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "User with ID " + userId + " deleted successfully.");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user with ID " + userId, e);
        }
        return false;
    }

    /**
     * Updates a user's details, specifically if they are an admin or not.
     * Passwords are not updated via this method.
     *
     * @param user The User object with updated details (userId is crucial).
     * @return True if update is successful, false otherwise.
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, is_admin = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setBoolean(2, user.isAdmin());
            pstmt.setInt(3, user.getUserId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "User with ID " + user.getUserId() + " updated successfully.");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user with ID " + user.getUserId(), e);
        }
        return false;
    }
}
