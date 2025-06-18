// src/main/java/com/financeapp/dao/TransactionDAO.java
package com.financeapp.dao;

import com.financeapp.model.Account;
import com.financeapp.model.Category;
import com.financeapp.model.Transaction;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) for managing Transaction entities in the database.
 * Handles CRUD operations and data retrieval for reports/charts.
 */
public class TransactionDAO {

    private static final Logger LOGGER = Logger.getLogger(TransactionDAO.class.getName());

    /**
     * Adds a new transaction to the database.
     * Also updates the current balance of the associated account.
     *
     * @param transaction The Transaction object to add.
     * @return true if the transaction was added successfully, false otherwise.
     */
    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, transaction.getUserId());
            pstmt.setInt(2, transaction.getAccountId());
            pstmt.setInt(3, transaction.getCategoryId());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setString(5, transaction.getType());
            pstmt.setString(6, transaction.getDescription());
            pstmt.setDate(7, Date.valueOf(transaction.getTransactionDate()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Get the generated transaction ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setTransactionId(generatedKeys.getInt(1));
                    }
                }

                // Update account balance
                updateAccountBalance(conn, transaction.getAccountId(), transaction.getAmount(), transaction.getType(), true);
                conn.commit(); // Commit transaction
                success = true;
            } else {
                conn.rollback(); // Rollback if no rows affected
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding transaction: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
                }
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // Restore auto-commit
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources: " + e.getMessage(), e);
            }
        }
        return success;
    }

    /**
     * Updates an existing transaction in the database.
     * Correctly adjusts old and new account balances.
     *
     * @param transaction The Transaction object with updated details.
     * @param oldAmount The original amount of the transaction before editing.
     * @param oldAccountId The original account ID before editing.
     * @param oldType The original type (Income/Expense) before editing.
     * @return true if the transaction was updated successfully, false otherwise.
     */
    public boolean updateTransaction(Transaction transaction, double oldAmount, int oldAccountId, String oldType) {
        String sql = "UPDATE transactions SET account_id = ?, category_id = ?, amount = ?, type = ?, description = ?, transaction_date = ? WHERE transaction_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Revert old balance changes from the original account
            updateAccountBalance(conn, oldAccountId, oldAmount, oldType, false); // false for reverse operation

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, transaction.getAccountId());
            pstmt.setInt(2, transaction.getCategoryId());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getType());
            pstmt.setString(5, transaction.getDescription());
            pstmt.setDate(6, Date.valueOf(transaction.getTransactionDate()));
            pstmt.setInt(7, transaction.getTransactionId());
            pstmt.setInt(8, transaction.getUserId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // 2. Apply new balance changes to the new/same account
                updateAccountBalance(conn, transaction.getAccountId(), transaction.getAmount(), transaction.getType(), true); // true for apply operation
                conn.commit(); // Commit transaction
                success = true;
            } else {
                conn.rollback(); // Rollback if no rows affected
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating transaction: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
                }
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // Restore auto-commit
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources: " + e.getMessage(), e);
            }
        }
        return success;
    }

    /**
     * Deletes a transaction from the database.
     * Also reverts the balance change from the associated account.
     *
     * @param transactionId The ID of the transaction to delete.
     * @param userId The ID of the user who owns the transaction.
     * @return true if the transaction was deleted successfully, false otherwise.
     */
    public boolean deleteTransaction(int transactionId, int userId) {
        String selectSql = "SELECT account_id, amount, type FROM transactions WHERE transaction_id = ? AND user_id = ?";
        String deleteSql = "DELETE FROM transactions WHERE transaction_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement selectPstmt = null;
        PreparedStatement deletePstmt = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // First, get transaction details to revert account balance
            selectPstmt = conn.prepareStatement(selectSql);
            selectPstmt.setInt(1, transactionId);
            selectPstmt.setInt(2, userId);
            rs = selectPstmt.executeQuery();

            if (rs.next()) {
                int accountId = rs.getInt("account_id");
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");

                // Delete the transaction
                deletePstmt = conn.prepareStatement(deleteSql);
                deletePstmt.setInt(1, transactionId);
                deletePstmt.setInt(2, userId);
                int affectedRows = deletePstmt.executeUpdate();

                if (affectedRows > 0) {
                    // Revert account balance change
                    updateAccountBalance(conn, accountId, amount, type, false); // false for reverse operation
                    conn.commit(); // Commit transaction
                    success = true;
                } else {
                    conn.rollback(); // Rollback if no rows affected
                }
            } else {
                conn.rollback(); // Rollback if transaction not found
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting transaction: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error during rollback: " + ex.getMessage(), ex);
                }
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // Restore auto-commit
                if (rs != null) rs.close();
                if (selectPstmt != null) selectPstmt.close();
                if (deletePstmt != null) deletePstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing resources: " + e.getMessage(), e);
            }
        }
        return success;
    }

    /**
     * Helper method to update account balance within a transaction.
     *
     * @param conn The active SQL Connection.
     * @param accountId The ID of the account to update.
     * @param amount The amount of the transaction.
     * @param type The type of the transaction ("Income" or "Expense").
     * @param applyOperation true to apply the transaction effect, false to reverse it.
     * @throws SQLException If a database access error occurs.
     */
    private void updateAccountBalance(Connection conn, int accountId, double amount, String type, boolean applyOperation) throws SQLException {
        String sql = "UPDATE accounts SET current_balance = current_balance + ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            double balanceChange = 0;
            // Determine the actual change based on type and operation (apply/reverse)
            if (type.equals("Income")) {
                balanceChange = applyOperation ? amount : -amount;
            } else if (type.equals("Expense")) {
                // For 'Expense', we subtract from checking/savings/cash, but add to credit card debt
                AccountDAO accountDAO = new AccountDAO(); // Instantiate AccountDAO to use its method
                String accountType = accountDAO.getAccountTypeById(conn, accountId); // Pass connection to avoid new one

                if ("Credit Card".equalsIgnoreCase(accountType)) {
                    // For credit card, expense increases the negative balance (e.g., -500 becomes -510)
                    balanceChange = applyOperation ? amount : -amount;
                } else {
                    // For other accounts (Checking, Savings, Cash), expense decreases balance
                    balanceChange = applyOperation ? -amount : amount;
                }
            }
            pstmt.setDouble(1, balanceChange);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        }
    }


    /**
     * Retrieves a specific transaction by its ID and user ID.
     *
     * @param transactionId The ID of the transaction to retrieve.
     * @param userId The ID of the user who owns the transaction.
     * @return The Transaction object, or null if not found.
     */
    public Transaction getTransactionById(int transactionId, int userId) {
        String sql = "SELECT t.*, c.category_name, a.account_name " +
                "FROM transactions t " +
                "JOIN categories c ON t.category_id = c.category_id " +
                "JOIN accounts a ON t.account_id = a.account_id " +
                "WHERE t.transaction_id = ? AND t.user_id = ?";
        Transaction transaction = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    transaction = new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getInt("user_id"),
                            rs.getInt("account_id"),
                            rs.getInt("category_id"),
                            rs.getDouble("amount"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    transaction.setCategoryName(rs.getString("category_name"));
                    transaction.setAccountName(rs.getString("account_name"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting transaction by ID: " + e.getMessage(), e);
        }
        return transaction;
    }

    /**
     * Retrieves all transactions for a specific user.
     * Joins with categories and accounts tables to get names.
     *
     * @param userId The ID of the user whose transactions to retrieve.
     * @return A list of Transaction objects.
     */
    public List<Transaction> getTransactionsByUserId(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.category_name, a.account_name " +
                "FROM transactions t " +
                "JOIN categories c ON t.category_id = c.category_id " +
                "JOIN accounts a ON t.account_id = a.account_id " +
                "WHERE t.user_id = ? ORDER BY t.transaction_date DESC, t.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("transaction_id"),
                            rs.getInt("user_id"),
                            rs.getInt("account_id"),
                            rs.getInt("category_id"),
                            rs.getDouble("amount"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getDate("transaction_date").toLocalDate(),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    transaction.setCategoryName(rs.getString("category_name"));
                    transaction.setAccountName(rs.getString("account_name"));
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by user ID: " + e.getMessage(), e);
        }
        return transactions;
    }

    /**
     * Retrieves all transactions in the database (for admin view).
     *
     * @return A list of all Transaction objects.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.category_name, a.account_name " +
                "FROM transactions t " +
                "JOIN categories c ON t.category_id = c.category_id " +
                "JOIN accounts a ON t.account_id = a.account_id " +
                "ORDER BY t.transaction_date DESC, t.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("user_id"),
                        rs.getInt("account_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getDate("transaction_date").toLocalDate(),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                transaction.setCategoryName(rs.getString("category_name"));
                transaction.setAccountName(rs.getString("account_name"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all transactions: " + e.getMessage(), e);
        }
        return transactions;
    }

    /**
     * Retrieves all transactions linked to a specific category ID.
     * Used for category deletion validation.
     *
     * @param categoryId The ID of the category.
     * @return A list of Transaction objects linked to the category.
     */
    public List<Transaction> getTransactionsByCategoryId(int categoryId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT transaction_id FROM transactions WHERE category_id = ?"; // Only select ID
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Only need the ID, no need to fully construct object
                    transactions.add(new Transaction(rs.getInt("transaction_id")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by category ID: " + e.getMessage(), e);
        }
        return transactions;
    }

    /**
     * Retrieves all transactions linked to a specific account ID.
     * Used for account deletion validation.
     *
     * @param accountId The ID of the account.
     * @return A list of Transaction objects linked to the account.
     */
    public List<Transaction> getTransactionsByAccountId(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT transaction_id FROM transactions WHERE account_id = ?"; // Only select ID
        try (Connection conn = DatabaseConnection.getConnection(); // Corrected here
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(rs.getInt("transaction_id"))); // Minimal object needed
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions by account ID: " + e.getMessage(), e);
        }
        return transactions;
    }


    // --- Methods for Dashboard Summary and Charts ---

    /**
     * Calculates the total income for a given user, month, and year.
     *
     * @param userId The ID of the user.
     * @param month The month (1-12).
     * @param year The year.
     * @return The total income.
     */
    public double getTotalIncomeForMonth(int userId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'Income' AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?";
        double totalIncome = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalIncome = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total income for month: " + e.getMessage(), e);
        }
        return totalIncome;
    }

    /**
     * Calculates the total expenses for a given user, month, and year.
     *
     * @param userId The ID of the user.
     * @param month The month (1-12).
     * @param year The year.
     * @return The total expenses.
     */
    public double getTotalExpensesForMonth(int userId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'Expense' AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?";
        double totalExpenses = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalExpenses = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total expenses for month: " + e.getMessage(), e);
        }
        return totalExpenses;
    }

    /**
     * Calculates the total expenses for a given category, month, and year for a specific user.
     * Used for budget tracking.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category.
     * @param month The month (1-12).
     * @param year The year.
     * @return The total expenses for that category, month, and year.
     */
    public double getTotalExpenseForCategoryMonthYear(int userId, int categoryId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t " +
                "WHERE t.user_id = ? AND t.category_id = ? AND t.type = 'Expense' " +
                "AND MONTH(t.transaction_date) = ? AND YEAR(t.transaction_date) = ?";
        double totalSpent = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, month);
            pstmt.setInt(4, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalSpent = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total expense for category month year: " + e.getMessage(), e);
        }
        return totalSpent;
    }

    /**
     * Retrieves the breakdown of expenses by category for a specific user
     * for the current month.
     * This is used for the Pie Chart.
     *
     * @param userId The ID of the user.
     * @return A map where keys are category names and values are total expense amounts.
     */
    public Map<String, Double> getExpenseCategoriesBreakdown(int userId) {
        Map<String, Double> breakdown = new HashMap<>();
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        String sql = "SELECT c.category_name, COALESCE(SUM(t.amount), 0) AS total_amount " +
                "FROM categories c " +
                "LEFT JOIN transactions t ON c.category_id = t.category_id " +
                "AND t.user_id = c.user_id " + // Ensure transactions belong to the same user
                "AND t.type = 'Expense' " +
                "AND MONTH(t.transaction_date) = ? " +
                "AND YEAR(t.transaction_date) = ? " +
                "WHERE c.user_id = ? AND c.category_type = 'Expense' " +
                "GROUP BY c.category_name " +
                "HAVING COALESCE(SUM(t.amount), 0) > 0"; // Only include categories with positive expenses
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentMonth);
            pstmt.setInt(2, currentYear);
            pstmt.setInt(3, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    breakdown.put(rs.getString("category_name"), rs.getDouble("total_amount"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting expense categories breakdown: " + e.getMessage(), e);
        }
        return breakdown;
    }


    /**
     * Retrieves total expenses for the last 6 months for a given user.
     * Used for the Bar Chart.
     *
     * @param userId The ID of the user.
     * @return A map where keys are "YYYY-MM" strings and values are total expense amounts.
     */
    public Map<String, Double> getMonthlyExpenses(int userId) {
        Map<String, Double> monthlyExpenses = new HashMap<>();
        // Query to get expenses grouped by YYYY-MM for the last 6 months
        String sql = "SELECT DATE_FORMAT(transaction_date, '%Y-%m') AS month_year, COALESCE(SUM(amount), 0) AS total_expense " +
                "FROM transactions " +
                "WHERE user_id = ? AND type = 'Expense' " +
                "AND transaction_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " + // Get data from last 6 months including current partial month
                "GROUP BY month_year " +
                "ORDER BY month_year ASC"; // Order chronologically

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    monthlyExpenses.put(rs.getString("month_year"), rs.getDouble("total_expense"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting monthly expenses: " + e.getMessage(), e);
        }
        return monthlyExpenses;
    }


    /**
     * Retrieves historical monthly spending for a specific category for the last N months.
     * Used for Weka prediction. The list is ordered from oldest to most recent.
     *
     * @param userId The ID of the user.
     * @param categoryId The ID of the category.
     * @param monthsBack The number of months back to retrieve data (e.g., 3 for last 3 months).
     * @return A list of monthly spending amounts, ordered from oldest to most recent.
     */
    public List<Double> getHistoricalMonthlySpendingForCategory(int userId, int categoryId, int monthsBack) {
        List<Double> historicalData = new ArrayList<>();
        // Query to get expenses for the given category for the last 'monthsBack' months
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total_expense " +
                "FROM transactions " +
                "WHERE user_id = ? AND category_id = ? AND type = 'Expense' " +
                "AND transaction_date <= CURDATE() " + // Only up to current date
                "AND transaction_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " + // Go back 'monthsBack' months
                "GROUP BY YEAR(transaction_date), MONTH(transaction_date) " +
                "ORDER BY YEAR(transaction_date) ASC, MONTH(transaction_date) ASC"; // Oldest to most recent

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, monthsBack); // Parameter for INTERVAL
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    historicalData.add(rs.getDouble("total_expense"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting historical monthly spending for category: " + e.getMessage(), e);
        }

        // Pad with zeros if not enough historical data
        // This ensures the list always has 'monthsBack' elements for Weka, even if data is sparse
        while (historicalData.size() < monthsBack) {
            historicalData.add(0, 0.0); // Add zeros to the beginning (oldest months)
        }
        // Ensure only 'monthsBack' elements, taking the most recent ones if more than 'monthsBack' are returned
        // This can happen if INTERVAL N MONTH includes more than N distinct month-year groups due to exact date ranges.
        if (historicalData.size() > monthsBack) {
            historicalData = historicalData.subList(historicalData.size() - monthsBack, historicalData.size());
        }

        return historicalData;
    }
}
