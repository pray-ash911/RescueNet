package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.UserModel;

/**
 * Service class for handling user registration in the RescueNet application.
 * Manages database interactions for user registration.
 */
public class RegisterService {

    private Connection dbConn;

    /**
     * Constructor initializes the database connection.
     */
    public RegisterService() {
        try {
            this.dbConn = DbConfig.getDbConnection();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the username already exists in the database.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean isUsernameExists(String username) {
        if (dbConn == null || username == null || username.isEmpty()) {
            return false;
        }

        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = dbConn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the email already exists in the database.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    public boolean isEmailExists(String email) {
        if (dbConn == null || email == null || email.isEmpty()) {
            return false;
        }

        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = dbConn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Registers a new user in the database.
     *
     * @param userModel the user details to be registered
     * @return Boolean indicating the success of the operation:
     *         - true: user registered successfully
     *         - false: username or email already exists
     *         - null: database connection error or other failure
     */
    public Boolean addUser(UserModel userModel) {
        if (dbConn == null) {
            System.err.println("Database connection is not available.");
            return null;
        }

        if (isUsernameExists(userModel.getUsername())) {
            return false;
        }

        if (isEmailExists(userModel.getEmail())) {
            return false;
        }

        String insertQuery = "INSERT INTO users (username, password_hash, role_id, full_name, email, phone_number, created_at, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)";
        try {
            PreparedStatement insertStmt = dbConn.prepareStatement(insertQuery);
            insertStmt.setString(1, userModel.getUsername());
            insertStmt.setString(2, userModel.getPasswordHash());
            insertStmt.setInt(3, userModel.getRoleId());
            insertStmt.setString(4, userModel.getFullName());
            insertStmt.setString(5, userModel.getEmail());
            insertStmt.setString(6, userModel.getPhoneNumber());
            insertStmt.setInt(7, userModel.isActive() ? 1 : 0);

            return insertStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}