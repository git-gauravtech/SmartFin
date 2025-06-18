// src/main/java/com/financeapp/controller/SignupController.java
package com.financeapp.controller;

import com.financeapp.dao.UserDAO;
import com.financeapp.utils.AlertUtil;
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
 * Controller for the Signup screen.
 * Handles new user registration.
 */
public class SignupController {

    private static final Logger LOGGER = Logger.getLogger(SignupController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField; // For showing password
    @FXML private Button togglePasswordVisibilityButton; // To toggle password visibility

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField; // For showing confirm password
    @FXML private Button toggleConfirmPasswordVisibilityButton; // To toggle confirm password visibility

    private final UserDAO userDAO = new UserDAO();

    private boolean passwordIsVisible = false;
    private boolean confirmPasswordIsVisible = false;

    /**
     * Initializes the controller. Sets up listeners for password visibility toggles.
     */
    @FXML
    public void initialize() {
        // Bind the text of the visible fields to the hidden fields for synchronization
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
    }

    /**
     * Handles the signup button action. Registers a new user.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleSignup(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showWarning("Signup Failed", "Missing Information", "All fields are required. Please fill them out.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            AlertUtil.showWarning("Signup Failed", "Password Mismatch", "Password and Confirm Password do not match.");
            return;
        }

        // Attempt to register the user (not as admin by default)
        if (userDAO.registerUser(username, password, false)) {
            AlertUtil.showInfo("Signup Successful", "Account Created", "Your account has been created. Please log in.");
            handleLogin(event); // Redirect to login page
        } else {
            AlertUtil.showError("Signup Failed", "Registration Error", "Username might already exist or an error occurred during registration. Please try a different username.");
        }
    }

    /**
     * Handles navigating back to the login screen.
     * @param event The ActionEvent that triggered this method.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/financeapp/view/Login.fxml")));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Personal Finance Login");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load login FXML.", e);
            AlertUtil.showError("Navigation Error", "Could not load login screen.", "Please try again later.");
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

    /**
     * Toggles the visibility of the confirm password field.
     */
    @FXML
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordIsVisible = !confirmPasswordIsVisible;
        if (confirmPasswordIsVisible) {
            confirmPasswordVisibleField.setVisible(true);
            confirmPasswordVisibleField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            toggleConfirmPasswordVisibilityButton.setText("Hide");
        } else {
            confirmPasswordVisibleField.setVisible(false);
            confirmPasswordVisibleField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            toggleConfirmPasswordVisibilityButton.setText("Show");
        }
    }
}
