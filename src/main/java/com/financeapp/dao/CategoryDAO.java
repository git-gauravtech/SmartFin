// src/main/java/com/financeapp/dao/CategoryDAO.java
package com.financeapp.dao;

import com.financeapp.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) for managing Category entities in the database.
 */
public class CategoryDAO {

    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());

    /**
     * Adds a new category to the database.
     *
     * @param category The Category object to add.
     * @return true if the category was added successfully, false otherwise.
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
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding category: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Updates an existing category in the database.
     *
     * @param category The Category object with updated details.
     * @return true if the category was updated successfully, false otherwise.
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
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating category: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Deletes a category from the database.
     *
     * @param categoryId The ID of the category to delete.
     * @param userId The ID of the user who owns the category.
     * @return true if the category was deleted successfully, false otherwise.
     */
    public boolean deleteCategory(int categoryId, int userId) {
        String sql = "DELETE FROM categories WHERE category_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting category: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Retrieves a specific category by its ID.
     *
     * @param categoryId The ID of the category to retrieve.
     * @return The Category object, or null if not found.
     */
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        Category category = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Use the full constructor
                    category = new Category(
                            rs.getInt("category_id"),
                            rs.getInt("user_id"),
                            rs.getString("category_name"),
                            rs.getString("category_type"),
                            rs.getBoolean("is_default"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting category by ID: " + e.getMessage(), e);
        }
        return category;
    }

    /**
     * Retrieves all categories for a specific user.
     *
     * @param userId The ID of the user whose categories to retrieve.
     * @return A list of Category objects.
     */
    public List<Category> getCategoriesByUserId(int userId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ? ORDER BY category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Use the full constructor
                    Category category = new Category(
                            rs.getInt("category_id"),
                            rs.getInt("user_id"),
                            rs.getString("category_name"),
                            rs.getString("category_type"),
                            rs.getBoolean("is_default"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting categories by user ID: " + e.getMessage(), e);
        }
        return categories;
    }

    /**
     * Retrieves categories for a specific user and type (Income/Expense).
     *
     * @param userId The ID of the user.
     * @param type "Income" or "Expense".
     * @return A list of Category objects.
     */
    public List<Category> getCategoriesByUserIdAndType(int userId, String type) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ? AND category_type = ? ORDER BY category_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Use the full constructor
                    Category category = new Category(
                            rs.getInt("category_id"),
                            rs.getInt("user_id"),
                            rs.getString("category_name"),
                            rs.getString("category_type"),
                            rs.getBoolean("is_default"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting categories by user ID and type: " + e.getMessage(), e);
        }
        return categories;
    }
}
