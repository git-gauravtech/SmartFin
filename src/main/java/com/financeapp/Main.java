// src/main/java/com/financeapp/Main.java
package com.financeapp;

import com.financeapp.dao.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the Personal Finance Management System application.
 * Handles application startup, database connection testing, and initial scene loading.
 */
public class Main extends Application {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * The start method is the main entry point for all JavaFX applications.
     * It is called after the init method returns, and after the system is ready for the application to begin running.
     *
     * @param stage The primary stage for this application, onto which the application scene can be set.
     * @throws IOException If the FXML file cannot be loaded.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Test database connection at startup
        if (!testDatabaseConnection()) {
            // If connection fails, log error and exit gracefully.
            // An alert would ideally be shown here, but direct exit is used for simplicity
            // if we assume database setup is a prerequisite for running.
            LOGGER.log(Level.SEVERE, "Database connection failed. Exiting application.");
            System.exit(1);
        }

        // Load the Login FXML
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/financeapp/view/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // Set initial scene size
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm()); // Load CSS

        stage.setTitle("Personal Finance Login");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Tests the database connection.
     * @return true if the connection is successful, false otherwise.
     */
    private boolean testDatabaseConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                LOGGER.log(Level.INFO, "Database connection successful!");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * The main method is ignored in JavaFX applications.
     * The `main` method in the `Application` class is typically overridden by the launcher.
     * However, it is good practice to include it for IDEs that may not support direct JavaFX launching.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
