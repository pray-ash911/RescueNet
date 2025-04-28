package com.rescuenet.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.UserModel;
import com.rescuenet.service.LoginService;
import com.rescuenet.util.CookiesUtil;
import com.rescuenet.util.RedirectionUtil;
import com.rescuenet.util.SessionUtil;
import com.rescuenet.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * LoginController handles login requests and processes user authentication.
 * It validates credentials, sets session attributes, and redirects based on user role.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/login" })
public class LoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ValidationUtil validationUtil;
    private RedirectionUtil redirectionUtil;
    private LoginService loginService;

    /**
     * Initializes the servlet with required utilities and services.
     */
    @Override
    public void init() throws ServletException {
        this.validationUtil = new ValidationUtil();
        this.redirectionUtil = new RedirectionUtil();
        this.loginService = new LoginService();
    }

    /**
     * Handles GET requests by displaying the login page.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("WEB-INF/pages/login.jsp").forward(req, resp);
    }

    /**
     * Handles POST requests for user login.
     * Validates credentials, sets session and cookie, and redirects based on role.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (!validationUtil.validateUsername(username) || password == null || password.trim().isEmpty()) {
            redirectionUtil.redirect("error", "Please fill all the fields correctly!", "WEB-INF/pages/login.jsp", req, resp);
            return;
        }

        UserModel userModel = new UserModel(username, password);
        Boolean loginStatus = loginService.loginUser(userModel);

        if (loginStatus != null && loginStatus) {
            // Update last_login
            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                try {
                    conn = DbConfig.getDbConnection();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String updateSql = "UPDATE users SET last_login = NOW() WHERE username = ?";
                stmt = conn.prepareStatement(updateSql);
                stmt.setString(1, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                redirectionUtil.redirect("error", "Database error: " + e.getMessage(), "WEB-INF/pages/login.jsp", req, resp);
                return;
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Set session attributes
            SessionUtil.setAttribute(req, "userId", String.valueOf(userModel.getUserId()));
            SessionUtil.setAttribute(req, "username", username);
            SessionUtil.setAttribute(req, "user", userModel); // Add UserModel to session

            System.out.println("LoginController: Setting user in session - " + userModel.getFullName());
            System.out.println("LoginController: User roleId - " + userModel.getRoleId());

            // Set role cookie and redirect
            if (userModel.getRoleId() == 2) { // Admin
                CookiesUtil.addCookie(resp, "role", "admin", 5 * 30);
                System.out.println("LoginController: Redirecting to /admin for admin user");
                resp.sendRedirect(req.getContextPath() + "/admin");
            } else { // User
                CookiesUtil.addCookie(resp, "role", "user", 5 * 30);
                System.out.println("LoginController: Redirecting to /home for regular user");
                resp.sendRedirect(req.getContextPath() + "/home");
            }
        } else {
            handleLoginFailure(req, resp, loginStatus);
        }
    }

    /**
     * Handles login failures by setting attributes and forwarding to the login page.
     *
     * @param req         the HttpServletRequest object
     * @param resp        the HttpServletResponse object
     * @param loginStatus the Boolean indicating the login status
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    private void handleLoginFailure(HttpServletRequest req, HttpServletResponse resp, Boolean loginStatus)
            throws ServletException, IOException {
        String errorMessage;
        if (loginStatus == null) {
            errorMessage = "Our server is under maintenance. Please try again later!";
        } else {
            errorMessage = "Username and password is mismatch!";
        }
        redirectionUtil.redirect("error", "Username and password is mismatch!", "WEB-INF/pages/login.jsp", req, resp);
    }
}