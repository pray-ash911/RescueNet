package com.rescuenet.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.UserModel;
import com.rescuenet.model.VehicleModel;
import com.rescuenet.util.PasswordUtil;
import com.rescuenet.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * PortfolioController handles requests to the user portfolio page.
 * Fetches user details and available vehicles to display on portfolio.jsp.
 * Also handles profile updates.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/portfolio" })
public class PortfolioController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ValidationUtil validationUtil;

    @Override
    public void init() throws ServletException {
        this.validationUtil = new ValidationUtil();
    }

    /**
     * Handles GET requests by fetching user details and available vehicles.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userIdStr = (String) req.getSession().getAttribute("userId");
        if (userIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        UserModel user = getUserDetails(userId);
        if (user == null) {
            req.setAttribute("error", "Unable to fetch user details. Please try again later.");
            req.getRequestDispatcher("WEB-INF/pages/login.jsp").forward(req, resp);
            return;
        }


        req.setAttribute("user", user);
        req.getRequestDispatcher("WEB-INF/pages/portfolio.jsp").forward(req, resp);
    }

    /**
     * Handles POST requests to update user profile details.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userIdStr = (String) req.getSession().getAttribute("userId");
        if (userIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Extract updated details from the form
        String fullName = req.getParameter("full_name");
        String email = req.getParameter("email");
        String phoneNumber = req.getParameter("phone_number");
        String password = req.getParameter("password");
        String username = (String) req.getSession().getAttribute("username");

        // Validate inputs
        if (fullName == null || fullName.trim().isEmpty()) {
            req.setAttribute("error", "Full name cannot be empty.");
            reloadPortfolioPage(userId, req, resp);
            return;
        }

        if (email != null && !email.isEmpty() && !validationUtil.validateEmail(email)) {
            req.setAttribute("error", "Invalid email format!");
            reloadPortfolioPage(userId, req, resp);
            return;
        }

        if (phoneNumber != null && !phoneNumber.isEmpty() && !validationUtil.validatePhoneNumber(phoneNumber)) {
            req.setAttribute("error", "Invalid phone number format! Use international format (e.g., +1234567890).");
            reloadPortfolioPage(userId, req, resp);
            return;
        }

        if (password != null && !password.isEmpty() && !validationUtil.isValidPassword(password)) {
            req.setAttribute("error", "Password must be at least 8 characters, with 1 capital letter, 1 number, and 1 symbol!");
            reloadPortfolioPage(userId, req, resp);
            return;
        }

        // Check for duplicate email (excluding the current user)
        if (email != null && !email.isEmpty() && isEmailTaken(email, userId)) {
            req.setAttribute("error", "This email is already registered by another user!");
            reloadPortfolioPage(userId, req, resp);
            return;
        }

        // Update the user in the database
        boolean updateSuccess = updateUser(userId, fullName, email, phoneNumber, password, username);
        if (updateSuccess) {
            req.setAttribute("success", "Profile updated successfully!");
        } else {
            req.setAttribute("error", "Failed to update profile. Please try again later.");
        }

        reloadPortfolioPage(userId, req, resp);
    }

    /**
     * Fetches user details from the database.
     */
    private UserModel getUserDetails(int userId) {
        try (Connection conn = DbConfig.getDbConnection()) {
            String query = "SELECT username, full_name, email, phone_number FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    UserModel user = new UserModel();
                    user.setUserId(userId);
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    return user;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates the user's profile in the database.
     */
    private boolean updateUser(int userId, String fullName, String email, String phoneNumber, String password, String username) {
        try (Connection conn = DbConfig.getDbConnection()) {
            // If password is provided, encrypt it
            String encryptedPassword = null;
            if (password != null && !password.isEmpty()) {
                encryptedPassword = PasswordUtil.encrypt(username, password);
                if (encryptedPassword == null) {
                    return false; // Encryption failed
                }
            }

            // Build the update query dynamically based on whether password is updated
            StringBuilder query = new StringBuilder("UPDATE users SET full_name = ?, email = ?, phone_number = ?");
            if (encryptedPassword != null) {
                query.append(", password_hash = ?");
            }
            query.append(" WHERE user_id = ?");

            try (PreparedStatement stmt = conn.prepareStatement(query.toString())) {
                stmt.setString(1, fullName);
                stmt.setString(2, email != null && !email.isEmpty() ? email : null);
                stmt.setString(3, phoneNumber != null && !phoneNumber.isEmpty() ? phoneNumber : null);
                int paramIndex = 4;
                if (encryptedPassword != null) {
                    stmt.setString(paramIndex++, encryptedPassword);
                }
                stmt.setInt(paramIndex, userId);

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if the email is already taken by another user.
     */
    private boolean isEmailTaken(String email, int currentUserId) {
        try (Connection conn = DbConfig.getDbConnection()) {
            String query = "SELECT user_id FROM users WHERE email = ? AND user_id != ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setInt(2, currentUserId);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reloads the portfolio page with updated data.
     */
    private void reloadPortfolioPage(int userId, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserModel user = getUserDetails(userId);
        req.setAttribute("user", user);
        req.getRequestDispatcher("WEB-INF/pages/portfolio.jsp").forward(req, resp);
    }
}