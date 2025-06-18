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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the TransactionForm.fxml, used for adding and editing transactions.
 */
public class TransactionFormController {

    private static final Logger LOGGER = Logger.getLogger(TransactionFormController.class.getName());

    @FXML private DatePicker datePicker;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> accountComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;

    private DashboardController dashboardController;
    private User currentUser;
    private Transaction transactionToEdit; // Will be null for new transaction, set for editing

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // Store original values for updating transaction to revert old balance changes
    private double originalAmount;
    private int originalAccountId;
    private String originalType;


    /**
     * Initializes the controller. Sets up combo boxes and their listeners.
     */
    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll("Income", "Expense");

        // Listener to dynamically update category combo box based on selected type
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && currentUser != null) {
                populateCategoryComboBox(newVal);
            }
        });

        // Add input validation for amount field
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                amountField.setText(oldValue);
            }
        });
    }

    /**
     * Sets the reference to the main dashboard controller to allow refreshing the table.
     * @param dashboardController The DashboardController instance.
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Sets the current logged-in user and populates initial data.
     * @param currentUser The User object of the current user.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        populateAccountComboBox();
        // Populate category combo box with default "Expense" type initially
        populateCategoryComboBox("Expense");
    }

    /**
     * Sets the transaction to be edited. If null, indicates a new transaction is being added.
     * Pre-fills the form fields if a transaction is provided.
     * @param transaction The Transaction object to edit, or null for a new transaction.
     */
    public void setTransaction(Transaction transaction) {
        this.transactionToEdit = transaction;
        if (transactionToEdit != null) {
            datePicker.setValue(transactionToEdit.getTransactionDate());
            amountField.setText(String.format("%.2f", transactionToEdit.getAmount()));
            typeComboBox.setValue(transactionToEdit.getType());
            // Populate category combo box based on the transaction's type before setting value
            populateCategoryComboBox(transactionToEdit.getType());
            categoryComboBox.setValue(transactionToEdit.getCategoryName());
            accountComboBox.setValue(transactionToEdit.getAccountName());
            descriptionArea.setText(transactionToEdit.getDescription());

            // Store original values for update operation
            originalAmount = transactionToEdit.getAmount();
            originalAccountId = transactionToEdit.getAccountId();
            originalType = transactionToEdit.getType();
        } else {
            // For new transactions, set default date to today
            datePicker.setValue(LocalDate.now());
        }
    }

    /**
     * Populates the account combo box with the current user's accounts.
     */
    private void populateAccountComboBox() {
        if (currentUser == null) return;
        List<Account> accounts = accountDAO.getAccountsByUserId(currentUser.getUserId());
        accountComboBox.getItems().setAll(accounts.stream()
                .map(Account::getAccountName)
                .collect(Collectors.toList()));
        if (!accounts.isEmpty() && transactionToEdit == null) {
            accountComboBox.getSelectionModel().selectFirst(); // Select first account for new transactions
        }
    }

    /**
     * Populates the category combo box based on the selected transaction type (Income/Expense).
     * @param type The type of category to load ("Income" or "Expense").
     */
    private void populateCategoryComboBox(String type) {
        if (currentUser == null || type == null) return;
        List<Category> categories = categoryDAO.getCategoriesByUserIdAndType(currentUser.getUserId(), type);
        categoryComboBox.getItems().setAll(categories.stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList()));
        if (!categories.isEmpty() && categoryComboBox.getValue() == null && transactionToEdit == null) {
            // Select first for new transactions if no category is pre-selected
            categoryComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Handles the save button action. Adds a new transaction or updates an existing one.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleSaveTransaction(ActionEvent event) {
        LocalDate transactionDate = datePicker.getValue();
        String amountText = amountField.getText().trim();
        String type = typeComboBox.getValue();
        String categoryName = categoryComboBox.getValue();
        String accountName = accountComboBox.getValue();
        String description = descriptionArea.getText().trim();

        if (transactionDate == null || amountText.isEmpty() || type == null || categoryName == null || accountName == null) {
            AlertUtil.showWarning("Input Error", "Missing Fields", "Please fill in all required fields (Date, Amount, Type, Category, Account).");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                AlertUtil.showWarning("Input Error", "Invalid Amount", "Amount must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Input Error", "Invalid Amount Format", "Please enter a valid numeric amount (e.g., 123.45).");
            return;
        }

        // Get actual Category and Account objects to retrieve their IDs
        Category selectedCategory = categoryDAO.getCategoriesByUserIdAndType(currentUser.getUserId(), type).stream()
                .filter(c -> c.getCategoryName().equals(categoryName))
                .findFirst().orElse(null);

        Account selectedAccount = accountDAO.getAccountsByUserId(currentUser.getUserId()).stream()
                .filter(a -> a.getAccountName().equals(accountName))
                .findFirst().orElse(null);

        if (selectedCategory == null) {
            AlertUtil.showError("Data Error", "Category Not Found", "Selected category could not be found. Please refresh and try again.");
            return;
        }
        if (selectedAccount == null) {
            AlertUtil.showError("Data Error", "Account Not Found", "Selected account could not be found. Please refresh and try again.");
            return;
        }


        boolean success;
        if (transactionToEdit == null) {
            // Add new transaction
            Transaction newTransaction = new Transaction(
                    currentUser.getUserId(),
                    selectedAccount.getAccountId(),
                    selectedCategory.getCategoryId(),
                    amount,
                    type,
                    description,
                    transactionDate
            );
            success = transactionDAO.addTransaction(newTransaction);
            if (success) {
                AlertUtil.showInfo("Success", "Transaction Added", "New transaction added successfully.");
            } else {
                AlertUtil.showError("Error", "Addition Failed", "Could not add transaction. Please try again.");
            }
        } else {
            // Update existing transaction
            Transaction updatedTransaction = new Transaction(
                    transactionToEdit.getTransactionId(),
                    currentUser.getUserId(),
                    selectedAccount.getAccountId(),
                    selectedCategory.getCategoryId(),
                    amount,
                    type,
                    description,
                    transactionDate,
                    transactionToEdit.getCreatedAt() // Preserve original creation timestamp
            );

            // Pass original values for correct balance adjustment
            success = transactionDAO.updateTransaction(updatedTransaction, originalAmount, originalAccountId, originalType);

            if (success) {
                AlertUtil.showInfo("Success", "Transaction Updated", "Transaction updated successfully.");
            } else {
                AlertUtil.showError("Error", "Update Failed", "Could not update transaction. Please try again.");
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
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
        if (dashboardController != null) {
            dashboardController.refreshDashboard(); // Refresh the main dashboard after close
        }
    }
}
