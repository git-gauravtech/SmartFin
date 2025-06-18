// src/main/java/com/financeapp/dao/AccountDAO.java
package com.financeapp.dao;

import com.financeapp.model.Account;
import com.financeapp.utils.AlertUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for managing Account data in the database.
 * Handles CRUD operations for accounts and balance updates.
 */
public class AccountDAO {

    private static final Logger LOGGER = Logger.getLogger(AccountDAO.class.getName());

    // Default accounts that every new user will get if they don't have any
    private static final List<Account> DEFAULT_ACCOUNTS = Arrays.asList(
            new Account(0, "Cash", 0.0, "Cash"),
            new Account(0, "Checking Account", 0.0, "Checking"),
            new Account(0, "Savings Account", 0.0, "Savings")
    );

    /**
     * Retrieves an account by its ID.
     * @param accountId The ID of the account.
     * @return The Account object, or null if not found.
     */
    public Account getAccountById(int accountId) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Account(
                        rs.getInt("account_id"),
                        rs.getInt("user_id"),
                        rs.getString("account_name"),
                        rs.getDouble("initial_balance"),
                        rs.getDouble("current_balance"),
                        rs.getString("account_type"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving account by ID: " + accountId, e);
        }
        return null;
    }

    /**
     * Retrieves all accounts for a specific user.
     * @param userId The ID of the user.
     * @return A list of Account objects.
     */
    public List<Account> getAccountsByUserId(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY account_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                accounts.add(new Account(
                        rs.getInt("account_id"),
                        rs.getInt("user_id"),
                        rs.getString("account_name"),
                        rs.getDouble("initial_balance"),
                        rs.getDouble("current_balance"),
                        rs.getString("account_type"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving accounts for user ID: " + userId, e);
        }
        return accounts;
    }

    /**
     * Adds a new account to the database.
     * @param account The Account object to add.
     * @return True if successful, false otherwise.
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
                LOGGER.log(Level.INFO, "Account added: " + account.getAccountName());
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            LOGGER.log(Level.WARNING, "Attempted to add duplicate account for user: " + account.getAccountName(), e);
            AlertUtil.showWarning("Account Exists", "Account Already Exists", "An account with this name already exists for you.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding account: " + account.getAccountName(), e);
        }
        return false;
    }

    /**
     * Updates an existing account in the database.
     * Can update name, type, and initial balance (which also adjusts current balance).
     * @param account The Account object with updated details.
     * @return True if successful, false otherwise.
     */
    public boolean updateAccount(Account account) {
        String sql = "UPDATE accounts SET account_name = ?, account_type = ?, initial_balance = ?, current_balance = ? WHERE account_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getAccountName());
            pstmt.setString(2, account.getAccountType());
            pstmt.setDouble(3, account.getInitialBalance());
            pstmt.setDouble(4, account.getCurrentBalance()); // Assuming current_balance is already adjusted by logic
            pstmt.setInt(5, account.getAccountId());
            pstmt.setInt(6, account.getUserId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "Account updated: " + account.getAccountName());
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            LOGGER.log(Level.WARNING, "Attempted to update account to a duplicate name: " + account.getAccountName(), e);
            AlertUtil.showWarning("Update Failed", "Account Name Exists", "An account with this new name already exists for you.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating account: " + account.getAccountName(), e);
        }
        return false;
    }

    /**
     * Deletes an account from the database.
     * @param accountId The ID of the account to delete.
     * @param userId The ID of the user who owns the account.
     * @return True if successful, false otherwise.
     */
    public boolean deleteAccount(int accountId, int userId) {
        String sql = "DELETE FROM accounts WHERE account_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "Account deleted: " + accountId + " for user " + userId);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting account: " + accountId, e);
        }
        return false;
    }

    /**
     * Updates the current balance of an account. This is called by TransactionDAO.
     * @param conn The database connection (for transactional consistency).
     * @param accountId The ID of the account to update.
     * @param amount The amount of the transaction.
     * @param type The type of transaction ("Income" or "Expense").
     * @return True if successful, false otherwise.
     */
    public boolean updateAccountBalance(Connection conn, int accountId, double amount, String type) throws SQLException {
        String updateSql;
        if ("Income".equals(type)) {
            updateSql = "UPDATE accounts SET current_balance = current_balance + ? WHERE account_id = ?";
        } else { // Expense
            updateSql = "UPDATE accounts SET current_balance = current_balance - ? WHERE account_id = ?";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "Account balance updated for account ID: " + accountId + " by " + type + " of " + amount);
                return true;
            }
        }
        LOGGER.log(Level.WARNING, "Failed to update account balance for account ID: " + accountId);
        return false;
    }

    /**
     * Checks if a user has any accounts defined (including default ones).
     * @param userId The ID of the user.
     * @return True if the user has at least one account, false otherwise.
     */
    public boolean hasAccounts(int userId) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if user has accounts: " + userId, e);
        }
        return false;
    }

    /**
     * Inserts a predefined set of default accounts for a user if they don't have any accounts yet.
     * This method ensures new users have a usable set of accounts without manual entry.
     * @param userId The ID of the user for whom to insert default accounts.
     */
    public void insertDefaultAccounts(int userId) {
        if (hasAccounts(userId)) {
            LOGGER.log(Level.INFO, "User " + userId + " already has accounts. Skipping default insertion.");
            return;
        }

        String sql = "INSERT INTO accounts (user_id, account_name, initial_balance, current_balance, account_type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Account defaultAcc : DEFAULT_ACCOUNTS) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, defaultAcc.getAccountName());
                pstmt.setDouble(3, defaultAcc.getInitialBalance());
                pstmt.setDouble(4, defaultAcc.getCurrentBalance());
                pstmt.setString(5, defaultAcc.getAccountType());
                pstmt.addBatch(); // Add to batch for efficiency
            }
            pstmt.executeBatch(); // Execute all inserts
            LOGGER.log(Level.INFO, "Default accounts inserted for user: " + userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting default accounts for user: " + userId, e);
        }
    }
}
