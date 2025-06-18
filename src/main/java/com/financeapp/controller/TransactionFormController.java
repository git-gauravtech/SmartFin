// src/main/java/com/financeapp/controller/TransactionFormController.java
package com.financeapp.controller;

import com.financeapp.dao.AccountDAO;
import com.financeapp.dao.CategoryDAO;
import com.financeapp.dao.TransactionDAO;
import com.financeapp.model.Account;
import com.financeapp.model.Category;
import com.financeapp.model.Transaction;
import com.financeapp.model.User;
import com.financeapp.utils.AlertUtil;
import com.financeapp.utils.WekaPredictor;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the Transaction Form (Add/Edit Transaction).
 * Handles input validation, AI categorization, and saving/updating transactions.
 */
public class TransactionFormController {

    private static final Logger LOGGER = Logger.getLogger(TransactionFormController.class.getName());

    @FXML private Label formTitle;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private RadioButton incomeRadio;
    @FXML private RadioButton expenseRadio;
    @FXML private ToggleGroup transactionTypeGroup;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Category> categoryComboBox; // Changed to Category object
    @FXML private ComboBox<Account> accountComboBox;   // New ComboBox for Account

    private DashboardController dashboardController; // Reference to the main dashboard controller
    private User currentUser;
    private Transaction transactionToEdit; // Holds the transaction if in edit mode

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO(); // New DAO for categories
    private final AccountDAO accountDAO = new AccountDAO();   // New DAO for accounts


    /**
     * Initializes the controller. Sets up listeners for description changes for AI categorization.
     */
    @FXML
    public void initialize() {
        // Set initial state for radios
        expenseRadio.setSelected(true); // Default to expense

        // Add listener to transaction type radio buttons
        transactionTypeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle != null) {
                // When type changes, re-populate categories based on type
                populateCategoryComboBox(((RadioButton) newToggle).getText());
            }
        });

        // Listener for description changes to trigger AI categorization
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                String predictedCategoryName = WekaPredictor.predictCategory(newValue.trim());
                // Try to find the Category object by name from the current list
                categoryComboBox.getItems().stream()
                        .filter(c -> c.getCategoryName().equalsIgnoreCase(predictedCategoryName))
                        .findFirst()
                        .ifPresentOrElse(
                                categoryComboBox.getSelectionModel()::select,
                                () -> categoryComboBox.getSelectionModel().select(
                                        categoryComboBox.getItems().stream()
                                                .filter(c -> c.getCategoryName().equalsIgnoreCase("Miscellaneous") && c.getCategoryType().equals("Expense"))
                                                .findFirst().orElse(null)
                                ) // Fallback to "Miscellaneous" expense category
                        );
            }
        });
    }

    /**
     * Sets the reference to the main DashboardController.
     * @param dashboardController The DashboardController instance.
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Sets the current logged-in user for the form and populates dynamic data.
     * @param currentUser The current User object.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        populateCategoryComboBox(expenseRadio.isSelected() ? "Expense" : "Income"); // Initial population based on default selected type
        populateAccountComboBox();
    }

    /**
     * Populates the category ComboBox based on the selected transaction type.
     * @param type "Income" or "Expense".
     */
    private void populateCategoryComboBox(String type) {
        if (currentUser != null) {
            List<Category> categories = categoryDAO.getCategoriesByUserIdAndType(currentUser.getUserId(), type);
            // Sort categories by name for better UX
            categories.sort(Comparator.comparing(Category::getCategoryName));
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            if (!categories.isEmpty()) {
                categoryComboBox.getSelectionModel().selectFirst();
            } else {
                categoryComboBox.getSelectionModel().clearSelection();
                AlertUtil.showWarning("No Categories", "No Categories Available",
                        "Please add " + type.toLowerCase() + " categories in the Categories tab before adding this transaction type.");
            }
        }
    }

    /**
     * Populates the account ComboBox with the current user's accounts.
     */
    private void populateAccountComboBox() {
        if (currentUser != null) {
            List<Account> accounts = accountDAO.getAccountsByUserId(currentUser.getUserId());
            accountComboBox.setItems(FXCollections.observableArrayList(accounts));
            if (!accounts.isEmpty()) {
                accountComboBox.getSelectionModel().selectFirst();
            } else {
                accountComboBox.getSelectionModel().clearSelection();
                AlertUtil.showWarning("No Accounts", "No Accounts Available", "Please add accounts in the Accounts tab before adding transactions.");
            }
        }
    }


    /**
     * Sets the transaction to be edited. If null, the form is in add mode.
     * @param transaction The Transaction object to edit.
     */
    public void setTransaction(Transaction transaction) {
        this.transactionToEdit = transaction;
        if (transactionToEdit != null) {
            formTitle.setText("Edit Transaction");
            amountField.setText(String.format("₹%.2f", transactionToEdit.getAmount()));
            datePicker.setValue(transactionToEdit.getTransactionDate());

            if ("Income".equals(transactionToEdit.getType())) {
                incomeRadio.setSelected(true);
                populateCategoryComboBox("Income");
            } else {
                expenseRadio.setSelected(true);
                populateCategoryComboBox("Expense");
            }
            descriptionArea.setText(transactionToEdit.getDescription());

            // Select category
            Category currentCategory = categoryDAO.getCategoryById(transactionToEdit.getCategoryId());
            if (currentCategory != null) {
                categoryComboBox.getSelectionModel().select(currentCategory);
            }

            // Select account
            Account currentAccount = accountDAO.getAccountById(transactionToEdit.getAccountId());
            if (currentAccount != null) {
                accountComboBox.getSelectionModel().select(currentAccount);
            }

        } else {
            formTitle.setText("Add New Transaction");
            datePicker.setValue(LocalDate.now()); // Default to today's date for new transactions
        }
    }

    /**
     * Handles the save button action. Validates input and saves/updates the transaction.
     */
    @FXML
    private void handleSave() {
        if (currentUser == null) {
            AlertUtil.showError("Save Failed", "No User", "Current user session not found. Please log in again.");
            return;
        }

        // Input Validation
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().replace("₹", "")); // Remove rupee symbol for parsing
            if (amount <= 0) {
                AlertUtil.showWarning("Invalid Input", "Invalid Amount", "Amount must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Invalid Input", "Invalid Amount", "Please enter a valid numerical amount (e.g., ₹100.50).");
            return;
        }

        LocalDate date = datePicker.getValue();
        if (date == null) {
            AlertUtil.showWarning("Invalid Input", "Invalid Date", "Please select a transaction date.");
            return;
        }

        String type = ((RadioButton) transactionTypeGroup.getSelectedToggle()).getText();
        String description = descriptionArea.getText().trim();

        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            AlertUtil.showWarning("Invalid Input", "Missing Category", "Please select a category.");
            return;
        }

        Account selectedAccount = accountComboBox.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            AlertUtil.showWarning("Invalid Input", "Missing Account", "Please select an account.");
            return;
        }

        if (description.isEmpty()) {
            AlertUtil.showWarning("Invalid Input", "Missing Description", "Please enter a description for the transaction.");
            return;
        }

        // Ensure the selected category type matches the transaction type
        if (!selectedCategory.getCategoryType().equalsIgnoreCase(type)) {
            AlertUtil.showWarning("Category Type Mismatch", "Invalid Category Selection",
                    "The selected category '" + selectedCategory.getCategoryName() + "' is an " +
                            selectedCategory.getCategoryType() + " category. Please select a " + type + " category.");
            return;
        }


        boolean success;
        if (transactionToEdit == null) {
            // Add new transaction
            Transaction newTransaction = new Transaction(
                    currentUser.getUserId(),
                    selectedAccount.getAccountId(), // Use selected Account ID
                    amount,
                    type,
                    selectedCategory.getCategoryId(), // Use selected Category ID
                    description,
                    date
            );
            success = transactionDAO.addTransaction(newTransaction);
            if (success) {
                AlertUtil.showInfo("Success", "Transaction Added", "New transaction has been added.");
            } else {
                AlertUtil.showError("Error", "Save Failed", "Failed to add new transaction. Please try again.");
            }
        } else {
            // Update existing transaction
            transactionToEdit.setAmount(amount);
            transactionToEdit.setTransactionDate(date);
            transactionToEdit.setType(type);
            transactionToEdit.setDescription(description);
            transactionToEdit.setCategoryId(selectedCategory.getCategoryId()); // Update Category ID
            transactionToEdit.setAccountId(selectedAccount.getAccountId());   // Update Account ID
            success = transactionDAO.updateTransaction(transactionToEdit);
            if (success) {
                AlertUtil.showInfo("Success", "Transaction Updated", "Transaction has been updated.");
            } else {
                AlertUtil.showError("Error", "Update Failed", "Failed to update transaction. Please try again.");
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
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
    }
}
