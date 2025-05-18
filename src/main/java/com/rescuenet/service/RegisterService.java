package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.UserModel;

/**
 * @author Prayash Rawal
 */

/**
 * Service class for handling user registration in the RescueNet application.
 * Validates username and email uniqueness and adds new users to the database.
 */
public class RegisterService {

    private Connection dbConn; // Instance variable for connection
    private String lastErrorMessage;
    private boolean isConnectionError = false; // Initialize to false

    /**
     * Retrieves the last error message encountered during a service operation.
     *
     * @return the last error message, or null if no error occurred
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Checks if a database connection error occurred during service initialization.
     *
     * @return true if a connection error was detected, false otherwise.
     */
    public boolean isConnectionError() {
        return isConnectionError;
    }

    /**
     * Initializes the database connection for registration operations.
     * Handles exceptions internally and sets flags.
     * This constructor does NOT throw SQLException.
     */
    public RegisterService() { // <<<< REMOVED 'throws SQLException'
        try {
            this.dbConn = DbConfig.getDbConnection(); // This might throw ClassNotFoundException or SQLException
            if (this.dbConn == null) { // Defensive check if DbConfig returns null
                this.isConnectionError = true;
                this.lastErrorMessage = "Database connection could not be established (DbConfig returned null).";
                System.err.println("RegisterService Constructor: " + this.lastErrorMessage);
            } else {
                System.out.println("RegisterService Constructor: Database connection appears to be established.");
            }
        } catch (ClassNotFoundException e) {
            this.isConnectionError = true;
            this.lastErrorMessage = "RegisterService: Database driver not found - " + e.getMessage();
            System.err.println(this.lastErrorMessage);
            e.printStackTrace();
            this.dbConn = null; // Ensure dbConn is null on this critical error
        } catch (SQLException e) {
            this.isConnectionError = true;
            this.lastErrorMessage = "RegisterService: Failed to initialize database connection - " + e.getMessage();
            System.err.println(this.lastErrorMessage);
            e.printStackTrace();
            this.dbConn = null; // Ensure dbConn is null
        } catch (Exception e) { // Catch any other unexpected runtime errors
            this.isConnectionError = true;
            this.lastErrorMessage = "RegisterService: Unexpected error during initialization - " + e.getMessage();
            System.err.println(this.lastErrorMessage);
            e.printStackTrace();
            this.dbConn = null;
        }
    }

    /**
     * Checks if the specified username already exists in the database.
     * Sets lastErrorMessage on DB error or if username is empty.
     *
     * @param username the username to check
     * @return true if the username exists or a database error occurs (fail-safe for this check),
     *         false otherwise.
     */
    public boolean isUsernameExists(String username) { // <<<< REMOVED 'throws SQLException'
        lastErrorMessage = null;
        if (this.isConnectionError || this.dbConn == null) {
            this.lastErrorMessage = "DB connection unavailable (isUsernameExists).";
            System.err.println("RegisterService: " + this.lastErrorMessage);
            return true; // Fail safe: assume exists to prevent issues if called before addUser
        }
        if (username == null || username.trim().isEmpty()) {
            this.lastErrorMessage = "Username cannot be empty for existence check.";
            System.err.println("RegisterService: " + this.lastErrorMessage);
            return false;
        }

        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = this.dbConn.prepareStatement(query)) {
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                System.out.println("RegisterService: Username '" + username + "' exists: " + exists);
                return exists;
            }
        } catch (SQLException e) {
            this.lastErrorMessage = "Error checking username availability: " + e.getMessage();
            System.err.println("RegisterService: " + this.lastErrorMessage);
            e.printStackTrace();
            return true; // Fail safe
        }
        return false;
    }

    /**
     * Checks if the specified email already exists in the database.
     * Sets lastErrorMessage on DB error or if email is empty.
     *
     * @param email the email to check
     * @return true if the email exists or a database error occurs (fail-safe for this check),
     *         false otherwise.
     */
    public boolean isEmailExists(String email) { // <<<< REMOVED 'throws SQLException'
        lastErrorMessage = null;
        if (this.isConnectionError || this.dbConn == null) {
            this.lastErrorMessage = "DB connection unavailable (isEmailExists).";
            System.err.println("RegisterService: " + this.lastErrorMessage);
            return true; // Fail safe
        }
        if (email == null || email.trim().isEmpty()) {
            this.lastErrorMessage = "Email cannot be empty for existence check.";
            System.err.println("RegisterService: " + this.lastErrorMessage);
            return false;
        }

        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = this.dbConn.prepareStatement(query)) {
            stmt.setString(1, email.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                System.out.println("RegisterService: Email '" + email + "' exists: " + exists);
                return exists;
            }
        } catch (SQLException e) {
            this.lastErrorMessage = "Error checking email availability: " + e.getMessage();
            System.err.println("RegisterService: " + this.lastErrorMessage);
            e.printStackTrace();
            return true; // Fail safe
        }
        return false;
    }

    /**
     * Adds a new user to the database after validating username and email
     * uniqueness.
     *
     * @param userModel the UserModel object containing user details
     * @return Boolean: true if success, false if username/email exists, null if DB/system error.
     */
    public Boolean addUser(UserModel userModel) { // <<<< REMOVED 'throws SQLException'
        lastErrorMessage = null;

        if (this.isConnectionError || this.dbConn == null) {
            this.lastErrorMessage = "Database connection unavailable for addUser.";
            System.err.println("RegisterService: " + this.lastErrorMessage);
            return null;
        }

        // These calls to isUsernameExists and isEmailExists will now use the versions
        // that don't throw SQLException but set lastErrorMessage.
        if (isUsernameExists(userModel.getUsername())) {
            if (this.lastErrorMessage == null) { // If isUsernameExists itself didn't have a DB problem
                this.lastErrorMessage = "Username '" + userModel.getUsername() + "' already exists.";
            }
            System.err.println("RegisterService (addUser): " + this.lastErrorMessage);
            return false;
        }
        lastErrorMessage = null; // Reset if first check was ok

        if (userModel.getEmail() != null && !userModel.getEmail().trim().isEmpty()) {
            if (isEmailExists(userModel.getEmail())) {
                if (this.lastErrorMessage == null) {
                    this.lastErrorMessage = "Email '" + userModel.getEmail() + "' already registered.";
                }
                System.err.println("RegisterService (addUser): " + this.lastErrorMessage);
                return false;
            }
            lastErrorMessage = null; // Reset if second check was ok
        }

        String insertQuery = "INSERT INTO users (username, password_hash, role_id, full_name, email, phone_number, profile_picture_path, created_at, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
        try (PreparedStatement insertStmt = this.dbConn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, userModel.getUsername());
			insertStmt.setString(2, userModel.getPasswordHash());
            insertStmt.setInt(3, userModel.getRoleId());
            insertStmt.setString(4, userModel.getFullName());
            insertStmt.setString(5, userModel.getEmail());
            insertStmt.setString(6, userModel.getPhoneNumber());
            insertStmt.setString(7, userModel.getProfilePicturePath());
            insertStmt.setBoolean(8, userModel.isActive());

            int rowsAffected = insertStmt.executeUpdate();
            System.out.println("RegisterService: Rows affected by insert: " + rowsAffected);
            if (rowsAffected > 0) {
                return true;
            } else {
                this.lastErrorMessage = "User registration failed: no rows were inserted into the database.";
                System.err.println("RegisterService: " + this.lastErrorMessage);
                return false;
            }
        } catch (SQLException e) {
            this.lastErrorMessage = "Database error during user insertion: " + e.getMessage();
            System.err.println("RegisterService: " + this.lastErrorMessage);
            e.printStackTrace();
            return null; // System error
        }
        // Note on dbConn: Connection management needs to be consistent.
        // If this is a request-scoped service, the connection should be closed.
        // If it's application-scoped (singleton), this dbConn member is problematic.
        // Assuming for now that it's managed by DbConfig or this instance is short-lived.
    }
}