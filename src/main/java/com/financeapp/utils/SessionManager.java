// src/main/java/com/financeapp/utils/SessionManager.java
package com.financeapp.utils;

import com.financeapp.model.User;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * Manages the current user session. This is a Singleton class.
 */
public class SessionManager {

    private static SessionManager instance;
    private final ReadOnlyObjectWrapper<User> currentUser = new ReadOnlyObjectWrapper<>();

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private SessionManager() {
        // Private constructor
    }

    /**
     * Returns the singleton instance of SessionManager.
     * @return The single instance of SessionManager.
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Sets the current logged-in user.
     * @param user The User object representing the logged-in user.
     */
    public void setCurrentUser(User user) {
        this.currentUser.set(user);
    }

    /**
     * Returns the current logged-in user.
     * @return The current User object, or null if no user is logged in.
     */
    public User getCurrentUser() {
        return currentUser.get();
    }

    /**
     * Clears the current user session (logs out the user).
     */
    public void logout() {
        this.currentUser.set(null);
    }

    /**
     * Returns the ReadOnlyObjectProperty for the current user.
     * This allows other components to observe changes in the logged-in user.
     * @return The ReadOnlyObjectProperty for the current user.
     */
    public ReadOnlyObjectProperty<User> currentUserProperty() {
        return currentUser.getReadOnlyProperty();
    }
}
