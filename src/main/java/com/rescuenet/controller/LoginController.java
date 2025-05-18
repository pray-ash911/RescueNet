package com.rescuenet.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.UserModel;
import com.rescuenet.service.LoginService;
import com.rescuenet.util.CookiesUtil;
import com.rescuenet.util.SessionUtil;
import com.rescuenet.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Prayash Rawal
 */
/**
 * LoginController handles user login requests in the RescueNet application. It
 * processes login form submissions, validates credentials, updates session and
 * cookies, and redirects users based on their roles or forwards to the login
 * page on failure.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/login" })
public class LoginController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ValidationUtil validationUtil;
	private LoginService loginService;
	private static final String LOGIN_JSP_PATH = "/WEB-INF/pages/login.jsp";

	/**
	 * Initializes the LoginController with instances of ValidationUtil and
	 * LoginService.
	 *
	 * @throws ServletException if an error occurs during initialization
	 */
	@Override
	public void init() throws ServletException {
		this.validationUtil = new ValidationUtil();
		this.loginService = new LoginService();
	}

	/**
	 * Handles GET requests for the login page. Clears previous session messages,
	 * sets redirect messages, and forwards to login.jsp.
	 *
	 * @param req  the HttpServletRequest object
	 * @param resp the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		SessionUtil.removeAttribute(req, "successMessage"); // Clear previous success messages from other operations
		if (req.getParameter("success") != null) {
			req.setAttribute("successMessageFromRedirect", req.getParameter("success"));
		}
		if (req.getParameter("error") != null) {
			req.setAttribute("errorMessageFromRedirect", req.getParameter("error"));
		}
		if (req.getParameter("message") != null) {
			req.setAttribute("infoMessage", req.getParameter("message"));
		}
		req.getRequestDispatcher(LOGIN_JSP_PATH).forward(req, resp);
	}

	/**
	 * Handles POST requests for user login. Validates input, authenticates users,
	 * updates session and cookies, and redirects based on role.
	 *
	 * @param req  the HttpServletRequest object
	 * @param resp the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String usernameFromForm = req.getParameter("username");
		String passwordFromForm = req.getParameter("password");

		req.setAttribute("username", usernameFromForm); // For repopulating form

		if (usernameFromForm == null || usernameFromForm.trim().isEmpty() || passwordFromForm == null
				|| passwordFromForm.isEmpty()) {
			req.setAttribute("error", "Username and Password cannot be empty.");
			req.getRequestDispatcher(LOGIN_JSP_PATH).forward(req, resp);
			return;
		}
		if (!validationUtil.validateUsername(usernameFromForm)) {
			req.setAttribute("error", "Invalid username format.");
			req.getRequestDispatcher(LOGIN_JSP_PATH).forward(req, resp);
			return;
		}

		// Check if LoginService had an issue during its initialization
		if (loginService.isConnectionError()) {
			System.err.println("LoginController: LoginService reported a connection error from its constructor.");
			String serviceInitError = loginService.getLastErrorMessage();
			req.setAttribute("error",
					serviceInitError != null ? serviceInitError : "Login service initialization failed.");
			req.getRequestDispatcher(LOGIN_JSP_PATH).forward(req, resp);
			return;
		}

		UserModel tempLoginUser = new UserModel(usernameFromForm, passwordFromForm);
		Boolean loginStatus = loginService.loginUser(tempLoginUser); // LoginService modifies tempLoginUser

		if (loginStatus != null && loginStatus == true) {
			UserModel fullyPopulatedUser = fetchFullUserDetails(tempLoginUser.getUserId());

			if (fullyPopulatedUser == null) {
				System.err.println(
						"LoginController: CRITICAL - LoginService reported success but could not fetch full user details for ID: "
								+ tempLoginUser.getUserId());
				req.setAttribute("error",
						"Login successful but failed to retrieve your profile. Please try again or contact support.");
				req.getRequestDispatcher(LOGIN_JSP_PATH).forward(req, resp);
				return;
			}

			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				conn = DbConfig.getDbConnection();
				String updateSql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
				stmt = conn.prepareStatement(updateSql);
				stmt.setInt(1, fullyPopulatedUser.getUserId());
				stmt.executeUpdate();
				System.out
						.println("LoginController: Updated last_login for user ID: " + fullyPopulatedUser.getUserId());
			} catch (SQLException | ClassNotFoundException e) {
				System.err.println("LoginController: Failed to update last_login - " + e.getMessage());
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException e) {
				}
				try {
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
				}
			}

			SessionUtil.setUser(req, fullyPopulatedUser);
			System.out.println("LoginController: Full user object set in session. ID: " + fullyPopulatedUser.getUserId()
					+ ", Username: " + fullyPopulatedUser.getUsername() + ", RoleID: "
					+ fullyPopulatedUser.getRoleId());

			req.getSession().setAttribute("successMessage", "Successfully logged in!"); // For display on next page

			CookiesUtil.addCookie(resp, "username", fullyPopulatedUser.getUsername(), 30 * 24 * 60 * 60); // 30 days
			System.out.println("LoginController: Username cookie set for: " + fullyPopulatedUser.getUsername());

			CookiesUtil.deleteCookie(resp, "role");

			if (fullyPopulatedUser.getRoleId() == 2) { // Admin
				System.out.println("LoginController: Redirecting admin to /admin");
				resp.sendRedirect(req.getContextPath() + "/admin");
			} else { // User
				System.out.println("LoginController: Redirecting user to /home");
				resp.sendRedirect(req.getContextPath() + "/home");
			}
		} else {
			handleLoginFailure(req, resp, loginStatus, loginService.isConnectionError(),
					loginService.getLastErrorMessage());
		}
	}

	/**
	 * Fetches complete user details (excluding password hash) for session storage.
	 *
	 * @param userId the ID of the user to fetch
	 * @return the fully populated UserModel, or null if not found or an error
	 *         occurs
	 */
	private UserModel fetchFullUserDetails(int userId) {
		UserModel user = null;
		String query = "SELECT user_id, username, full_name, email, phone_number, profile_picture_path, role_id, is_active "
				+ "FROM users WHERE user_id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DbConfig.getDbConnection();
			if (conn == null) {
				System.err.println("LoginController (fetchFullUserDetails): DB Connection is null.");
				return null;
			}
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, userId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				user = new UserModel();
				user.setUserId(rs.getInt("user_id"));
				user.setUsername(rs.getString("username"));
				user.setFullName(rs.getString("full_name"));
				user.setEmail(rs.getString("email"));
				user.setPhoneNumber(rs.getString("phone_number"));
				user.setProfilePicturePath(rs.getString("profile_picture_path"));
				user.setRoleId(rs.getInt("role_id"));
				user.setActive(rs.getBoolean("is_active"));
			} else {
				System.out.println("LoginController (fetchFullUserDetails): No user found for ID: " + userId);
			}
		} catch (SQLException | ClassNotFoundException e) {
			System.err.println("LoginController (fetchFullUserDetails): Error fetching full user details for ID "
					+ userId + " - " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return user;
	}

	/**
	 * Handles login failure by setting an error message and forwarding to
	 * login.jsp.
	 *
	 * @param req                the HttpServletRequest object
	 * @param resp               the HttpServletResponse object
	 * @param loginStatusFlag    the login status from LoginService
	 * @param wasConnectionError indicates if a connection error occurred
	 * @param serviceMsg         the error message from LoginService
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	private void handleLoginFailure(HttpServletRequest req, HttpServletResponse resp, Boolean loginStatusFlag,
			boolean wasConnectionError, String serviceMsg) throws ServletException, IOException {
		String displayErrorMessage;
		if (wasConnectionError) {
			displayErrorMessage = serviceMsg != null && !serviceMsg.isEmpty() ? serviceMsg
					: "Login service unavailable. Please try again later.";
			System.err.println(
					"LoginController: Login failed due to service connection error. Service Msg: " + serviceMsg);
		} else if (loginStatusFlag == null) {
			displayErrorMessage = serviceMsg != null && !serviceMsg.isEmpty() ? serviceMsg
					: "An error occurred during login. Please try again.";
			System.err.println("LoginController: Login failed due to service error (loginStatus is null). Service Msg: "
					+ serviceMsg);
		} else {
			displayErrorMessage = serviceMsg != null && !serviceMsg.isEmpty() ? serviceMsg
					: "Invalid username or password.";
			System.err.println(
					"LoginController: Login failed - invalid credentials/user not found. Service Msg: " + serviceMsg);
		}
		req.setAttribute("error", displayErrorMessage);
		req.getRequestDispatcher(LOGIN_JSP_PATH).forward(req, resp);
	}
}