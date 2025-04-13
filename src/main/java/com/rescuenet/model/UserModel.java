package com.rescuenet.model;

import java.time.LocalDateTime;

/**
 * UserModel represents a user entity in the RescueNet application.
 * It maps to the USERS table in the database and contains user-related information.
 */
public class UserModel {

    private int userId;
    private String username;
    private String passwordHash;
    private int roleId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private boolean isActive;

    /**
     * Default constructor for UserModel.
     */
    public UserModel() {
    }

    /**
     * Constructor for login purposes.
     * 
     * @param username    the username of the user
     * @param passwordHash the hashed password of the user
     */
    public UserModel(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Constructor for registration purposes.
     * 
     * @param username     the username of the user
     * @param passwordHash the hashed password of the user
     * @param roleId       the role ID of the user
     * @param fullName     the full name of the user
     * @param email        the email address of the user
     * @param phoneNumber  the phone number of the user
     * @param isActive     the active status of the user
     */
    public UserModel(String username, String passwordHash, int roleId, String fullName, String email, String phoneNumber, boolean isActive) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}