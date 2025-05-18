package com.rescuenet.model;

import java.time.LocalDateTime;

/**
 * @author Prayash Rawal
 */
/**
 * UserModel represents a user entity in the RescueNet application. It stores
 * user details such as username, password hash, role, contact information,
 * profile picture path, and account status for authentication and profile
 * management.
 */
public class UserModel {

	private int userId;
	private String username;
	private String passwordHash;
	private int roleId;
	private String fullName;
	private String email;
	private String phoneNumber;
	private String profilePicturePath;
	private LocalDateTime lastLogin;
	private LocalDateTime createdAt;
	private boolean isActive;

	/**
	 * Default constructor for UserModel.
	 */
	public UserModel() {
	}

	/**
	 * Constructor for login purposes with username and password hash.
	 *
	 * @param username     the username of the user
	 * @param passwordHash the hashed password of the user
	 */
	public UserModel(String username, String passwordHash) {
		this.username = username;
		this.passwordHash = passwordHash;
	}

	/**
	 * Constructor for user registration with all required fields.
	 *
	 * @param username           the username of the user
	 * @param passwordHash       the hashed password of the user
	 * @param roleId             the role ID of the user
	 * @param fullName           the full name of the user
	 * @param email              the email address of the user
	 * @param phoneNumber        the phone number of the user
	 * @param profilePicturePath the path to the user's profile picture
	 * @param isActive           the active status of the user
	 */
	public UserModel(String username, String passwordHash, int roleId, String fullName, String email,
			String phoneNumber, String profilePicturePath, boolean isActive) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.roleId = roleId;
		this.fullName = fullName;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.profilePicturePath = profilePicturePath;
		this.isActive = isActive;
	}

	/**
	 * Gets the user ID.
	 *
	 * @return the user ID
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Sets the user ID.
	 *
	 * @param userId the user ID to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the password hash.
	 *
	 * @return the password hash
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * Sets the password hash.
	 *
	 * @param passwordHash the password hash to set
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	/**
	 * Gets the role ID.
	 *
	 * @return the role ID
	 */
	public int getRoleId() {
		return roleId;
	}

	/**
	 * Sets the role ID.
	 *
	 * @param roleId the role ID to set
	 */
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	/**
	 * Gets the full name.
	 *
	 * @return the full name
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Sets the full name.
	 *
	 * @param fullName the full name to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * Gets the email address.
	 *
	 * @return the email address
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email address.
	 *
	 * @param email the email address to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the phone number.
	 *
	 * @return the phone number
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * Sets the phone number.
	 *
	 * @param phoneNumber the phone number to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * Gets the profile picture path.
	 *
	 * @return the profile picture path
	 */
	public String getProfilePicturePath() {
		return profilePicturePath;
	}

	/**
	 * Sets the profile picture path.
	 *
	 * @param profilePicturePath the profile picture path to set
	 */
	public void setProfilePicturePath(String profilePicturePath) {
		this.profilePicturePath = profilePicturePath;
	}

	/**
	 * Gets the last login time.
	 *
	 * @return the last login time
	 */
	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	/**
	 * Sets the last login time.
	 *
	 * @param lastLogin the last login time to set
	 */
	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * Gets the account creation time.
	 *
	 * @return the account creation time
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Sets the account creation time.
	 *
	 * @param createdAt the account creation time to set
	 */
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Checks if the user account is active.
	 *
	 * @return true if the account is active, false otherwise
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Sets the active status of the user account.
	 *
	 * @param isActive the active status to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}