package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.UserModel;
import com.rescuenet.util.PasswordUtil;

/**
 * Service class for handling login operations in the RescueNet application.
 * Connects to the database, verifies user credentials, and returns login status.
 */
public class LoginService {

    private Connection dbConn;
    private boolean isConnectionError = false;

    /**
     * Constructor initializes the database connection.
     * Sets the connection error flag if the connection fails.
     */
    public LoginService() {
        try {
            try {
				dbConn = DbConfig.getDbConnection();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (SQLException e) {
            e.printStackTrace();
            isConnectionError = true;
        }
    }

    /**
     * Validates the user credentials against the database records.
     *
     * @param userModel the UserModel object containing user credentials
     * @return true if the user credentials are valid, false otherwise; null if a connection error occurs
     */
    public Boolean loginUser(UserModel userModel) {
        if (isConnectionError) {
            System.out.println("Connection Error!");
            return null;
        }

        String query = "SELECT user_id, username, password_hash, role_id FROM users WHERE username = ? AND is_active = 1";
        try (PreparedStatement stmt = dbConn.prepareStatement(query)) {
            stmt.setString(1, userModel.getUsername());
            ResultSet result = stmt.executeQuery();

            if (result.next()) {
                userModel.setUserId(result.getInt("user_id")); // Set userId
                userModel.setRoleId(result.getInt("role_id")); // Set roleId
                return validatePassword(result, userModel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return false;
    }

    /**
     * Validates the password retrieved from the database.
     *
     * @param result     the ResultSet containing the username and password from the database
     * @param userModel  the UserModel object containing user credentials
     * @return true if the passwords match, false otherwise
     * @throws SQLException if a database access error occurs
     */
    private boolean validatePassword(ResultSet result, UserModel userModel) throws SQLException {
        String dbUsername = result.getString("username");
        String dbPassword = result.getString("password_hash");

        return dbUsername.equals(userModel.getUsername())
                && PasswordUtil.decrypt(dbPassword, dbUsername).equals(userModel.getPasswordHash());
    }
}