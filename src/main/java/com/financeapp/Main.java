// src/main/java/com/financeapp/Main.java
package com.financeapp;

import com.financeapp.dao.AccountDAO; // New import
import com.financeapp.dao.CategoryDAO; // New import
import com.financeapp.model.User;
import com.financeapp.utils.SessionManager;
import com.financeapp.utils.WekaPredictor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the AI-Based Personal Finance Management System application.
 * Initializes Weka models and loads the login screen.
 */
public class Main extends Application {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private final CategoryDAO categoryDAO = new CategoryDAO(); // New instance
    private final AccountDAO accountDAO = new AccountDAO(); // New instance

    /**
     * The start method is the main entry point for all JavaFX applications.
     * @param primaryStage The primary stage for this application.
     * @throws Exception If an error occurs during application startup.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize Weka models on application start
        WekaPredictor.initialize();

        // Add a listener to the SessionManager to handle default category/account insertion on login
        SessionManager.getInstance().currentUserProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                // User has logged in. Check and insert default categories/accounts if needed.
                LOGGER.log(Level.INFO, "User logged in: " + newUser.getUsername() + ". Checking for default categories/accounts.");
                categoryDAO.insertDefaultCategories(newUser.getUserId());
                accountDAO.insertDefaultAccounts(newUser.getUserId());
            }
        });

        // Load the Login screen
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/financeapp/view/Login.fxml")));
        Parent root = loader.load();

        primaryStage.setTitle("Personal Finance Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * The main method, which is the entry point for the Java application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
