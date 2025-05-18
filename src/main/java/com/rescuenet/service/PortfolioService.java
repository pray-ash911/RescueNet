package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.UserModel;
import com.rescuenet.util.PasswordUtil;

/**
 * @author Prayash Rawal
 */

/**
 * Service class for managing user profile operations in the RescueNet
 * application. Handles retrieval and updating of user details, including
 * username, email, phone number, profile picture, and password, with validation
 * for unique usernames and emails.
 */
public class PortfolioService {

	private String lastErrorMessage;

	/**
	 * Retrieves the last error message encountered during a service operation.
	 *
	 * @return the last error message, or null if no error occurred
	 */
	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

	/**
	 * Retrieves user details from the database for the specified user ID.
	 *
	 * @param userId the ID of the user to retrieve details for
	 * @return a UserModel object containing user details, or null if not found or
	 *         an error occurs
	 * @throws SQLException if a database access error occurs
	 */
	public UserModel getUserDetails(int userId) throws SQLException {
		lastErrorMessage = null;
		UserModel user = null;
		String query = "SELECT user_id, username, password_hash, full_name, email, phone_number, profile_picture_path, role_id, is_active "
				+ "FROM users WHERE user_id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		// --- Establish Database Connection ---
		try {
			conn = DbConfig.getDbConnection();
			if (conn == null) {
				lastErrorMessage = "Failed to connect to database.";
				System.err.println("PortfolioService: " + lastErrorMessage);
				return null;
			}

			// --- Query User Details ---
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, userId);
			rs = stmt.executeQuery();

			// --- Populate UserModel ---
			if (rs.next()) {
				user = new UserModel();
				user.setUserId(rs.getInt("user_id"));
				user.setUsername(rs.getString("username"));
				user.setPasswordHash(rs.getString("password_hash"));
				user.setFullName(rs.getString("full_name"));
				user.setEmail(rs.getString("email"));
				user.setPhoneNumber(rs.getString("phone_number"));
				user.setProfilePicturePath(rs.getString("profile_picture_path"));
				user.setRoleId(rs.getInt("role_id"));
				user.setActive(rs.getBoolean("is_active"));
			} else {
				lastErrorMessage = "User not found for ID: " + userId;
				System.err.println("PortfolioService: " + lastErrorMessage);
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error fetching user details: " + e.getMessage();
			System.err.println("PortfolioService: " + lastErrorMessage);
			throw new SQLException("Failed to fetch user details", e);
		} finally {
			// --- Clean Up Resources ---
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing ResultSet - " + e.getMessage());
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing PreparedStatement - " + e.getMessage());
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing Connection - " + e.getMessage());
			}
		}
		return user;
	}

	/**
	 * Updates the user's profile in the database with the provided fields.
	 * Dynamically constructs the update query based on non-null fields.
	 *
	 * @param userId                the ID of the user to update
	 * @param newUsername           the new username
	 * @param fullName              the updated full name
	 * @param email                 the updated email, or null if unchanged
	 * @param phoneNumber           the updated phone number, or null if unchanged
	 * @param newProfilePicturePath the new profile picture path, or null if
	 *                              unchanged
	 * @param newEncryptedPassword  the new encrypted password, or null if unchanged
	 * @return true if the update was successful, false if an error occurred or no
	 *         changes were made
	 * @throws SQLException if a database access error occurs
	 */
	public boolean updateUserProfile(int userId, String newUsername, String fullName, String email, String phoneNumber,
			String newProfilePicturePath, String newEncryptedPassword) throws SQLException {
		lastErrorMessage = null;
		List<Object> params = new ArrayList<>();
		StringBuilder queryBuilder = new StringBuilder("UPDATE users SET ");
		boolean needsComma = false;

		// --- Build Base Query ---
		queryBuilder.append("username = ?, full_name = ?, email = ?, phone_number = ?");
		params.add(newUsername.trim());
		params.add(fullName.trim());
		params.add(email != null && !email.trim().isEmpty() ? email.trim() : null);
		params.add(phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber.trim() : null);
		needsComma = true;

		// --- Add Profile Picture Update ---
		if (newProfilePicturePath != null) {
			if (needsComma)
				queryBuilder.append(", ");
			queryBuilder.append("profile_picture_path = ?");
			params.add(newProfilePicturePath);
			needsComma = true;
		}

		// --- Add Password Update ---
		if (newEncryptedPassword != null && !newEncryptedPassword.isEmpty()) {
			if (needsComma)
				queryBuilder.append(", ");
			queryBuilder.append("password_hash = ?");
			params.add(newEncryptedPassword);
			needsComma = true;
		}

		// --- Finalize Query ---
		queryBuilder.append(" WHERE user_id = ?");
		params.add(userId);

		String finalQuery = queryBuilder.toString();
		System.out.println("PortfolioService: Executing update for user ID: " + userId + ". Query: " + finalQuery);

		// --- Check for No Changes ---
		if (params.size() == 5 && newProfilePicturePath == null
				&& (newEncryptedPassword == null || newEncryptedPassword.isEmpty())) {
			lastErrorMessage = "No changes to update.";
			System.out.println("PortfolioService: No fields marked for update.");
			return true; // Consider no changes as a successful operation
		}

		// --- Execute Update ---
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DbConfig.getDbConnection();
			if (conn == null) {
				lastErrorMessage = "Failed to connect to database.";
				System.err.println("PortfolioService: " + lastErrorMessage);
				return false;
			}

			stmt = conn.prepareStatement(finalQuery);
			for (int i = 0; i < params.size(); i++) {
				stmt.setObject(i + 1, params.get(i));
			}
			int rowsAffected = stmt.executeUpdate();
			System.out.println("PortfolioService: Rows affected by update: " + rowsAffected);
			return rowsAffected > 0;

		} catch (SQLException | ClassNotFoundException e) {
			// --- Handle Specific Errors ---
			String errorMsgLower = e.getMessage().toLowerCase();
			if (errorMsgLower.contains("duplicate entry")) {
				if (errorMsgLower.contains("for key 'users.username'")
						|| errorMsgLower.contains("for key 'username'")) {
					lastErrorMessage = "The username '" + newUsername + "' is already taken.";
				} else if (errorMsgLower.contains("for key 'users.email'")
						|| errorMsgLower.contains("for key 'email'")) {
					lastErrorMessage = "The email '" + email + "' is already registered.";
				}
			} else {
				lastErrorMessage = "Error updating user profile: " + e.getMessage();
			}
			System.err.println("PortfolioService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			// --- Clean Up Resources ---
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing PreparedStatement - " + e.getMessage());
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing Connection - " + e.getMessage());
			}
		}
	}

	/**
	 * Checks if the specified username is taken by another user.
	 *
	 * @param username      the username to check
	 * @param currentUserId the ID of the current user to exclude from the check
	 * @return true if the username is taken by another user or an error occurs,
	 *         false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean isUsernameTakenByOtherUser(String username, int currentUserId) throws SQLException {
		lastErrorMessage = null;
		String query = "SELECT user_id FROM users WHERE username = ? AND user_id != ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		// --- Check Username Availability ---
		try {
			conn = DbConfig.getDbConnection();
			if (conn == null) {
				lastErrorMessage = "Failed to connect to database.";
				System.err.println("PortfolioService: " + lastErrorMessage);
				return true; // Conservative approach: assume taken on connection failure
			}
			stmt = conn.prepareStatement(query);
			stmt.setString(1, username);
			stmt.setInt(2, currentUserId);
			rs = stmt.executeQuery();
			boolean isTaken = rs.next();
			if (isTaken) {
				lastErrorMessage = "Username '" + username + "' is already taken.";
				System.out.println("PortfolioService: " + lastErrorMessage);
			}
			return isTaken;
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error checking username availability: " + e.getMessage();
			System.err.println("PortfolioService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			// --- Clean Up Resources ---
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing ResultSet - " + e.getMessage());
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing PreparedStatement - " + e.getMessage());
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing Connection - " + e.getMessage());
			}
		}
	}

	/**
	 * Checks if the specified email is taken by another user.
	 *
	 * @param email         the email to check
	 * @param currentUserId the ID of the current user to exclude from the check
	 * @return true if the email is taken by another user or an error occurs, false
	 *         otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean isEmailTakenByOtherUser(String email, int currentUserId) throws SQLException {
		lastErrorMessage = null;
		String query = "SELECT user_id FROM users WHERE email = ? AND user_id != ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		// --- Check Email Availability ---
		try {
			conn = DbConfig.getDbConnection();
			if (conn == null) {
				lastErrorMessage = "Failed to connect to database.";
				System.err.println("PortfolioService: " + lastErrorMessage);
				return true; // Conservative approach: assume taken on connection failure
			}
			stmt = conn.prepareStatement(query);
			stmt.setString(1, email);
			stmt.setInt(2, currentUserId);
			rs = stmt.executeQuery();
			boolean isTaken = rs.next();
			if (isTaken) {
				lastErrorMessage = "Email '" + email + "' is already registered.";
				System.out.println("PortfolioService: " + lastErrorMessage);
			}
			return isTaken;
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error checking email availability: " + e.getMessage();
			System.err.println("PortfolioService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			// --- Clean Up Resources ---
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing ResultSet - " + e.getMessage());
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing PreparedStatement - " + e.getMessage());
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				System.err.println("PortfolioService: Error closing Connection - " + e.getMessage());
			}
		}
	}
}