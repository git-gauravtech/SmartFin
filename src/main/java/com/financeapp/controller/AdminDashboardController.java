// src/main/java/com/financeapp/controller/AdminDashboardController.java
package com.financeapp.controller;

import com.financeapp.dao.*;
import com.financeapp.model.Account;
import com.financeapp.model.Budget;
import com.financeapp.model.Category;
import com.financeapp.model.Transaction;
import com.financeapp.model.User;
import com.financeapp.utils.AlertUtil;
import com.financeapp.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the Admin Dashboard.
 * Provides functionalities for user management, viewing all transactions/budgets,
 * and displaying global application statistics.
 */
public class AdminDashboardController {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());
    private final UserDAO userDAO = new UserDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private User currentAdminUser;

    @FXML private Label adminWelcomeLabel;
    @FXML private TabPane adminTabPane;

    // Users Management Tab
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, Boolean> colIsAdmin;
    @FXML private TableColumn<User, LocalDateTime> colUserCreatedAt;

    // All Transactions Tab
    @FXML private TableView<Transaction> allTransactionTable;
    @FXML private TableColumn<Transaction, Integer> colAllTransId;
    @FXML private TableColumn<Transaction, Integer> colAllTransUserId;
    @FXML private TableColumn<Transaction, String> colAllTransUsername;
    @FXML private TableColumn<Transaction, Double> colAllTransAmount;
    @FXML private TableColumn<Transaction, String> colAllTransType;
    @FXML private TableColumn<Transaction, String> colAllTransCategory;
    @FXML private TableColumn<Transaction, String> colAllTransAccount;
    @FXML private TableColumn<Transaction, String> colAllTransDescription;
    @FXML private TableColumn<Transaction, LocalDate> colAllTransDate;
    @FXML private TableColumn<Transaction, LocalDateTime> colAllTransCreatedAt;

    // All Budgets Tab
    @FXML private TableView<Budget> allBudgetTable;
    @FXML private TableColumn<Budget, Integer> colAllBudgetId;
    @FXML private TableColumn<Budget, Integer> colAllBudgetUserId;
    @FXML private TableColumn<Budget, String> colAllBudgetUsername;
    @FXML private TableColumn<Budget, String> colAllBudgetCategory;
    @FXML private TableColumn<Budget, Double> colAllBudgetLimit;
    @FXML private TableColumn<Budget, Integer> colAllBudgetMonth;
    @FXML private TableColumn<Budget, Integer> colAllBudgetYear;
    @FXML private TableColumn<Budget, LocalDateTime> colAllBudgetCreatedAt;

    // Global Statistics Tab
    @FXML private Label totalUsersLabel;
    @FXML private Label totalTransactionsLabel;
    @FXML private Label overallIncomeLabel;
    @FXML private Label overallExpensesLabel;
    @FXML private Label avgTransactionsPerUserLabel;
    @FXML private VBox topCategoriesVBox;

    /**
     * Initializes the controller. This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        currentAdminUser = SessionManager.getInstance().getCurrentUser();
        if (currentAdminUser == null || !currentAdminUser.isAdmin()) {
            AlertUtil.showError("Access Denied", "Unauthorized Access", "You must be an admin to access this dashboard.");
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/financeapp/view/Login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) adminWelcomeLabel.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Login");
                    stage.show();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to load login FXML after admin access denied.", e);
                }
            });
            return;
        }

        adminWelcomeLabel.setText("Welcome, Admin " + currentAdminUser.getUsername() + "!");

        // Initialize User Table columns
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colIsAdmin.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));
        colUserCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Initialize All Transactions Table columns
        colAllTransId.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colAllTransUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colAllTransUsername.setCellValueFactory(cellData -> {
            User user = userDAO.getUserByUserId(cellData.getValue().getUserId());
            return new javafx.beans.property.SimpleStringProperty(user != null ? user.getUsername() : "N/A");
        });
        // Format amount with ₹ symbol
        colAllTransAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
        colAllTransAmount.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%.2f", item));
                }
            }
        });
        colAllTransType.setCellValueFactory(new PropertyValueFactory<>("type"));
        // Display category name using category_id
        colAllTransCategory.setCellValueFactory(cellData -> {
            Category category = categoryDAO.getCategoryById(cellData.getValue().getCategoryId());
            return new javafx.beans.property.SimpleStringProperty(category != null ? category.getCategoryName() : "N/A");
        });
        // Display account name using account_id
        colAllTransAccount.setCellValueFactory(cellData -> {
            Account account = new AccountDAO().getAccountById(cellData.getValue().getAccountId());
            return new javafx.beans.property.SimpleStringProperty(account != null ? account.getAccountName() : "N/A");
        });
        colAllTransDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAllTransDate.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        colAllTransCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Initialize All Budgets Table columns
        colAllBudgetId.setCellValueFactory(new PropertyValueFactory<>("budgetId"));
        colAllBudgetUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colAllBudgetUsername.setCellValueFactory(cellData -> {
            User user = userDAO.getUserByUserId(cellData.getValue().getUserId());
            return new javafx.beans.property.SimpleStringProperty(user != null ? user.getUsername() : "N/A");
        });
        // Display category name using category_id
        colAllBudgetCategory.setCellValueFactory(cellData -> {
            Category category = categoryDAO.getCategoryById(cellData.getValue().getCategoryId());
            return new javafx.beans.property.SimpleStringProperty(category != null ? category.getCategoryName() : "N/A");
        });
        // Format amount with ₹ symbol
        colAllBudgetLimit.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmountLimit()));
        colAllBudgetLimit.setCellFactory(column -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%.2f", item));
                }
            }
        });
        colAllBudgetMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        colAllBudgetYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colAllBudgetCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        refreshAdminDashboard();

        adminTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                refreshAdminDashboard();
            }
        });
    }

    /**
     * Refreshes all data displayed on the admin dashboard.
     */
    private void refreshAdminDashboard() {
        refreshUserTable();
        refreshAllTransactions();
        refreshAllBudgets();
        updateGlobalStatistics();
    }

    // --- User Management ---

    /**
     * Populates the user table with all users from the database.
     */
    private void refreshUserTable() {
        List<User> users = userDAO.getAllUsers();
        ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
        userTable.setItems(observableUsers);
    }

    /**
     * Handles deleting a selected user.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            AlertUtil.showWarning("No Selection", "No User Selected", "Please select a user to delete.");
            return;
        }
        if (selectedUser.getUserId() == currentAdminUser.getUserId()) {
            AlertUtil.showWarning("Deletion Forbidden", "Cannot Delete Self", "You cannot delete your own admin account.");
            return;
        }

        Optional<ButtonType> result = AlertUtil.showConfirmation("Confirm Deletion", "Delete User: " + selectedUser.getUsername(),
                "Are you sure you want to delete this user? This action is irreversible and will delete all their transactions, budgets, accounts, and categories.");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userDAO.deleteUser(selectedUser.getUserId())) {
                AlertUtil.showInfo("Success", "User Deleted", "User " + selectedUser.getUsername() + " has been successfully deleted.");
                refreshAdminDashboard();
            } else {
                AlertUtil.showError("Error", "Deletion Failed", "Could not delete user. Please try again.");
            }
        }
    }

    /**
     * Handles toggling the admin status of a selected user.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleToggleAdminStatus(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            AlertUtil.showWarning("No Selection", "No User Selected", "Please select a user to toggle admin status.");
            return;
        }
        if (selectedUser.getUserId() == currentAdminUser.getUserId()) {
            AlertUtil.showWarning("Action Forbidden", "Cannot Change Own Status", "You cannot change your own admin status.");
            return;
        }

        User updatedUser = new User(
                selectedUser.getUserId(),
                selectedUser.getUsername(),
                selectedUser.getPasswordHash(),
                selectedUser.getPasswordSalt(),
                !selectedUser.isAdmin(), // Toggle admin status
                selectedUser.getCreatedAt()
        );

        if (userDAO.updateUser(updatedUser)) {
            AlertUtil.showInfo("Success", "Admin Status Toggled",
                    "Admin status for " + selectedUser.getUsername() + " changed to " + updatedUser.isAdmin() + ".");
            refreshUserTable();
        } else {
            AlertUtil.showError("Error", "Update Failed", "Could not toggle admin status. Please try again.");
        }
    }

    /**
     * Handles registering a new user with admin privileges.
     * Opens a simple dialog for username and password.
     */
    @FXML
    private void handleRegisterNewAdmin(ActionEvent event) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Register New Admin");
        dialog.setHeaderText("Enter details for the new admin account.");

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(usernameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                return new User(usernameField.getText(), passwordField.getText(), "", true);
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(newUser -> {
            if (newUser.getUsername().isEmpty() || newUser.getPasswordHash().isEmpty()) {
                AlertUtil.showWarning("Input Error", "Missing Credentials", "Username and password cannot be empty.");
                return;
            }
            if (userDAO.registerUser(newUser.getUsername(), newUser.getPasswordHash(), true)) {
                AlertUtil.showInfo("Success", "Admin Registered", "New admin '" + newUser.getUsername() + "' registered successfully.");
                refreshUserTable();
            } else {
                AlertUtil.showError("Registration Failed", "Error", "Could not register new admin. Username might already exist.");
            }
        });
    }

    // --- All Transactions ---

    /**
     * Refreshes the table showing all transactions from all users.
     * Includes a lookup for username, category, and account for display.
     */
    @FXML
    private void refreshAllTransactions() {
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(transactions);
        allTransactionTable.setItems(observableTransactions);
    }

    // --- All Budgets ---

    /**
     * Refreshes the table showing all budgets from all users.
     * Includes a lookup for username and category for display.
     */
    @FXML
    private void refreshAllBudgets() {
        List<Budget> budgets = budgetDAO.getAllBudgets();
        ObservableList<Budget> observableBudgets = FXCollections.observableArrayList(budgets);
        allBudgetTable.setItems(observableBudgets);
    }

    // --- Global Statistics ---

    /**
     * Updates the global statistics displayed on the admin dashboard.
     */
    private void updateGlobalStatistics() {
        List<User> allUsers = userDAO.getAllUsers();
        List<Transaction> allTransactions = transactionDAO.getAllTransactions();

        totalUsersLabel.setText(String.valueOf(allUsers.size()));
        totalTransactionsLabel.setText(String.valueOf(allTransactions.size()));

        double overallIncome = allTransactions.stream()
                .filter(t -> t.getType().equals("Income"))
                .mapToDouble(Transaction::getAmount)
                .sum();
        overallIncomeLabel.setText(String.format("₹%.2f", overallIncome));

        double overallExpenses = allTransactions.stream()
                .filter(t -> t.getType().equals("Expense"))
                .mapToDouble(Transaction::getAmount)
                .sum();
        overallExpensesLabel.setText(String.format("₹%.2f", overallExpenses));

        double avgTransPerUser = (allUsers.size() > 0) ? (double) allTransactions.size() / allUsers.size() : 0.0;
        avgTransactionsPerUserLabel.setText(String.format("%.2f", avgTransPerUser)); // No currency symbol for average count

        // Top Expense Categories (All Users)
        Map<String, Double> allExpenseCategoriesBreakdown = allTransactions.stream()
                .filter(t -> t.getType().equals("Expense"))
                .collect(Collectors.groupingBy(t -> {
                    Category category = categoryDAO.getCategoryById(t.getCategoryId());
                    return category != null ? category.getCategoryName() : "Unknown";
                }, Collectors.summingDouble(Transaction::getAmount)));

        List<Map.Entry<String, Double>> sortedCategories = allExpenseCategoriesBreakdown.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        topCategoriesVBox.getChildren().clear();
        topCategoriesVBox.getChildren().add(new Label("Top Expense Categories:"));
        topCategoriesVBox.getChildren().add(new Label("Category: Total Amount")); // Header
        if (sortedCategories.isEmpty()) {
            topCategoriesVBox.getChildren().add(new Label("No expense data available."));
        } else {
            for (Map.Entry<String, Double> entry : sortedCategories) {
                topCategoriesVBox.getChildren().add(new Label(String.format("- %s: ₹%.2f", entry.getKey(), entry.getValue())));
            }
        }
    }

    // --- Logout ---

    /**
     * Handles the logout action, clearing the session and returning to the login screen.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/financeapp/view/Login.fxml")));
            Stage stage = (Stage) adminWelcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Personal Finance Login");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load login FXML after logout from admin dashboard.", e);
            AlertUtil.showError("Navigation Error", "Could not load login screen.", "Please restart the application.");
        }
    }
}
