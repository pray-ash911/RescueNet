package com.rescuenet.controller;

import java.io.IOException;

import com.rescuenet.model.UserModel;
import com.rescuenet.service.RegisterService;
import com.rescuenet.util.PasswordUtil;
import com.rescuenet.util.RedirectionUtil;
import com.rescuenet.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * RegisterController handles user registration requests and processes form submissions.
 * It validates input, registers the user, and redirects to the login page.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/register" })
public class RegisterController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ValidationUtil validationUtil;
    private RegisterService registerService;
    private RedirectionUtil redirectionUtil;

    /**
     * Initializes the servlet with required utilities and services.
     */
    @Override
    public void init() throws ServletException {
        this.validationUtil = new ValidationUtil();
        this.registerService = new RegisterService();
        this.redirectionUtil = new RedirectionUtil();
    }

    /**
     * Handles GET requests by displaying the registration page.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("WEB-INF/pages/register.jsp").forward(req, resp);
    }

    /**
     * Handles POST requests for user registration.
     * Validates input, registers the user, and redirects to the login page.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            UserModel userModel = extractUserModel(req, resp);
            Boolean isAdded = registerService.addUser(userModel);

            if (isAdded == null) {
                redirectionUtil.redirect("error", "An unexpected error occurred. Please try again later!",
                        "WEB-INF/pages/register.jsp", req, resp);
            } else if (isAdded) {
                redirectionUtil.redirect("success", "Your account is successfully created!",
                        "WEB-INF/pages/login.jsp", req, resp);
            } else {
                redirectionUtil.redirect("error", "Could not register your account. Please try again later!",
                        "WEB-INF/pages/register.jsp", req, resp);
            }
        } catch (Exception e) {
            redirectionUtil.redirect("error", "An unexpected error occurred: " + e.getMessage(),
                    "WEB-INF/pages/register.jsp", req, resp);
            e.printStackTrace();
        }
    }

    /**
     * Extracts user details from the request and creates a UserModel object.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @return the UserModel object with user details
     * @throws Exception if validation fails or an error occurs
     */
    private UserModel extractUserModel(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String fullName = req.getParameter("full_name");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String retypePassword = req.getParameter("retype_password");
        String email = req.getParameter("email");
        String phoneNumber = req.getParameter("phone_number");
        int roleId = Integer.parseInt(req.getParameter("role_id"));
        String isActiveStr = req.getParameter("is_active");
        boolean isActive = (isActiveStr != null && isActiveStr.equals("1"));

        // Validation
        if (!validationUtil.validateUsername(username)) {
            throw new Exception("Invalid username! Must be 3-50 characters and contain only letters, numbers, or underscores.");
        }
        if (!validationUtil.isValidPassword(password)) {
            throw new Exception("Password must be at least 8 characters, with 1 capital letter, 1 number, and 1 symbol!");
        }
        if (!validationUtil.validatePasswordMatch(password, retypePassword)) {
            throw new Exception("Passwords do not match!");
        }
        if (email != null && !email.isEmpty() && !validationUtil.validateEmail(email)) {
            throw new Exception("Invalid email format!");
        }
        if (phoneNumber != null && !phoneNumber.isEmpty() && !validationUtil.validatePhoneNumber(phoneNumber)) {
            throw new Exception("Invalid phone number format! Use international format (e.g., +1234567890).");
        }

        // Encrypt password
        String passwordHash = PasswordUtil.encrypt(username, password);
        if (passwordHash == null) {
            throw new Exception("Password encryption failed!");
        }

        return new UserModel(username, passwordHash, roleId, fullName, email, phoneNumber, isActive);
    }
}