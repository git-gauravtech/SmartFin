// src/main/java/com/financeapp/dao/AccountDAO.java
package com.financeapp.dao;

import com.financeapp.model.Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) for managing Account entities in the database.
 */
public class AccountDAO {

    private static final Logger LOGGER = Logger.getLogger(AccountDAO.class.getName());

    /**
     * Adds a new account to the database.
     *
     * @param account The Account object to add.
     * @return true if the account was added successfully, false otherwise.
     */
    public boolean addAccount(Account account) {
        String sql = "INSERT INTO accounts (user_id, account_name, initial_balance, current_balance, account_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, account.getUserId());
            pstmt.setString(2, account.getAccountName());
            pstmt.setDouble(3, account.getInitialBalance());
            pstmt.setDouble(4, account.getCurrentBalance());
            pstmt.setString(5, account.getAccountType());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        account.setAccountId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding account: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Updates an existing account in the database.
     *
     * @param account The Account object with updated details.
     * @return true if the account was updated successfully, false otherwise.
     */
    public boolean updateAccount(Account account) {
        String sql = "UPDATE accounts SET account_name = ?, initial_balance = ?, current_balance = ?, account_type = ? WHERE account_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getAccountName());
            pstmt.setDouble(2, account.getInitialBalance());
            pstmt.setDouble(3, account.getCurrentBalance());
            pstmt.setString(4, account.getAccountType());
            pstmt.setInt(5, account.getAccountId());
            pstmt.setInt(6, account.getUserId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating account: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Deletes an account from the database.
     *
     * @param accountId The ID of the account to delete.
     * @param userId The ID of the user who owns the account.
     * @return true if the account was deleted successfully, false otherwise.
     */
    public boolean deleteAccount(int accountId, int userId) {
        String sql = "DELETE FROM accounts WHERE account_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting account: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Retrieves a specific account by its ID.
     *
     * @param accountId The ID of the account to retrieve.
     * @return The Account object, or null if not found.
     */
    public Account getAccountById(int accountId) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        Account account = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    account = new Account(
                            rs.getInt("account_id"),
                            rs.getInt("user_id"),
                            rs.getString("account_name"),
                            rs.getDouble("initial_balance"),
                            rs.getDouble("current_balance"),
                            rs.getString("account_type"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting account by ID: " + e.getMessage(), e);
        }
        return account;
    }

    /**
     * Retrieves the account type for a given account ID using an existing connection.
     * This is a helper method primarily for TransactionDAO's balance updates.
     *
     * @param conn The active SQL Connection.
     * @param accountId The ID of the account.
     * @return The account type (e.g., "Checking", "Credit Card"), or null if not found.
     * @throws SQLException If a database access error occurs.
     */
    public String getAccountTypeById(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT account_type FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("account_type");
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all accounts for a specific user.
     *
     * @param userId The ID of the user whose accounts to retrieve.
     * @return A list of Account objects.
     */
    public List<Account> getAccountsByUserId(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account(
                            rs.getInt("account_id"),
                            rs.getInt("user_id"),
                            rs.getString("account_name"),
                            rs.getDouble("initial_balance"),
                            rs.getDouble("current_balance"),
                            rs.getString("account_type"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting accounts by user ID: " + e.getMessage(), e);
        }
        return accounts;
    }
}
