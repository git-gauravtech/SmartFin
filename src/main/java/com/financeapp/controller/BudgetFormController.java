// src/main/java/com/financeapp/controller/BudgetFormController.java
package com.financeapp.controller;

import com.financeapp.dao.BudgetDAO;
import com.financeapp.dao.CategoryDAO; // New import
import com.financeapp.model.Budget;
import com.financeapp.model.Category; // New import
import com.financeapp.model.User;
import com.financeapp.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Controller for the Budget Form (Set/Edit Budget).
 * Handles input validation and saving/updating budget limits.
 */
public class BudgetFormController {

    private static final Logger LOGGER = Logger.getLogger(BudgetFormController.class.getName());

    @FXML private Label formTitle;
    @FXML private ComboBox<Category> categoryComboBox; // Changed to Category object
    @FXML private TextField amountLimitField;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;

    private DashboardController dashboardController; // Reference to the main dashboard controller
    private User currentUser;
    private Budget budgetToEdit; // Holds the budget if in edit mode

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO(); // New DAO

    /**
     * Initializes the controller. Populates month and year combo boxes.
     */
    @FXML
    public void initialize() {
        // Populate Month ComboBox
        monthComboBox.setItems(FXCollections.observableArrayList(Arrays.asList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        )));

        // Populate Year ComboBox (e.g., current year +/- 5 years)
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = IntStream.rangeClosed(currentYear - 2, currentYear + 5)
                .boxed()
                .collect(Collectors.toList());
        yearComboBox.setItems(FXCollections.observableArrayList(years));

        // Default selections
        monthComboBox.getSelectionModel().select(LocalDate.now().getMonth().name()); // Select current month
        yearComboBox.getSelectionModel().select(Integer.valueOf(currentYear)); // Select current year

        // Category ComboBox will be populated after currentUser is set
    }

    /**
     * Sets the reference to the main DashboardController.
     * @param dashboardController The DashboardController instance.
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Sets the current logged-in user for the form and populates categories.
     * @param currentUser The current User object.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        populateCategoryComboBox(); // Populate categories once user is known
    }

    /**
     * Populates the category combo box with only EXPENSE categories from user's categories.
     */
    private void populateCategoryComboBox() {
        if (currentUser != null) {
            List<Category> expenseCategories = categoryDAO.getCategoriesByUserIdAndType(currentUser.getUserId(), "Expense");
            expenseCategories.sort(Comparator.comparing(Category::getCategoryName)); // Sort alphabetically
            categoryComboBox.setItems(FXCollections.observableArrayList(expenseCategories));
            if (!expenseCategories.isEmpty()) {
                categoryComboBox.getSelectionModel().selectFirst();
            } else {
                categoryComboBox.getSelectionModel().clearSelection();
                AlertUtil.showWarning("No Expense Categories", "No Categories Available", "Please add expense categories in the Categories tab before setting budgets.");
            }
        }
    }


    /**
     * Sets the budget to be edited. If null, the form is in add mode.
     * @param budget The Budget object to edit.
     */
    public void setBudget(Budget budget) {
        this.budgetToEdit = budget;
        if (budgetToEdit != null) {
            formTitle.setText("Edit Monthly Budget");

            // Select the correct category object
            Category currentCategory = categoryDAO.getCategoryById(budgetToEdit.getCategoryId());
            if (currentCategory != null) {
                categoryComboBox.getSelectionModel().select(currentCategory);
            }

            amountLimitField.setText(String.format("₹%.2f", budgetToEdit.getAmountLimit()));
            monthComboBox.getSelectionModel().select(Month.of(budgetToEdit.getMonth()).name());
            yearComboBox.getSelectionModel().select(Integer.valueOf(budgetToEdit.getYear()));

            // Disable category, month, year fields when editing to ensure unique budget update
            categoryComboBox.setDisable(true);
            monthComboBox.setDisable(true);
            yearComboBox.setDisable(true);
        } else {
            formTitle.setText("Set New Monthly Budget");
        }
    }

    /**
     * Handles the save button action. Validates input and saves/updates the budget.
     */
    @FXML
    private void handleSave() {
        if (currentUser == null) {
            AlertUtil.showError("Save Failed", "No User", "Current user session not found. Please log in again.");
            return;
        }

        // Input Validation
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            AlertUtil.showWarning("Invalid Input", "Missing Category", "Please select a category.");
            return;
        }
        // Ensure only Expense categories can be budgeted
        if (!"Expense".equalsIgnoreCase(selectedCategory.getCategoryType())) {
            AlertUtil.showWarning("Invalid Category Type", "Budgeting only for Expenses", "You can only set budgets for 'Expense' categories. Please select an expense category.");
            return;
        }


        double amountLimit;
        try {
            amountLimit = Double.parseDouble(amountLimitField.getText().replace("₹", "")); // Remove rupee symbol for parsing
            if (amountLimit <= 0) {
                AlertUtil.showWarning("Invalid Input", "Invalid Amount Limit", "Amount limit must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Invalid Input", "Invalid Amount Limit", "Please enter a valid numerical amount limit (e.g., ₹500.00).");
            return;
        }

        String selectedMonthName = monthComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonthName == null) {
            AlertUtil.showWarning("Invalid Input", "Missing Month", "Please select a month.");
            return;
        }
        int month = Month.valueOf(selectedMonthName.toUpperCase()).getValue();
        Integer year = yearComboBox.getSelectionModel().getSelectedItem();
        if (year == null) {
            AlertUtil.showWarning("Invalid Input", "Missing Year", "Please select a year.");
            return;
        }

        boolean success;
        Budget budget;
        if (budgetToEdit == null) {
            // Add new budget
            budget = new Budget(currentUser.getUserId(), selectedCategory.getCategoryId(), amountLimit, month, year);
        } else {
            // Update existing budget (only amount limit can be changed here)
            budget = budgetToEdit; // Keep original category, month, year, user ID
            budget.setAmountLimit(amountLimit);
        }

        success = budgetDAO.saveOrUpdateBudget(budget); // Handles both insert and update

        if (success) {
            AlertUtil.showInfo("Success", "Budget Saved", "Budget has been successfully saved/updated.");
            closeForm();
        } else {
            AlertUtil.showError("Error", "Save Failed", "Failed to save budget. A budget for this category, month, and year might already exist.");
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
        Stage stage = (Stage) amountLimitField.getScene().getWindow();
        stage.close();
    }
}
