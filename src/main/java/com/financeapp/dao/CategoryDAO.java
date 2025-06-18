// src/main/java/com/financeapp/dao/CategoryDAO.java
package com.financeapp.dao;

import com.financeapp.model.Category;
import com.financeapp.utils.AlertUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Data Access Object for managing Category data in the database.
 * Handles CRUD operations for categories and provides default category management.
 */
public class CategoryDAO {

    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());

    // Default categories that every new user will get if they don't have any
    private static final List<Category> DEFAULT_EXPENSE_CATEGORIES = Arrays.asList(
            new Category(0, "Food", "Expense", true),
            new Category(0, "Transport", "Expense", true),
            new Category(0, "Utilities", "Expense", true),
            new Category(0, "Rent", "Expense", true),
            new Category(0, "Shopping", "Expense", true),
            new Category(0, "Entertainment", "Expense", true),
            new Category(0, "Healthcare", "Expense", true),
            new Category(0, "Education", "Expense", true),
            new Category(0, "Donations", "Expense", true),
            new Category(0, "Personal Care", "Expense", true),
            new Category(0, "Travel", "Expense", true),
            new Category(0, "Bills", "Expense", true),
            new Category(0, "Miscellaneous", "Expense", true)
    );

    private static final List<Category> DEFAULT_INCOME_CATEGORIES = Arrays.asList(
            new Category(0, "Salary", "Income", true),
            new Category(0, "Freelance", "Income", true),
            new Category(0, "Investments", "Income", true),
            new Category(0, "Gifts", "Income", true),
            new Category(0, "Bonus", "Income", true),
            new Category(0, "Other Income", "Income", true)
    );

    /**
     * Retrieves a category by its ID.
     * @param categoryId The ID of the category.
     * @return The Category object, or null if not found.
     */
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Category(
                        rs.getInt("category_id"),
                        rs.getInt("user_id"),
                        rs.getString("category_name"),
                        rs.getString("category_type"),
                        rs.getBoolean("is_default"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving category by ID: " + categoryId, e);
        }
        return null;
    }

    /**
     * Retrieves a category by user ID and category name.
     * @param userId The ID of the user.
     * @param categoryName The name of the category.
     * @return The Category object, or null if not found.
     */
    public Category getCategoryByUserIdAndName(int userId, String categoryName) {
        String sql = "SELECT * FROM categories WHERE user_id = ? AND category_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, categoryName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Category(
                        rs.getInt("category_id"),
                        rs.getInt("user_id"),
                        rs.getString("category_name"),
                        rs.getString("category_type"),
                        rs.getBoolean("is_default"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving category by user ID and name: " + userId + ", " + categoryName, e);
        }
        return null;
    }

    /**
     * Retrieves all categories for a specific user, including default categories.
     * @param userId The ID of the user.
     * @return A list of Category objects.
     */
    public List<Category> getCategoriesByUserId(int userId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ? ORDER BY category_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getInt("user_id"),
                        rs.getString("category_name"),
                        rs.getString("category_type"),
                        rs.getBoolean("is_default"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving categories for user ID: " + userId, e);
        }
        return categories;
    }

    /**
     * Retrieves categories for a specific user filtered by type ("Income" or "Expense").
     * @param userId The ID of the user.
     * @param type The type of category ("Income" or "Expense").
     * @return A list of Category objects.
     */
    public List<Category> getCategoriesByUserIdAndType(int userId, String type) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ? AND category_type = ? ORDER BY category_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getInt("user_id"),
                        rs.getString("category_name"),
                        rs.getString("category_type"),
                        rs.getBoolean("is_default"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving categories for user ID " + userId + " and type " + type, e);
        }
        return categories;
    }

    /**
     * Adds a new category to the database.
     * @param category The Category object to add.
     * @return True if successful, false otherwise.
     */
    public boolean addCategory(Category category) {
        String sql = "INSERT INTO categories (user_id, category_name, category_type, is_default) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, category.getUserId());
            pstmt.setString(2, category.getCategoryName());
            pstmt.setString(3, category.getCategoryType());
            pstmt.setBoolean(4, category.isDefault());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setCategoryId(generatedKeys.getInt(1));
                    }
                }
                LOGGER.log(Level.INFO, "Category added: " + category.getCategoryName());
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            LOGGER.log(Level.WARNING, "Attempted to add duplicate category for user: " + category.getCategoryName(), e);
            AlertUtil.showWarning("Category Exists", "Category Already Exists", "A category with this name already exists for you.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding category: " + category.getCategoryName(), e);
        }
        return false;
    }

    /**
     * Updates an existing category in the database.
     * @param category The Category object with updated details.
     * @return True if successful, false otherwise.
     */
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET category_name = ?, category_type = ? WHERE category_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getCategoryType());
            pstmt.setInt(3, category.getCategoryId());
            pstmt.setInt(4, category.getUserId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "Category updated: " + category.getCategoryName());
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            LOGGER.log(Level.WARNING, "Attempted to update category to a duplicate name: " + category.getCategoryName(), e);
            AlertUtil.showWarning("Update Failed", "Category Name Exists", "A category with this new name already exists for you.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating category: " + category.getCategoryName(), e);
        }
        return false;
    }

    /**
     * Deletes a category from the database.
     * @param categoryId The ID of the category to delete.
     * @param userId The ID of the user who owns the category.
     * @return True if successful, false otherwise.
     */
    public boolean deleteCategory(int categoryId, int userId) {
        String sql = "DELETE FROM categories WHERE category_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "Category deleted: " + categoryId + " for user " + userId);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting category: " + categoryId, e);
        }
        return false;
    }

    /**
     * Checks if a user has any categories defined (including default ones).
     * @param userId The ID of the user.
     * @return True if the user has at least one category, false otherwise.
     */
    public boolean hasCategories(int userId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if user has categories: " + userId, e);
        }
        return false;
    }

    /**
     * Inserts a predefined set of default categories for a user if they don't have any categories yet.
     * This method ensures new users have a usable set of categories without manual entry.
     * @param userId The ID of the user for whom to insert default categories.
     */
    public void insertDefaultCategories(int userId) {
        if (hasCategories(userId)) {
            LOGGER.log(Level.INFO, "User " + userId + " already has categories. Skipping default insertion.");
            return;
        }

        List<Category> allDefaultCategories = new ArrayList<>();
        allDefaultCategories.addAll(DEFAULT_EXPENSE_CATEGORIES);
        allDefaultCategories.addAll(DEFAULT_INCOME_CATEGORIES);

        String sql = "INSERT INTO categories (user_id, category_name, category_type, is_default) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Category defaultCat : allDefaultCategories) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, defaultCat.getCategoryName());
                pstmt.setString(3, defaultCat.getCategoryType());
                pstmt.setBoolean(4, true); // Always true for default categories
                pstmt.addBatch(); // Add to batch for efficiency
            }
            pstmt.executeBatch(); // Execute all inserts
            LOGGER.log(Level.INFO, "Default categories inserted for user: " + userId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting default categories for user: " + userId, e);
        }
    }
}
