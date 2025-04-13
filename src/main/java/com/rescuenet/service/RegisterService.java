package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
            try {
				this.dbConn = DbConfig.getDbConnection();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registers a new user in the database.
     *
     * @param userModel the user details to be registered
     * @return Boolean indicating the success of the operation; null if a connection error occurs
     */
    public Boolean addUser(UserModel userModel) {
        if (dbConn == null) {
            System.err.println("Database connection is not available.");
            return null;
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