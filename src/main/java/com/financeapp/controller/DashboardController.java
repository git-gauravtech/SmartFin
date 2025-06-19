// src/main/java/com/financeapp/controller/DashboardController.java
package com.financeapp.controller;

import com.financeapp.dao.AccountDAO;
import com.financeapp.dao.BudgetDAO;
import com.financeapp.dao.CategoryDAO;
import com.financeapp.dao.TransactionDAO;
import com.financeapp.model.Account;
import com.financeapp.model.Budget;
import com.financeapp.model.Category;
import com.financeapp.model.Transaction;
import com.financeapp.model.User;
import com.financeapp.utils.AlertUtil;
import com.financeapp.utils.SessionManager;
import com.financeapp.utils.WekaPredictor; // Import your WekaPredictor
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the main user Dashboard.
 * Manages financial overview, transaction and budget display, CRUD operations, financial charts,
 * and Weka-based predictions. Now also manages Accounts and Categories.
 */
public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private User currentUser;

    @FXML private Label welcomeLabel;
    @FXML private TabPane dashboardTabPane;

    // Overview Tab
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netBalanceLabel;
    @FXML private Label totalPredictedExpenseLabel;
    @FXML private VBox predictedExpensesVBox;
    @FXML private PieChart expensePieChart;
    @FXML private BarChart<String, Number> monthlyExpensesBarChart;
    @FXML private CategoryAxis monthlyExpensesXAxis;
    @FXML private NumberAxis monthlyExpensesYAxis;
    @FXML private LineChart<String, Number> expenseTrendsLineChart;
    @FXML private CategoryAxis expenseTrendsXAxis;
    @FXML private NumberAxis expenseTrendsYAxis;

    // Transactions List Tab
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> colTransDate;
    @FXML private TableColumn<Transaction, Double> colTransAmount;
    @FXML private TableColumn<Transaction, String> colTransType;
    @FXML private TableColumn<Transaction, String> colTransCategory;
    @FXML private TableColumn<Transaction, String> colTransDescription;
    @FXML private TableColumn<Transaction, String> colTransAccount;

    // Budgeting Tab
    @FXML private TableView<BudgetWrapper> budgetTable;
    @FXML private TableColumn<BudgetWrapper, String> colBudgetCategory;
    @FXML private TableColumn<BudgetWrapper, Double> colBudgetLimit;
    @FXML private TableColumn<BudgetWrapper, String> colBudgetMonth;
    @FXML private TableColumn<BudgetWrapper, String> colBudgetYear;
    @FXML private TableColumn<BudgetWrapper, Double> colBudgetSpent;
    @FXML private TableColumn<BudgetWrapper, Double> colBudgetRemaining;
    @FXML private TableColumn<BudgetWrapper, String> colBudgetStatus;

    // Accounts Tab
    @FXML private TableView<Account> accountTable;
    @FXML private TableColumn<Account, Integer> colAccountId;
    @FXML private TableColumn<Account, String> colAccountName;
    @FXML private TableColumn<Account, String> colAccountType;
    @FXML private TableColumn<Account, Double> colAccountInitialBalance;
    @FXML private TableColumn<Account, Double> colAccountCurrentBalance;
    @FXML private TableColumn<Account, LocalDateTime> colAccountCreatedAt;

    // Categories Tab
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Integer> colCategoryId;
    @FXML private TableColumn<Category, String> colCategoryName;
    @FXML private TableColumn<Category, String> colCategoryType;
    @FXML private TableColumn<Category, Boolean> colCategoryIsDefault;
    @FXML private TableColumn<Category, LocalDateTime> colCategoryCreatedAt;

    /**
     * Initializes the controller. This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            Platform.runLater(() -> {
                AlertUtil.showError("Session Error", "No user logged in.", "Please log in to continue.");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/financeapp/view/Login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Login");
                    stage.show();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to load login FXML after session error.", e);
                }
            });
            return;
        }

        welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");

        // Initialize Transaction Table columns
        colTransDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTransactionDate()));
        colTransAmount.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount()));
        colTransType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        colTransCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
        colTransDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        colTransAccount.setCellValueFactory(cellData -> {
            Account account = accountDAO.getAccountById(cellData.getValue().getAccountId());
            return new SimpleStringProperty(account != null ? account.getAccountName() : "N/A");
        });


        // Initialize Budget Table columns
        colBudgetCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        colBudgetLimit.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmountLimit()));
        colBudgetMonth.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMonthString()));
        colBudgetYear.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getYear())));
        colBudgetSpent.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmountSpent()));
        colBudgetRemaining.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmountRemaining()));
        colBudgetStatus.setCellValueFactory(cellData -> {
            SimpleStringProperty property = new SimpleStringProperty(cellData.getValue().getStatus());
            Platform.runLater(() -> { /* CSS styling logic could go here */ });
            return property;
        });

        // Initialize Accounts Table columns
        colAccountId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAccountId()));
        colAccountName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountName()));
        colAccountType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountType()));
        colAccountInitialBalance.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getInitialBalance()));
        colAccountCurrentBalance.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCurrentBalance()));
        colAccountCreatedAt.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));

        // Initialize Categories Table columns
        colCategoryId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCategoryId()));
        colCategoryName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
        colCategoryType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryType()));
        colCategoryIsDefault.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().isDefault()));
        colCategoryCreatedAt.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));

        // Initialize Weka Predictor at startup using your provided method
        WekaPredictor.initialize(); // Calls the initialize() method in your WekaPredictor

        // Initial refresh
        refreshDashboard();

        // Add listeners to tabs to refresh data when a tab is selected
        dashboardTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                LOGGER.log(Level.INFO, "Tab switched to: " + newTab.getText());
                refreshDashboard();
            }
        });
    }

    /**
     * Refreshes all data displayed on the dashboard, including tables, summaries, and charts.
     */
    public void refreshDashboard() {
        if (currentUser == null) return;

        Tab selectedTab = dashboardTabPane.getSelectionModel().getSelectedItem();
        String tabText = (selectedTab != null) ? selectedTab.getText() : "";

        // These are typically on the Overview tab, but if the user switches quickly,
        // it's good to ensure they are updated when any tab is loaded.
        updateFinancialSummary();
        updatePredictedExpenses(); // This now relies on the pre-loaded models from WekaPredictor
        updateCharts();

        if ("Transactions List".equals(tabText)) {
            refreshTransactionTable();
        } else if ("Budgeting".equals(tabText)) {
            refreshBudgetTable();
        } else if ("Accounts".equals(tabText)) {
            refreshAccountTable();
        } else if ("Categories".equals(tabText)) {
            refreshCategoryTable();
        }
    }


    // --- Transaction Management ---

    /**
     * Populates the transaction table with the current user's transactions.
     */
    private void refreshTransactionTable() {
        List<Transaction> transactions = transactionDAO.getTransactionsByUserId(currentUser.getUserId());
        ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(transactions);
        transactionTable.setItems(observableTransactions);
    }

    /**
     * Handles adding a new transaction. Opens a separate form for input.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleAddTransaction(ActionEvent event) {
        if (accountDAO.getAccountsByUserId(currentUser.getUserId()).isEmpty()) {
            AlertUtil.showWarning("No Accounts", "Cannot Add Transaction", "Please add at least one account before adding transactions.");
            return;
        }
        openTransactionForm(null);
    }

    /**
     * Handles editing a selected transaction. Opens a separate form pre-filled with transaction data.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleEditTransaction(ActionEvent event) {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            openTransactionForm(selectedTransaction);
        } else {
            AlertUtil.showWarning("No Selection", "No Transaction Selected", "Please select a transaction to edit.");
        }
    }

    /**
     * Handles deleting a selected transaction.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleDeleteTransaction(ActionEvent event) {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            Optional<ButtonType> result = AlertUtil.showConfirmation("Confirm Deletion", "Delete Transaction?", "Are you sure you want to delete this transaction?");
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (transactionDAO.deleteTransaction(selectedTransaction.getTransactionId(), currentUser.getUserId())) {
                    AlertUtil.showInfo("Success", "Transaction Deleted", "Transaction has been successfully deleted.");
                    refreshDashboard();
                } else {
                    AlertUtil.showError("Error", "Deletion Failed", "Could not delete transaction. Please try again.");
                }
            }
        } else {
            AlertUtil.showWarning("No Selection", "No Transaction Selected", "Please select a transaction to delete.");
        }
    }

    /**
     * Opens the TransactionForm window for adding or editing a transaction.
     * @param transaction The transaction to edit, or null if adding a new one.
     */
    private void openTransactionForm(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/financeapp/view/TransactionForm.fxml")));
            Parent parent = loader.load();

            TransactionFormController controller = loader.getController();
            controller.setDashboardController(this);
            controller.setCurrentUser(currentUser);
            controller.setTransaction(transaction);

            Stage stage = new Stage();
            stage.setTitle(transaction == null ? "Add New Transaction" : "Edit Transaction");
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.showAndWait();
            refreshDashboard();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading TransactionForm.fxml", e);
            AlertUtil.showError("UI Error", "Could not load transaction form.", "Please check FXML file syntax and integrity.");
        }
    }

    // --- Budget Management ---

    /**
     * Populates the budget table with the current user's budgets and calculated utilization.
     */
    private void refreshBudgetTable() {
        List<Budget> budgets = budgetDAO.getBudgetsByUserId(currentUser.getUserId());
        ObservableList<BudgetWrapper> budgetWrappers = FXCollections.observableArrayList();

        for (Budget budget : budgets) {
            double spentAmount = transactionDAO.getTotalExpenseForCategoryMonthYear(
                    currentUser.getUserId(), budget.getCategoryId(), budget.getMonth(), budget.getYear()
            );

            String status = "On Track";
            double remaining = budget.getAmountLimit() - spentAmount;
            if (spentAmount > budget.getAmountLimit()) {
                status = "Over Budget";
            } else if (remaining <= budget.getAmountLimit() * 0.10 && remaining > 0) {
                status = "Nearing Limit";
            }

            budgetWrappers.add(new BudgetWrapper(budget, spentAmount, remaining, status));
        }

        budgetWrappers.sort(Comparator
                .comparingInt((BudgetWrapper bw) -> bw.getYear())
                .reversed()
                .thenComparingInt(bw -> bw.getMonth())
                .reversed()
                .thenComparing(BudgetWrapper::getCategory));

        budgetTable.setItems(budgetWrappers);
    }

    /**
     * Handles setting a new budget. Opens a separate form for input.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleAddBudget(ActionEvent event) {
        if (categoryDAO.getCategoriesByUserIdAndType(currentUser.getUserId(), "Expense").isEmpty()) {
            AlertUtil.showWarning("No Expense Categories", "Cannot Set Budget", "Please add at least one expense category in the Categories tab before setting budgets.");
            return;
        }
        openBudgetForm(null);
    }

    /**
     * Handles editing a selected budget. Opens a separate form pre-filled with budget data.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleEditBudget(ActionEvent event) {
        BudgetWrapper selectedBudgetWrapper = budgetTable.getSelectionModel().getSelectedItem();
        if (selectedBudgetWrapper != null) {
            openBudgetForm(selectedBudgetWrapper.getBudget());
        } else {
            AlertUtil.showWarning("No Selection", "No Budget Selected", "Please select a budget to edit.");
        }
    }

    /**
     * Handles deleting a selected budget.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleDeleteBudget(ActionEvent event) {
        BudgetWrapper selectedBudgetWrapper = budgetTable.getSelectionModel().getSelectedItem();
        if (selectedBudgetWrapper != null) {
            Optional<ButtonType> result = AlertUtil.showConfirmation("Confirm Deletion", "Delete Budget?", "Are you sure you want to delete this budget?");
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (budgetDAO.deleteBudget(selectedBudgetWrapper.getBudgetId(), currentUser.getUserId())) {
                    AlertUtil.showInfo("Success", "Budget Deleted", "Budget has been successfully deleted.");
                    refreshDashboard();
                } else {
                    AlertUtil.showError("Error", "Deletion Failed", "Could not delete budget. Please try again.");
                }
            }
        } else {
            AlertUtil.showWarning("No Selection", "No Budget Selected", "Please select a budget to delete.");
        }
    }

    /**
     * Opens the BudgetForm window for adding or editing a budget.
     * @param budget The budget to edit, or null if adding a new one.
     */
    private void openBudgetForm(Budget budget) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/financeapp/view/BudgetForm.fxml")));
            Parent parent = loader.load();

            BudgetFormController controller = loader.getController();
            controller.setDashboardController(this);
            controller.setCurrentUser(currentUser);
            controller.setBudget(budget);

            Stage stage = new Stage();
            stage.setTitle(budget == null ? "Set New Budget" : "Edit Budget");
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.showAndWait();
            refreshDashboard();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading BudgetForm.fxml", e);
            AlertUtil.showError("UI Error", "Could not load budget form.", "Please check FXML file syntax and integrity.");
        }
    }

    /**
     * Helper class to wrap Budget object with calculated fields for TableView display.
     */
    public static class BudgetWrapper {
        private final Budget budget;
        private final SimpleDoubleProperty amountSpent;
        private final SimpleDoubleProperty amountRemaining;
        private final SimpleStringProperty status;

        public BudgetWrapper(Budget budget, double amountSpent, double doubleRemaining, String status) {
            this.budget = budget;
            this.amountSpent = new SimpleDoubleProperty(amountSpent);
            this.amountRemaining = new SimpleDoubleProperty(doubleRemaining);
            this.status = new SimpleStringProperty(status);
        }

        public int getBudgetId() { return budget.getBudgetId(); }
        public int getUserId() { return budget.getUserId(); }
        public int getCategoryId() { return budget.getCategoryId(); }
        public String getCategory() { return budget.getCategoryName(); }
        public double getAmountLimit() { return budget.getAmountLimit(); }
        public int getMonth() { return budget.getMonth(); }
        public int getYear() { return budget.getYear(); }
        public String getMonthString() {
            // Use YearMonth to handle cases like January for previous year correctly
            return YearMonth.of(getYear(), getMonth()).format(DateTimeFormatter.ofPattern("MMM"));
        }
        public Budget getBudget() { return budget; }

        public double getAmountSpent() { return amountSpent.get(); }
        public SimpleDoubleProperty amountSpentProperty() { return amountSpent; }
        public double getAmountRemaining() { return amountRemaining.get(); }
        public SimpleDoubleProperty amountRemainingProperty() { return amountRemaining; }
        public String getStatus() { return status.get(); }
        public SimpleStringProperty statusProperty() { return status; }
    }

    // --- Financial Summary & Charts ---

    /**
     * Updates the total income and expenses for the current month.
     */
    private void updateFinancialSummary() {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        double totalIncome = transactionDAO.getTotalIncomeForMonth(currentUser.getUserId(), currentMonth, currentYear);
        double totalExpenses = transactionDAO.getTotalExpensesForMonth(currentUser.getUserId(), currentMonth, currentYear);
        double netBalance = totalIncome - totalExpenses;

        totalIncomeLabel.setText(String.format("₹%.2f", totalIncome));
        totalExpensesLabel.setText(String.format("₹%.2f", totalExpenses));
        netBalanceLabel.setText(String.format("₹%.2f", netBalance));
        if (netBalance >= 0) {
            netBalanceLabel.getStyleClass().setAll("value-label", "green");
        } else {
            netBalanceLabel.getStyleClass().setAll("value-label", "red");
        }
    }

    /**
     * Updates all charts (Pie, Bar, Line) with the latest financial data.
     */
    private void updateCharts() {
        // Clear previous chart data to prevent accumulation
        expensePieChart.getData().clear();
        // Clear old series data from BarChart and LineChart explicitly
        monthlyExpensesBarChart.getData().clear();
        expenseTrendsLineChart.getData().clear();


        // 1. Pie Chart: Expense Categories Breakdown for Current Month
        Map<String, Double> categoryBreakdown = transactionDAO.getExpenseCategoriesBreakdown(currentUser.getUserId());
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        if (categoryBreakdown.isEmpty() || categoryBreakdown.values().stream().allMatch(v -> v == 0.0)) {
            pieChartData.add(new PieChart.Data("No Expenses This Month", 1.0)); // Show empty slice if no data or all zero
        } else {
            for (Map.Entry<String, Double> entry : categoryBreakdown.entrySet()) {
                if (entry.getValue() > 0) { // Only add categories with actual positive expenses
                    pieChartData.add(new PieChart.Data(entry.getKey() + " (₹" + String.format("%.2f", entry.getValue()) + ")", entry.getValue()));
                }
            }
            if (pieChartData.isEmpty()) { // If all values were 0 after filtering, show "No Expenses"
                pieChartData.add(new PieChart.Data("No Expenses This Month", 1.0));
            }
        }
        expensePieChart.setData(pieChartData);
        expensePieChart.setTitle("Expense Categories Breakdown (Current Month)");

        // 2. Bar Chart: Monthly Expenses Comparison (Last 6 Months)
        Map<String, Double> monthlyExpensesRaw = transactionDAO.getMonthlyExpenses(currentUser.getUserId());
        // Use TreeMap to ensure natural sorting (YYYY-MM) for months on the chart
        Map<String, Double> monthlyExpenses = new TreeMap<>(monthlyExpensesRaw);

        XYChart.Series<String, Number> monthlySeries = new XYChart.Series<>();
        monthlySeries.setName("Monthly Expenses");

        // Dynamically get the last 6 months in "YYYY-MM" format, including current month
        LocalDate today = LocalDate.now();
        List<String> lastSixMonthsLabels = new ArrayList<>();
        for (int i = 5; i >= 0; i--) { // Iterate from 5 months ago up to current month
            lastSixMonthsLabels.add(today.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }

        for (String monthYearLabel : lastSixMonthsLabels) {
            // Ensure all 6 months are present, even if no data, to show "0"
            double expense = monthlyExpenses.getOrDefault(monthYearLabel, 0.0);
            monthlySeries.getData().add(new XYChart.Data<>(monthYearLabel, expense));
        }
        monthlyExpensesBarChart.getData().add(monthlySeries);
        monthlyExpensesBarChart.setTitle("Monthly Expenses Comparison");


        // 3. Line Chart: Expense Trends Over Time (All Expenses)
        List<Transaction> allExpenses = transactionDAO.getTransactionsByUserId(currentUser.getUserId()).stream()
                .filter(t -> t.getType().equals("Expense"))
                .sorted(Comparator.comparing(Transaction::getTransactionDate)) // Sort by date for trend
                .collect(Collectors.toList());

        // Group by date and sum expenses for that date
        // Use TreeMap to ensure dates are sorted for the chart
        Map<LocalDate, Double> dailyExpenses = allExpenses.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionDate,
                        TreeMap::new, // Ensures sorted keys
                        Collectors.summingDouble(Transaction::getAmount)));

        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("Daily Expense Trend");

        // Use a shorter date format for the X-axis to prevent overlapping
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd"); // Shorter format

        if (dailyExpenses.isEmpty()) {
            // Handle case where there are no expenses at all
            trendSeries.getData().add(new XYChart.Data<>("No Data", 0)); // Add a placeholder point
        } else {
            for (Map.Entry<LocalDate, Double> entry : dailyExpenses.entrySet()) {
                trendSeries.getData().add(new XYChart.Data<>(entry.getKey().format(dateFormatter), entry.getValue()));
            }
        }
        expenseTrendsLineChart.getData().add(trendSeries);
        expenseTrendsLineChart.setTitle("Expense Trends Over Time");
    }

    /**
     * Uses Weka to predict next month's expenses for each category and displays them.
     * Also calculates the total predicted expense.
     * This method now uses the pre-trained model loaded by WekaPredictor.initialize().
     */
    private void updatePredictedExpenses() {
        predictedExpensesVBox.getChildren().clear(); // Clear previous predictions
        double overallPredictedExpense = 0.0;

        List<Category> expenseCategories = categoryDAO.getCategoriesByUserIdAndType(currentUser.getUserId(), "Expense");

        if (expenseCategories.isEmpty()) {
            predictedExpensesVBox.getChildren().add(new Label("No expense categories defined for predictions."));
            totalPredictedExpenseLabel.setText("₹0.00");
            return;
        }

        predictedExpensesVBox.getChildren().add(new Label("By Category:"));
        for (Category category : expenseCategories) {
            // Retrieve last 3 months of spending for this specific category for prediction input.
            // getHistoricalMonthlySpendingForCategory returns [M3, M2, M1] (oldest to most recent).
            List<Double> historicalData = transactionDAO.getHistoricalMonthlySpendingForCategory(currentUser.getUserId(), category.getCategoryId(), 3);

            // Ensure we have exactly 3 data points, padding with 0.0 if not enough history
            // The `getHistoricalMonthlySpendingForCategory` already pads to `monthsBack`
            // and returns in order (oldest to most recent).
            double m3_oldest = historicalData.size() >= 3 ? historicalData.get(0) : 0.0;
            double m2_middle = historicalData.size() >= 3 ? historicalData.get(1) : 0.0;
            double m1_mostRecent = historicalData.size() >= 3 ? historicalData.get(2) : 0.0;

            // Predict using Weka. Your WekaPredictor.predictNextMonthExpense expects parameters
            // (category, PastSpendingM1, PastSpendingM2, PastSpendingM3)
            // where PastSpendingM1 is the most recent (M1), PastSpendingM2 is M2, PastSpendingM3 is M3.
            double predictedAmount = WekaPredictor.predictNextMonthExpense(
                    category.getCategoryName(),
                    m1_mostRecent,    // This corresponds to PastSpendingM1 in WekaPredictor
                    m2_middle,        // This corresponds to PastSpendingM2 in WekaPredictor
                    m3_oldest         // This corresponds to PastSpendingM3 in WekaPredictor
            );

            predictedAmount = Math.max(0, predictedAmount); // Ensure prediction is not negative
            overallPredictedExpense += predictedAmount;

            // Display prediction
            Label predictionLabel = new Label(String.format(" - %s: ₹%.2f", category.getCategoryName(), predictedAmount));
            predictedExpensesVBox.getChildren().add(predictionLabel);
        }
        totalPredictedExpenseLabel.setText(String.format("₹%.2f", overallPredictedExpense));
    }


    // --- Account Management ---

    /**
     * Refreshes the account table with the current user's accounts.
     */
    private void refreshAccountTable() {
        List<Account> accounts = accountDAO.getAccountsByUserId(currentUser.getUserId());
        ObservableList<Account> observableAccounts = FXCollections.observableArrayList(accounts);
        accountTable.setItems(observableAccounts);
    }

    /**
     * Handles adding a new account. Opens a separate form for input.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleAddAccount(ActionEvent event) {
        if (accountDAO.getAccountsByUserId(currentUser.getUserId()).isEmpty()) {
            AlertUtil.showWarning("No Accounts", "Cannot Add Transaction", "Please add at least one account before adding transactions.");
            return;
        }
        openAccountForm(null);
    }

    /**
     * Handles editing a selected account. Opens a separate form pre-filled with account data.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleEditAccount(ActionEvent event) {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            openAccountForm(selectedAccount);
        } else {
            AlertUtil.showWarning("No Selection", "No Account Selected", "Please select an account to edit.");
        }
    }

    /**
     * Handles deleting a selected account.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            AlertUtil.showWarning("No Selection", "No Account Selected", "Please select an account to delete.");
            return;
        }

        List<Transaction> linkedTransactions = transactionDAO.getTransactionsByAccountId(selectedAccount.getAccountId());
        if (!linkedTransactions.isEmpty()) {
            AlertUtil.showWarning("Cannot Delete Account", "Transactions Linked",
                    "This account has associated transactions. Please delete or reassign them before deleting the account.");
            return;
        }

        Optional<ButtonType> result = AlertUtil.showConfirmation("Confirm Deletion", "Delete Account?", "Are you sure you want to delete this account?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (accountDAO.deleteAccount(selectedAccount.getAccountId(), currentUser.getUserId())) {
                AlertUtil.showInfo("Success", "Account Deleted", "Account has been successfully deleted.");
                refreshDashboard();
            } else {
                AlertUtil.showError("Error", "Deletion Failed", "Could not delete account. Please try again.");
            }
        }
    }

    /**
     * Opens the AccountForm window for adding or editing an account.
     * @param account The account to edit, or null if adding a new one.
     */
    private void openAccountForm(Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/financeapp/view/AccountForm.fxml")));
            Parent parent = loader.load();

            AccountFormController controller = loader.getController();
            controller.setDashboardController(this);
            controller.setCurrentUser(currentUser);
            controller.setAccount(account);

            Stage stage = new Stage();
            stage.setTitle(account == null ? "Add New Account" : "Edit Account");
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.showAndWait();
            refreshDashboard();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading AccountForm.fxml", e);
            AlertUtil.showError("UI Error", "Could not load account form.", "Please check FXML file syntax and integrity.");
        }
    }


    // --- Category Management ---

    /**
     * Refreshes the category table with the current user's categories.
     */
    private void refreshCategoryTable() {
        List<Category> categories = categoryDAO.getCategoriesByUserId(currentUser.getUserId());
        ObservableList<Category> observableCategories = FXCollections.observableArrayList(categories);
        categoryTable.setItems(observableCategories);
    }

    /**
     * Handles adding a new category. Opens a separate form for input.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleAddCategory(ActionEvent event) {
        openCategoryForm(null);
    }

    /**
     * Handles editing a selected category. Opens a separate form pre-filled with category data.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleEditCategory(ActionEvent event) {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            openCategoryForm(selectedCategory);
        } else {
            AlertUtil.showWarning("No Selection", "No Category Selected", "Please select a category to edit.");
        }
    }

    /**
     * Handles deleting a selected category.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleDeleteCategory(ActionEvent event) {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            AlertUtil.showWarning("No Selection", "No Category Selected", "Please select a category to delete.");
            return;
        }
        if (selectedCategory.isDefault()) {
            AlertUtil.showWarning("Deletion Forbidden", "Cannot Delete Default Category", "System default categories cannot be deleted.");
            return;
        }

        List<Transaction> linkedTransactions = transactionDAO.getTransactionsByCategoryId(selectedCategory.getCategoryId());
        List<Budget> linkedBudgets = budgetDAO.getBudgetsByCategoryId(selectedCategory.getCategoryId());

        if (!linkedTransactions.isEmpty()) {
            AlertUtil.showWarning("Cannot Delete Category", "Transactions Linked",
                    "This category has associated transactions. Please delete or reassign them before deleting the category.");
            return;
        }
        if (!linkedBudgets.isEmpty()) {
            AlertUtil.showWarning("Cannot Delete Category", "Budgets Linked",
                    "This category has associated budgets. Please delete them before deleting the category.");
            return;
        }

        Optional<ButtonType> result = AlertUtil.showConfirmation("Confirm Deletion", "Delete Category?", "Are you sure you want to delete this category?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (categoryDAO.deleteCategory(selectedCategory.getCategoryId(), currentUser.getUserId())) {
                AlertUtil.showInfo("Success", "Category Deleted", "Category has been successfully deleted.");
                refreshDashboard();
            } else {
                AlertUtil.showError("Error", "Deletion Failed", "Could not delete category. Please try again.");
            }
        }
    }

    /**
     * Opens the CategoryForm window for adding or editing a category.
     * @param category The category to edit, or null if adding a new one.
     */
    private void openCategoryForm(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/financeapp/view/CategoryForm.fxml")));
            Parent parent = loader.load();

            CategoryFormController controller = loader.getController();
            controller.setDashboardController(this);
            controller.setCurrentUser(currentUser);
            controller.setCategory(category);

            Stage stage = new Stage();
            stage.setTitle(category == null ? "Add New Category" : "Edit Category");
            stage.setScene(new Scene(parent));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.showAndWait();
            refreshDashboard();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading CategoryForm.fxml", e);
            AlertUtil.showError("UI Error", "Could not load category form.", "Please check FXML file syntax and integrity.");
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
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Personal Finance Login");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load login FXML after logout.", e);
            AlertUtil.showError("Navigation Error", "Could not load login screen.", "Please restart the application.");
        }
    }
}
