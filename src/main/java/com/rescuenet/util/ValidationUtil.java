package com.rescuenet.util;

import java.util.regex.Pattern;

/**
 * Utility class for validation in the RescueNet application.
 */
public class ValidationUtil {

    /**
     * Validates if a username is between 3 and 50 characters and contains only letters, numbers, or underscores.
     *
     * @param username the username to validate
     * @return true if the username is valid, false otherwise
     */
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    /**
     * Validates if an email address is in a valid format.
     *
     * @param email the email to validate
     * @return true if the email is valid, false otherwise
     */
    public boolean validateEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email != null && Pattern.matches(emailRegex, email);
    }

    /**
     * Validates if a phone number is in international format (e.g., +1234567890).
     *
     * @param phoneNumber the phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public boolean validatePhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }

    /**
     * Validates if two passwords match.
     *
     * @param password        the password
     * @param retypePassword  the retyped password
     * @return true if the passwords match, false otherwise
     */
    public boolean validatePasswordMatch(String password, String retypePassword) {
        return password != null && retypePassword != null && password.equals(retypePassword);
    }

    /**
     * Validates if a password meets complexity requirements.
     * Must be at least 8 characters, with 1 capital letter, 1 number, and 1 symbol.
     *
     * @param password the password to validate
     * @return true if the password is valid, false otherwise
     */
    public boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password != null && password.matches(passwordRegex);
    }
}