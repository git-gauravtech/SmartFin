// src/main/java/com/financeapp/controller/LoginController.java
package com.financeapp.controller;

import com.financeapp.dao.UserDAO;
import com.financeapp.model.User;
import com.financeapp.utils.AlertUtil;
import com.financeapp.utils.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Login screen.
 * Handles user authentication and navigates to the appropriate dashboard (User or Admin).
 */
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField; // For showing password
    @FXML private Button togglePasswordVisibilityButton; // To toggle password visibility

    private final UserDAO userDAO = new UserDAO();

    private boolean passwordIsVisible = false; // Track password field state

    /**
     * Initializes the controller. Sets up listeners for password visibility toggle.
     */
    @FXML
    public void initialize() {
        // Bind the text of the visible field to the hidden field for synchronization
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    /**
     * Handles the login button action. Authenticates the user and navigates to the dashboard.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showWarning("Login Failed", "Missing Credentials", "Please enter both username and password.");
            return;
        }

        User user = userDAO.loginUser(username, password);

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            AlertUtil.showInfo("Login Successful", "Welcome!", "You have successfully logged in as " + user.getUsername() + ".");

            try {
                String fxmlPath = user.isAdmin() ? "/com/financeapp/view/AdminDashboard.fxml" : "/com/financeapp/view/Dashboard.fxml";
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle(user.isAdmin() ? "Admin Dashboard" : "Personal Finance Dashboard");
                stage.setMaximized(true); // Maximize the window
                stage.show();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to load dashboard FXML.", e);
                AlertUtil.showError("Navigation Error", "Could not load dashboard.", "Please try again later.");
            }
        } else {
            AlertUtil.showError("Login Failed", "Authentication Error", "Invalid username or password.");
        }
    }

    /**
     * Handles navigating to the signup screen.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/financeapp/view/Signup.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Personal Finance Signup");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load signup FXML.", e);
            AlertUtil.showError("Navigation Error", "Could not load signup screen.", "Please try again later.");
        }
    }

    /**
     * Toggles the visibility of the password field.
     */
    @FXML
    private void togglePasswordVisibility() {
        passwordIsVisible = !passwordIsVisible;
        if (passwordIsVisible) {
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordVisibilityButton.setText("Hide");
        } else {
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordVisibilityButton.setText("Show");
        }
    }
}
