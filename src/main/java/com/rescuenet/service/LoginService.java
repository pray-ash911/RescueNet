package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.UserModel;
import com.rescuenet.util.PasswordUtil;

/**
 * @author Prayash Rawal
 */

/**
 * Service class for handling login operations in the RescueNet application.
 * Connects to the database, verifies user credentials, and returns login
 * status.
 */
public class LoginService {

	private Connection dbConn; // This instance variable can lead to issues in a multi-threaded environment.
	// Consider getting a fresh connection in each method or using a connection
	// pool.
	private boolean isConnectionError = false;
	private String lastErrorMessage = null;

	/**
	 * Initializes the database connection for login operations. Sets the connection
	 * error flag and message if the connection fails. This constructor does NOT
	 * throw SQLException.
	 */
	public LoginService() {
		try {
			this.dbConn = DbConfig.getDbConnection(); // This might throw ClassNotFoundException or SQLException
			if (this.dbConn == null) { // Defensive check if DbConfig returns null
				this.isConnectionError = true;
				this.lastErrorMessage = "Database connection could not be established (DbConfig returned null).";
				System.err.println("LoginService Constructor: " + this.lastErrorMessage);
			} else {
				System.out.println("LoginService Constructor: Database connection appears to be established.");
			}
		} catch (ClassNotFoundException e) {
			this.isConnectionError = true;
			this.lastErrorMessage = "LoginService: Database driver not found - " + e.getMessage();
			System.err.println(this.lastErrorMessage);
			e.printStackTrace();
			this.dbConn = null; // Ensure dbConn is null on this critical error
		} catch (SQLException e) {
			this.isConnectionError = true;
			this.lastErrorMessage = "LoginService: Failed to initialize database connection - " + e.getMessage();
			System.err.println(this.lastErrorMessage);
			e.printStackTrace();
			this.dbConn = null; // Ensure dbConn is null
		} catch (Exception e) { // Catch any other unexpected runtime errors
			this.isConnectionError = true;
			this.lastErrorMessage = "LoginService: Unexpected error during initialization - " + e.getMessage();
			System.err.println(this.lastErrorMessage);
			e.printStackTrace();
			this.dbConn = null;
		}
	}

	// Getter for connection error flag (for controller to check)
	public boolean isConnectionError() {
		return isConnectionError;
	}

	// Getter for last error message (for controller to use)
	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

	/**
	 * Validates user credentials against the database. Populates the passed-in
	 * UserModel with user ID and role ID if credentials are valid.
	 *
	 * @param userModelFromController the UserModel object containing username and
	 *                                (plain) password
	 * @return true if credentials are valid, false if invalid (user not
	 *         found/password mismatch), null if a database/system error occurs
	 *         during the login attempt.
	 */
	public Boolean loginUser(UserModel userModelFromController) {
		this.lastErrorMessage = null; // Reset for this specific login attempt

		// Check flag set by constructor OR if dbConn is null for any other reason
		if (this.isConnectionError || this.dbConn == null) {
			if (this.lastErrorMessage == null) { // If constructor error wasn't set but dbConn is null
				this.lastErrorMessage = "Database connection not available for login operation.";
			}
			System.err.println("LoginService (loginUser): " + this.lastErrorMessage);
			return null; // Indicate system-level error
		}

		String query = "SELECT user_id, username, password_hash, role_id FROM users WHERE username = ? AND is_active = 1";
		System.out.println(
				"LoginService (loginUser): Attempting to login user: " + userModelFromController.getUsername());

		// Using try-with-resources for PreparedStatement and ResultSet for auto-closing
		try (PreparedStatement stmt = this.dbConn.prepareStatement(query)) {
			stmt.setString(1, userModelFromController.getUsername());
			try (ResultSet result = stmt.executeQuery()) {
				if (result.next()) {
					System.out.println(
							"LoginService (loginUser): User found in DB: " + userModelFromController.getUsername());
					// Populate the passed-in userModel with id and roleId from DB
					userModelFromController.setUserId(result.getInt("user_id"));
					userModelFromController.setRoleId(result.getInt("role_id"));
					// Perform password validation
					return validatePassword(result, userModelFromController);
				} else {
					this.lastErrorMessage = "No active user found for username: "
							+ userModelFromController.getUsername();
					System.err.println("LoginService (loginUser): " + this.lastErrorMessage);
					return false; // User not found or inactive
				}
			}
		} catch (SQLException e) {
			// Don't override isConnectionError if it was already true from constructor.
			// However, a new SQLException during query could also indicate a connection
			// issue.
			// For simplicity here, we'll set a specific message.
			this.lastErrorMessage = "Database error during login query for username: "
					+ userModelFromController.getUsername() + " - " + e.getMessage();
			System.err.println("LoginService (loginUser): " + this.lastErrorMessage);
			e.printStackTrace();
			return null; // Indicate system error during this login attempt
		}
		// Note on dbConn: If this.dbConn is a class member initialized in the
		// constructor,
		// it should NOT be closed here if the LoginService instance is long-lived or
		// reused.
		// It should be closed when the LoginService itself is destroyed or no longer
		// needed.
		// For web applications, it's often better to get and close a connection within
		// each method
		// or use a connection pool managed by DbConfig.
	}

	/**
	 * Validates the provided password against the stored password hash.
	 *
	 * @param resultFromDb      the ResultSet containing database user data
	 * @param userModelFromForm the UserModel object with user-provided credentials
	 * @return true if the password matches, false otherwise
	 * @throws SQLException if a database access error occurs (from
	 *                      result.getString)
	 */
	private boolean validatePassword(ResultSet resultFromDb, UserModel userModelFromForm) throws SQLException {
		String dbUsername = resultFromDb.getString("username");
		String dbPasswordHash = resultFromDb.getString("password_hash");
		String plainPasswordFromForm = userModelFromForm.getPasswordHash(); // This is the plain password

		System.out.println("LoginService (validatePassword): Validating password for DB user: " + dbUsername);

		String decryptedDbPassword = null;
		try {
			decryptedDbPassword = PasswordUtil.decrypt(dbPasswordHash, dbUsername);
			if (decryptedDbPassword == null) {
				this.lastErrorMessage = "Password decryption failed for user: " + dbUsername
						+ ". Stored hash or key (username) might be incorrect.";
				System.err.println("LoginService (validatePassword): " + this.lastErrorMessage);
				return false; // Decryption itself failed
			}
		} catch (Exception e) {
			// Catching a broader Exception in case PasswordUtil.decrypt throws something
			// unexpected
			this.lastErrorMessage = "An unexpected error occurred during password decryption for user: " + dbUsername
					+ " - " + e.getMessage();
			System.err.println("LoginService (validatePassword): " + this.lastErrorMessage);
			e.printStackTrace();
			return false;
		}

		// Username match is implicitly handled by the SQL query using `WHERE username =
		// ?`.
		// An extra check like `dbUsername.equals(userModelFromForm.getUsername())` is
		// redundant here.
		boolean passwordsMatch = decryptedDbPassword.equals(plainPasswordFromForm);
		if (passwordsMatch) {
			System.out.println("LoginService (validatePassword): Password match successful for " + dbUsername);
			return true;
		} else {
			this.lastErrorMessage = "Password mismatch for user: " + dbUsername;
			System.err.println("LoginService (validatePassword): " + this.lastErrorMessage);
			return false;
		}
	}
}