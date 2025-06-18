// src/main/java/com/financeapp/dao/BudgetDAO.java
package com.financeapp.dao;

import com.financeapp.model.Budget;
import com.financeapp.model.Category;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for managing Budget data in the database.
 * Handles CRUD operations for budgets.
 */
public class BudgetDAO {

    private static final Logger LOGGER = Logger.getLogger(BudgetDAO.class.getName());
    private final CategoryDAO categoryDAO = new CategoryDAO(); // To fetch category name by ID

    /**
     * Saves a new budget or updates an existing one if a budget for the same
     * user, category, month, and year already exists.
     * @param budget The Budget object to save or update.
     * @return True if successful, false otherwise.
     */
    public boolean saveOrUpdateBudget(Budget budget) {
        // Check if a budget already exists for this user, category, month, and year
        Budget existingBudget = getBudgetByUserIdCategoryIdMonthYear(
                budget.getUserId(), budget.getCategoryId(), budget.getMonth(), budget.getYear());

        if (existingBudget != null) {
            // Update existing budget
            budget.setBudgetId(existingBudget.getBudgetId()); // Ensure the ID is set for update operation
            return updateBudget(budget);
        } else {
            // Add new budget
            return addBudget(budget);
        }
    }

    /**
     * Adds a new budget to the database.
     * @param budget The Budget object to add.
     * @return True if successful, false otherwise.
     */
    private boolean addBudget(Budget budget) {
        String sql = "INSERT INTO budgets (user_id, category_id, amount_limit, month, year) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, budget.getUserId());
            pstmt.setInt(2, budget.getCategoryId());
            pstmt.setDouble(3, budget.getAmountLimit());
            pstmt.setInt(4, budget.getMonth());
            pstmt.setInt(5, budget.getYear());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        budget.setBudgetId(generatedKeys.getInt(1));
                    }
                }
                LOGGER.log(Level.INFO, "Budget added for user " + budget.getUserId() + ", category ID " + budget.getCategoryId());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding budget for user " + budget.getUserId() + ", category ID " + budget.getCategoryId(), e);
        }
        return false;
    }

    /**
     * Updates an existing budget in the database.
     * @param budget The Budget object with updated details.
     * @return True if successful, false otherwise.
     */
    public boolean updateBudget(Budget budget) {
        String sql = "UPDATE budgets SET amount_limit = ? WHERE budget_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, budget.getAmountLimit());
            pstmt.setInt(2, budget.getBudgetId());
            pstmt.setInt(3, budget.getUserId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "Budget updated for ID: " + budget.getBudgetId());
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating budget with ID: " + budget.getBudgetId(), e);
        }
        return false;
    }

    /**
     * Deletes a budget from the database.
     * @param budgetId The ID of the budget to delete.
     * @param userId The ID of the user who owns the budget.
     * @return True if successful, false otherwise.
     */
    public boolean deleteBudget(int budgetId, int userId) {
        String sql = "DELETE FROM budgets WHERE budget_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, budgetId);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "Budget deleted: " + budgetId + " for user " + userId);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting budget: " + budgetId, e);
        }
        return false;
    }

    /**
     * Retrieves a single budget by its ID.
     * @param budgetId The ID of the budget.
     * @return The Budget object, or null if not found.
     */
    public Budget getBudgetById(int budgetId) {
        String sql = "SELECT b.*, c.category_name FROM budgets b JOIN categories c ON b.category_id = c.category_id WHERE b.budget_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, budgetId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Budget budget = new Budget(
                        rs.getInt("budget_id"),
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount_limit"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                budget.setCategoryName(rs.getString("category_name"));
                return budget;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving budget by ID: " + budgetId, e);
        }
        return null;
    }

    /**
     * Retrieves a budget for a specific user, category, month, and year.
     * Used to check for existing budgets before adding.
     * @param userId The ID of the user.
     * @param categoryId The ID of the category.
     * @param month The month (1-12).
     * @param year The year.
     * @return The Budget object, or null if not found.
     */
    public Budget getBudgetByUserIdCategoryIdMonthYear(int userId, int categoryId, int month, int year) {
        String sql = "SELECT b.*, c.category_name FROM budgets b JOIN categories c ON b.category_id = c.category_id WHERE b.user_id = ? AND b.category_id = ? AND b.month = ? AND b.year = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, month);
            pstmt.setInt(4, year);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Budget budget = new Budget(
                        rs.getInt("budget_id"),
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount_limit"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                budget.setCategoryName(rs.getString("category_name"));
                return budget;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving budget by user, category, month, year.", e);
        }
        return null;
    }

    /**
     * Retrieves all budgets for a specific user.
     * @param userId The ID of the user.
     * @return A list of Budget objects.
     */
    public List<Budget> getBudgetsByUserId(int userId) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT b.*, c.category_name FROM budgets b JOIN categories c ON b.category_id = c.category_id WHERE b.user_id = ? ORDER BY b.year DESC, b.month DESC, c.category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Budget budget = new Budget(
                        rs.getInt("budget_id"),
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount_limit"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                budget.setCategoryName(rs.getString("category_name"));
                budgets.add(budget);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving budgets for user ID: " + userId, e);
        }
        return budgets;
    }

    /**
     * Retrieves all budgets from the database (for admin view).
     * @return A list of all Budget objects.
     */
    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT b.*, c.category_name FROM budgets b JOIN categories c ON b.category_id = c.category_id ORDER BY b.year DESC, b.month DESC, c.category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Budget budget = new Budget(
                        rs.getInt("budget_id"),
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount_limit"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                budget.setCategoryName(rs.getString("category_name"));
                budgets.add(budget);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all budgets.", e);
        }
        return budgets;
    }

    /**
     * Retrieves budgets linked to a specific category ID.
     * Used for category deletion check.
     * @param categoryId The ID of the category.
     * @return A list of budgets linked to the category.
     */
    public List<Budget> getBudgetsByCategoryId(int categoryId) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Budget budget = new Budget(
                        rs.getInt("budget_id"),
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount_limit"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                // Populate category name for consistency
                Category category = categoryDAO.getCategoryById(budget.getCategoryId());
                if (category != null) {
                    budget.setCategoryName(category.getCategoryName());
                }
                budgets.add(budget);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving budgets by category ID: " + categoryId, e);
        }
        return budgets;
    }
}
