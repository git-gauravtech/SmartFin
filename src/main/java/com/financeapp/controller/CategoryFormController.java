// src/main/java/com/financeapp/controller/CategoryFormController.java
package com.financeapp.controller;

import com.financeapp.dao.CategoryDAO;
import com.financeapp.model.Category;
import com.financeapp.model.User;
import com.financeapp.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Category Form (Add/Edit Category).
 * Handles input validation and saving/updating categories.
 */
public class CategoryFormController {

    private static final Logger LOGGER = Logger.getLogger(CategoryFormController.class.getName());

    @FXML private Label formTitle;
    @FXML private TextField categoryNameField;
    @FXML private ComboBox<String> categoryTypeComboBox;

    private DashboardController dashboardController; // Reference to the main dashboard controller
    private User currentUser;
    private Category categoryToEdit; // Holds the category if in edit mode

    private final CategoryDAO categoryDAO = new CategoryDAO();

    private static final String[] CATEGORY_TYPES = {"Income", "Expense"};

    /**
     * Initializes the controller. Populates the category type combo box.
     */
    @FXML
    public void initialize() {
        categoryTypeComboBox.setItems(FXCollections.observableArrayList(Arrays.asList(CATEGORY_TYPES)));
        categoryTypeComboBox.getSelectionModel().selectFirst(); // Default to "Income" or first in list
    }

    /**
     * Sets the reference to the main DashboardController.
     * @param dashboardController The DashboardController instance.
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Sets the current logged-in user for the form.
     * @param currentUser The current User object.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Sets the category to be edited. If null, the form is in add mode.
     * @param category The Category object to edit.
     */
    public void setCategory(Category category) {
        this.categoryToEdit = category;
        if (categoryToEdit != null) {
            formTitle.setText("Edit Category");
            categoryNameField.setText(categoryToEdit.getCategoryName());
            categoryTypeComboBox.getSelectionModel().select(categoryToEdit.getCategoryType());

            // Disable name and type if it's a default category or if category name is being edited (unique constraint)
            if (categoryToEdit.isDefault()) {
                categoryNameField.setDisable(true);
                categoryTypeComboBox.setDisable(true);
                AlertUtil.showInfo("Default Category", "Cannot Edit Default Category", "System default categories cannot be edited, only viewed.");
            } else {
                // Allow name and type to be edited for custom categories
                categoryNameField.setDisable(false);
                categoryTypeComboBox.setDisable(false);
            }
        } else {
            formTitle.setText("Add New Category");
        }
    }

    /**
     * Handles the save button action. Validates input and saves/updates the category.
     */
    @FXML
    private void handleSave() {
        if (currentUser == null) {
            AlertUtil.showError("Save Failed", "No User", "Current user session not found. Please log in again.");
            return;
        }

        // If editing a default category, just close (as per setCategory logic)
        if (categoryToEdit != null && categoryToEdit.isDefault()) {
            closeForm();
            return;
        }

        // Input Validation
        String categoryName = categoryNameField.getText().trim();
        String categoryType = categoryTypeComboBox.getSelectionModel().getSelectedItem();

        if (categoryName.isEmpty()) {
            AlertUtil.showWarning("Invalid Input", "Missing Category Name", "Please enter a category name.");
            return;
        }
        if (categoryType == null || categoryType.isEmpty()) {
            AlertUtil.showWarning("Invalid Input", "Missing Category Type", "Please select a category type.");
            return;
        }

        boolean success;
        if (categoryToEdit == null) {
            // Add new category
            Category newCategory = new Category(currentUser.getUserId(), categoryName, categoryType, false); // New custom category is not default
            success = categoryDAO.addCategory(newCategory);
            if (success) {
                AlertUtil.showInfo("Success", "Category Added", "New category has been added.");
            } else {
                // Error message handled by DAO (duplicate category name)
            }
        } else {
            // Update existing category
            categoryToEdit.setCategoryName(categoryName);
            categoryToEdit.setCategoryType(categoryType);
            success = categoryDAO.updateCategory(categoryToEdit);
            if (success) {
                AlertUtil.showInfo("Success", "Category Updated", "Category has been updated.");
            } else {
                // Error message handled by DAO (duplicate category name)
            }
        }

        if (success) {
            closeForm();
        }
    }

    /**
     * Handles the cancel button action, closing the form without saving.
     */
    @FXML
    private void handleCancel() {
        closeForm();
    }

    /**
     * Closes the current stage (form window).
     */
    private void closeForm() {
        Stage stage = (Stage) categoryNameField.getScene().getWindow();
        stage.close();
    }
}
