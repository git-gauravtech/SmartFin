// src/main/java/com/financeapp/controller/CategoryFormController.java
package com.financeapp.controller;

import com.financeapp.dao.CategoryDAO;
import com.financeapp.model.Category;
import com.financeapp.model.User;
import com.financeapp.utils.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the CategoryForm.fxml, used for adding and editing categories.
 */
public class CategoryFormController {

    private static final Logger LOGGER = Logger.getLogger(CategoryFormController.class.getName());

    @FXML private TextField categoryNameField;
    @FXML private ComboBox<String> categoryTypeComboBox;
    @FXML private Button saveButton;

    private DashboardController dashboardController;
    private User currentUser;
    private Category categoryToEdit; // Will be null for new category, set for editing

    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * Initializes the controller. Sets up the category type combo box.
     */
    @FXML
    public void initialize() {
        categoryTypeComboBox.getItems().addAll("Income", "Expense");
    }

    /**
     * Sets the reference to the main dashboard controller to allow refreshing the table.
     * @param dashboardController The DashboardController instance.
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Sets the current logged-in user.
     * @param currentUser The User object of the current user.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Sets the category to be edited. If null, indicates a new category is being added.
     * Pre-fills the form fields if a category is provided.
     * @param category The Category object to edit, or null for a new category.
     */
    public void setCategory(Category category) {
        this.categoryToEdit = category;
        if (categoryToEdit != null) {
            categoryNameField.setText(categoryToEdit.getCategoryName());
            categoryTypeComboBox.setValue(categoryToEdit.getCategoryType());
            // Disable editing for default categories
            if (categoryToEdit.isDefault()) {
                categoryNameField.setEditable(false);
                categoryTypeComboBox.setDisable(true);
                saveButton.setDisable(true);
                AlertUtil.showInfo("Default Category", "Cannot Edit Default Category", "System default categories cannot be edited.");
            }
        }
    }

    /**
     * Handles the save button action. Adds a new category or updates an existing one.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleSaveCategory(ActionEvent event) {
        String categoryName = categoryNameField.getText().trim();
        String categoryType = categoryTypeComboBox.getValue();

        if (categoryName.isEmpty() || categoryType == null) {
            AlertUtil.showWarning("Input Error", "Missing Fields", "Please enter category name and select type.");
            return;
        }

        boolean success;
        if (categoryToEdit == null) {
            // Add new category
            Category newCategory = new Category(currentUser.getUserId(), categoryName, categoryType);
            success = categoryDAO.addCategory(newCategory);
            if (success) {
                AlertUtil.showInfo("Success", "Category Added", "New category '" + categoryName + "' added successfully.");
            } else {
                AlertUtil.showError("Error", "Addition Failed", "Could not add category. It might already exist or a database error occurred.");
            }
        } else {
            // Update existing category
            // Use the constructor that matches the fields you intend to update and have available
            Category updatedCategory = new Category(categoryToEdit.getCategoryId(), currentUser.getUserId(), categoryName, categoryType, categoryToEdit.isDefault(), categoryToEdit.getCreatedAt());
            success = categoryDAO.updateCategory(updatedCategory);
            if (success) {
                AlertUtil.showInfo("Success", "Category Updated", "Category '" + categoryName + "' updated successfully.");
            } else {
                AlertUtil.showError("Error", "Update Failed", "Could not update category. It might already exist or a database error occurred.");
            }
        }

        if (success) {
            closeForm();
        }
    }

    /**
     * Handles the cancel button action, closing the form without saving.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeForm();
    }

    /**
     * Closes the current form window.
     */
    private void closeForm() {
        Stage stage = (Stage) categoryNameField.getScene().getWindow();
        stage.close();
        if (dashboardController != null) {
            dashboardController.refreshDashboard(); // Refresh the main dashboard after close
        }
    }
}
