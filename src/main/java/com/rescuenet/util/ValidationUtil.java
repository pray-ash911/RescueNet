package com.rescuenet.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Prayash Rawal
 */

/**
 * Utility class for input validation in the RescueNet application.
 * Provides methods to validate user and vehicle-related data, such as usernames, emails, and vehicle attributes.
 */
public class ValidationUtil {

    /**
     * Validates a username to ensure it is 3-50 characters and contains only letters, numbers, or underscores.
     *
     * @param username the username to validate
     * @return true if the username is valid, false otherwise
     */
    public boolean validateUsername(String username) {
        // --- Validate Username ---
        return username != null && username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    /**
     * Validates an email address to ensure it follows a standard format.
     *
     * @param email the email to validate
     * @return true if the email is valid, false otherwise
     */
    public boolean validateEmail(String email) {
        // --- Validate Email ---
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email != null && Pattern.matches(emailRegex, email);
    }

    /**
     * Validates a phone number to ensure it follows an international format (e.g., +1234567890).
     *
     * @param phoneNumber the phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public boolean validatePhoneNumber(String phoneNumber) {
        // --- Validate Phone Number ---
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }

    /**
     * Validates that two password inputs match.
     *
     * @param password       the first password input
     * @param retypePassword the retyped password input
     * @return true if the passwords match, false otherwise
     */
    public boolean validatePasswordMatch(String password, String retypePassword) {
        // --- Validate Password Match ---
        return password != null && retypePassword != null && password.equals(retypePassword);
    }

    /**
     * Validates a password to ensure it meets complexity requirements (8+ characters, 1 capital letter, 1 number, 1 symbol).
     *
     * @param password the password to validate
     * @return true if the password is valid, false otherwise
     */
    public boolean isValidPassword(String password) {
        // --- Validate Password Complexity ---
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password != null && password.matches(passwordRegex);
    }

    /**
     * Validates a vehicle’s serial number to ensure it contains only alphanumeric characters and hyphens (3-50 characters).
     *
     * @param serialNumber the serial number to validate
     * @return true if the serial number is valid, false otherwise
     */
    public boolean validateVehicleSerialNumber(String serialNumber) {
        // --- Validate Serial Number ---
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return false;
        }
        return serialNumber.matches("^[a-zA-Z0-9\\-]{3,50}$");
    }

    /**
     * Validates a vehicle’s brand name or model to ensure it contains letters, numbers, spaces, hyphens, or apostrophes (2-50 characters).
     *
     * @param name the brand name or model to validate
     * @return true if the name is valid, false otherwise
     */
    public boolean validateVehicleNameField(String name) {
        // --- Validate Name Field ---
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-Z0-9\\s\\-']{2,50}$");
    }

    /**
     * Validates a vehicle status against a predefined set of allowed values.
     *
     * @param status the status to validate
     * @return true if the status is valid, false otherwise
     */
    public boolean validateVehicleStatus(String status) {
        // --- Validate Vehicle Status ---
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        Set<String> allowedStatuses = new HashSet<>(Arrays.asList(
                "Available", "Reserved", "Maintenance", "Rented", "Unavailable"
        ));
        return allowedStatuses.contains(status);
    }

    /**
     * Validates a vehicle’s manufactured date to ensure it is a valid date and not in the future.
     *
     * @param dateStr the date string in "yyyy-MM-dd" format
     * @return true if the date is valid or null/empty, false if invalid or in the future
     */
    public boolean validateManufacturedDate(String dateStr) {
        // --- Validate Manufactured Date ---
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return true;
        }
        try {
            LocalDate manufacturedDate = LocalDate.parse(dateStr);
            if (manufacturedDate.isAfter(LocalDate.now())) {
                return false;
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validates a vehicle’s price to ensure it is a non-negative number.
     *
     * @param priceStr the price as a string
     * @return true if the price is valid or null/empty, false otherwise
     */
    public boolean validateVehiclePrice(String priceStr) {
        // --- Validate Vehicle Price ---
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return true;
        }
        try {
            BigDecimal price = new BigDecimal(priceStr);
            return price.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates a text field (e.g., description) to ensure it does not exceed a maximum length.
     *
     * @param text      the text to validate
     * @param maxLength the maximum allowed length
     * @return true if the text is within the length limit or null, false otherwise
     */
    public boolean validateTextMaxLength(String text, int maxLength) {
        // --- Validate Text Length ---
        if (text == null) {
            return true;
        }
        return text.length() <= maxLength;
    }
}