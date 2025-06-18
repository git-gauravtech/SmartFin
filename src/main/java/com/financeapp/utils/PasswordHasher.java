// src/main/java/com/financeapp/utils/PasswordHasher.java
package com.financeapp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for secure password hashing and verification.
 * Uses SHA-256 for hashing and generates a random salt for each password.
 * <p>
 * NOTE: For higher security in production, consider using libraries that implement
 * more robust algorithms like BCrypt or Scrypt, which are designed to be
 * computationally expensive and resistant to brute-force attacks.
 */
public class PasswordHasher {

    private static final Logger LOGGER = Logger.getLogger(PasswordHasher.class.getName());
    private static final int SALT_LENGTH = 16; // 16 bytes for salt

    /**
     * Generates a random salt for password hashing.
     *
     * @return A Base64 encoded string representation of the salt.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password using SHA-256 with a given salt.
     *
     * @param password The plain-text password to hash.
     * @param salt     The salt to use for hashing (Base64 encoded string).
     * @return The Base64 encoded hashed password, or null if an error occurs.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt)); // Add salt to the digest
            byte[] hashedPassword = md.digest(password.getBytes()); // Hash the password
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "SHA-256 algorithm not found!", e);
        }
        return null;
    }

    /**
     * Verifies a plain-text password against a stored hashed password and salt.
     *
     * @param plainPassword        The plain-text password to verify.
     * @param storedHashedPassword The hashed password retrieved from the database.
     * @param storedSalt           The salt retrieved from the database.
     * @return True if the plain password matches the stored hash, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String storedHashedPassword, String storedSalt) {
        String hashedPasswordAttempt = hashPassword(plainPassword, storedSalt);
        return hashedPasswordAttempt != null && hashedPasswordAttempt.equals(storedHashedPassword);
    }

    // Main method for testing hashing (can be removed in production)
    public static void main(String[] args) {
        String password = "testPassword123";
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

        System.out.println("Original Password: " + password);
        System.out.println("Generated Salt: " + salt);
        System.out.println("Hashed Password: " + hashedPassword);

        // Test verification
        boolean verified = verifyPassword(password, hashedPassword, salt);
        System.out.println("Password Verified: " + verified); // Should be true

        boolean failedVerified = verifyPassword("wrongPassword", hashedPassword, salt);
        System.out.println("Wrong Password Verified: " + failedVerified); // Should be false
    }
}
