// src/main/java/com/financeapp/dao/DatabaseConnection.java
package com.financeapp.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to manage database connections.
 * Provides a singleton-like method to get and close database connections.
 * <p>
 * IMPORTANT: Update DB_URL, DB_USER, and DB_PASSWORD with your MySQL credentials.
 */
public class DatabaseConnection {

    // JDBC URL for your MySQL database. Replace 'localhost:3306' and 'finance_app_db'.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/finance_app_db";
    // Database username
    private static final String DB_USER = "root";
    // Database password
    private static final String DB_PASSWORD = "Kavita@0020";

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    /**
     * Private constructor to prevent instantiation from outside,
     * as this is a utility class with static methods.
     */
    private DatabaseConnection() {
        // Private constructor
    }

    /**
     * Establishes and returns a connection to the database.
     *
     * @return A valid Connection object if successful, null otherwise.
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the MySQL JDBC driver (not strictly necessary for newer JDBC versions but good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            LOGGER.log(Level.INFO, "Database connection established successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to the database!", e);
            // In a real application, you might want to show an alert to the user here.
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found!", e);
        }
        return connection;
    }

    /**
     * Closes the given database connection, if it's not null and not already closed.
     *
     * @param connection The Connection object to close.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.log(Level.INFO, "Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing database connection!", e);
            }
        }
    }
}
