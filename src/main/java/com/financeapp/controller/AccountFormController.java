// src/main/java/com/financeapp/controller/AccountFormController.java
package com.financeapp.controller;

import com.financeapp.dao.AccountDAO;
import com.financeapp.model.Account;
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
 * Controller for the Account Form (Add/Edit Account).
 * Handles input validation and saving/updating accounts.
 */
public class AccountFormController {

    private static final Logger LOGGER = Logger.getLogger(AccountFormController.class.getName());

    @FXML private Label formTitle;
    @FXML private TextField accountNameField;
    @FXML private ComboBox<String> accountTypeComboBox;
    @FXML private TextField initialBalanceField;

    private DashboardController dashboardController; // Reference to the main dashboard controller
    private User currentUser;
    private Account accountToEdit; // Holds the account if in edit mode

    private final AccountDAO accountDAO = new AccountDAO();

    private static final String[] ACCOUNT_TYPES = {"Checking", "Savings", "Credit Card", "Cash", "Investment", "Loan"};

    /**
     * Initializes the controller. Populates the account type combo box.
     */
    @FXML
    public void initialize() {
        accountTypeComboBox.setItems(FXCollections.observableArrayList(Arrays.asList(ACCOUNT_TYPES)));
        accountTypeComboBox.getSelectionModel().selectFirst(); // Default to "Checking" or first in list
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
     * Sets the account to be edited. If null, the form is in add mode.
     * @param account The Account object to edit.
     */
    public void setAccount(Account account) {
        this.accountToEdit = account;
        if (accountToEdit != null) {
            formTitle.setText("Edit Account");
            accountNameField.setText(accountToEdit.getAccountName());
            accountTypeComboBox.getSelectionModel().select(accountToEdit.getAccountType());
            initialBalanceField.setText(String.format("₹%.2f", accountToEdit.getInitialBalance()));

            // Disable name and type for editing to maintain unique constraint, only initial balance can be adjusted
            // Account balance changes via transactions, initial balance is a historical value.
            // If we allow editing initial balance, we need to carefully re-calculate current balance.
            // For simplicity, we will allow initial balance to be edited, and it will update the current balance directly.
            accountNameField.setDisable(true); // Account name cannot be changed easily (unique constraint)
            accountTypeComboBox.setDisable(true); // Account type often fixed once created.
        } else {
            formTitle.setText("Add New Account");
            initialBalanceField.setText("₹0.00"); // Default initial balance
        }
    }

    /**
     * Handles the save button action. Validates input and saves/updates the account.
     */
    @FXML
    private void handleSave() {
        if (currentUser == null) {
            AlertUtil.showError("Save Failed", "No User", "Current user session not found. Please log in again.");
            return;
        }

        // Input Validation
        String accountName = accountNameField.getText().trim();
        String accountType = accountTypeComboBox.getSelectionModel().getSelectedItem();
        double initialBalance;

        if (accountName.isEmpty()) {
            AlertUtil.showWarning("Invalid Input", "Missing Account Name", "Please enter an account name.");
            return;
        }
        if (accountType == null || accountType.isEmpty()) {
            AlertUtil.showWarning("Invalid Input", "Missing Account Type", "Please select an account type.");
            return;
        }

        try {
            initialBalance = Double.parseDouble(initialBalanceField.getText().replace("₹", "")); // Remove rupee symbol
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Invalid Input", "Invalid Balance", "Please enter a valid numerical balance (e.g., ₹1000.00).");
            return;
        }

        boolean success;
        if (accountToEdit == null) {
            // Add new account
            Account newAccount = new Account(currentUser.getUserId(), accountName, initialBalance, accountType);
            success = accountDAO.addAccount(newAccount);
            if (success) {
                AlertUtil.showInfo("Success", "Account Added", "New account has been added.");
            } else {
                AlertUtil.showError("Error", "Save Failed", "Failed to add new account. Account name might already exist.");
            }
        } else {
            // Update existing account
            // Only initial balance and thus current balance can be directly adjusted here
            // To change name/type, a more complex re-creation or data migration logic would be needed.
            accountToEdit.setInitialBalance(initialBalance);
            // When initial balance is edited, current balance should be reset to it,
            // assuming all transactions will be re-calculated or current_balance is purely initial.
            // A more robust approach might be to not allow initialBalance edits or recalculate based on transactions.
            // For now, we'll update current_balance to initial_balance.
            accountToEdit.setCurrentBalance(initialBalance); // Reset current to new initial balance
            success = accountDAO.updateAccount(accountToEdit); // Update with new initial/current balance
            if (success) {
                AlertUtil.showInfo("Success", "Account Updated", "Account has been updated.");
            } else {
                AlertUtil.showError("Error", "Update Failed", "Failed to update account. Please try again.");
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
        Stage stage = (Stage) accountNameField.getScene().getWindow();
        stage.close();
    }
}
