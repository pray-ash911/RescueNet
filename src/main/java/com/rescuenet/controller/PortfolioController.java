package com.rescuenet.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rescuenet.model.UserModel;
import com.rescuenet.service.PortfolioService;
import com.rescuenet.util.PasswordUtil;
import com.rescuenet.util.ValidationUtil;
import com.rescuenet.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * @author Prayash Rawal
 */
/**
 * PortfolioController handles user portfolio management in the RescueNet
 * application. It processes requests to view and update user profiles,
 * including profile picture uploads, and forwards to the portfolio JSP page or
 * redirects on session expiration.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/portfolio" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1, maxFileSize = 1024 * 1024 * 2, maxRequestSize = 1024 * 1024 * 5)
public class PortfolioController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ValidationUtil validationUtil;
	private PortfolioService portfolioService;
	private static final String PROFILE_PIC_UPLOAD_DIR = "Uploads" + File.separator + "profile_pictures";
	private static final String PORTFOLIO_JSP_PATH = "/WEB-INF/pages/portfolio.jsp";

	/**
	 * Initializes the PortfolioController with instances of ValidationUtil and
	 * PortfolioService.
	 *
	 * @throws ServletException if an error occurs during initialization
	 */
	@Override
	public void init() throws ServletException {
		this.validationUtil = new ValidationUtil();
		this.portfolioService = new PortfolioService();
	}

	/**
	 * Handles GET requests to display the user portfolio. Fetches user details and
	 * forwards to portfolio.jsp, or redirects to login if session is invalid.
	 *
	 * @param req  the HttpServletRequest object
	 * @param resp the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("PortfolioController: GET request to /portfolio");
		UserModel sessionUser = SessionUtil.getUser(req);
		if (sessionUser == null) {
			resp.sendRedirect(req.getContextPath() + "/login?message="
					+ java.net.URLEncoder.encode("Please log in to view your portfolio.", "UTF-8"));
			return;
		}

		UserModel userDetailsToDisplay = null;
		List<String> errorMessages = new ArrayList<>();

		try {
			userDetailsToDisplay = portfolioService.getUserDetails(sessionUser.getUserId());
			if (userDetailsToDisplay == null && portfolioService.getLastErrorMessage() != null) {
				errorMessages.add(portfolioService.getLastErrorMessage());
			}
		} catch (SQLException e) {
			System.err.println("PortfolioController (doGet): SQLException fetching user details - " + e.getMessage());
			e.printStackTrace();
			errorMessages.add("Could not load profile due to a database error. Please try again later.");
		} catch (Exception e) {
			System.err
					.println("PortfolioController (doGet): Unexpected error fetching user details - " + e.getMessage());
			e.printStackTrace();
			errorMessages.add("An unexpected error occurred while loading your profile.");
		}

		if (userDetailsToDisplay == null) {
			if (errorMessages.isEmpty()) {
				errorMessages
						.add(portfolioService.getLastErrorMessage() != null ? portfolioService.getLastErrorMessage()
								: "Unable to load your profile at this time.");
			}
			req.setAttribute("errorMessagesList", errorMessages);
			System.out.println(
					"PortfolioController: Forwarding to portfolio.jsp with errors (user details null or fetch error).");
			req.getRequestDispatcher(PORTFOLIO_JSP_PATH).forward(req, resp);
			return;
		}

		req.setAttribute("user", userDetailsToDisplay);
		System.out.println(
				"PortfolioController: Forwarding to portfolio.jsp for user: " + userDetailsToDisplay.getUsername());
		req.getRequestDispatcher(PORTFOLIO_JSP_PATH).forward(req, resp);
	}

	/**
	 * Handles POST requests to update the user profile. Validates input, processes
	 * profile picture uploads, updates user details, and forwards to portfolio.jsp.
	 *
	 * @param req  the HttpServletRequest object
	 * @param resp the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("PortfolioController: POST request to /portfolio (update profile)");
		UserModel sessionUser = SessionUtil.getUser(req);

		if (sessionUser == null) {
			resp.sendRedirect(req.getContextPath() + "/login?message="
					+ java.net.URLEncoder.encode("Your session has expired. Please log in again.", "UTF-8"));
			return;
		}
		int userId = sessionUser.getUserId();
		String currentUsernameFromSession = sessionUser.getUsername();
		String currentEmailFromSession = sessionUser.getEmail();
		String currentProfilePicPath = sessionUser.getProfilePicturePath();

		UserModel submittedUserValues = new UserModel();
		submittedUserValues.setUserId(userId);
		submittedUserValues.setUsername(req.getParameter("username"));
		submittedUserValues.setFullName(req.getParameter("full_name"));
		submittedUserValues.setEmail(req.getParameter("email"));
		submittedUserValues.setPhoneNumber(req.getParameter("phone_number"));
		submittedUserValues.setProfilePicturePath(currentProfilePicPath);
		req.setAttribute("user", submittedUserValues);

		String newUsername = req.getParameter("username");
		String fullName = req.getParameter("full_name");
		String email = req.getParameter("email");
		String phoneNumber = req.getParameter("phone_number");
		String newPassword = req.getParameter("new_password");
		String retypeNewPassword = req.getParameter("retype_new_password");
		Part profilePicPart = null;
		try {
			profilePicPart = req.getPart("profile_picture_new");
		} catch (ServletException | IOException e) { // ... error handling for getPart ...
			List<String> errorMessages = new ArrayList<>();
			errorMessages.add("Error processing uploaded file. Please try again.");
			req.setAttribute("errorMessagesList", errorMessages);
			req.getRequestDispatcher(PORTFOLIO_JSP_PATH).forward(req, resp);
			return;
		}

		List<String> errorMessages = new ArrayList<>();
		boolean usernameChanged = newUsername != null && !newUsername.trim().isEmpty()
				&& !newUsername.trim().equalsIgnoreCase(currentUsernameFromSession);
		boolean emailChanged = email != null && !email.trim().isEmpty()
				&& !email.trim().equalsIgnoreCase(currentEmailFromSession);

		if (newUsername == null || newUsername.trim().isEmpty())
			errorMessages.add("Username cannot be empty.");
		else if (!validationUtil.validateUsername(newUsername.trim()))
			errorMessages.add("Invalid new username format.");

		try {
			if (usernameChanged && portfolioService.isUsernameTakenByOtherUser(newUsername.trim(), userId)) {
				errorMessages
						.add(portfolioService.getLastErrorMessage() != null ? portfolioService.getLastErrorMessage()
								: "This username is already taken.");
			}
		} catch (SQLException e) {
			errorMessages.add("Error checking username uniqueness. Please try again.");
			System.err.println("SQL Error checking username: " + e.getMessage());
		}

		if (fullName == null || fullName.trim().isEmpty())
			errorMessages.add("Full name cannot be empty.");
		if (email == null || email.trim().isEmpty())
			errorMessages.add("Email cannot be empty.");
		else if (!validationUtil.validateEmail(email.trim()))
			errorMessages.add("Invalid email format.");

		try {
			if (emailChanged && portfolioService.isEmailTakenByOtherUser(email.trim(), userId)) {
				errorMessages
						.add(portfolioService.getLastErrorMessage() != null ? portfolioService.getLastErrorMessage()
								: "This email is already registered by another user.");
			}
		} catch (SQLException e) {
			errorMessages.add("Error checking email uniqueness. Please try again.");
			System.err.println("SQL Error checking email: " + e.getMessage());
		}

		if (phoneNumber != null && !phoneNumber.trim().isEmpty()
				&& !validationUtil.validatePhoneNumber(phoneNumber.trim()))
			errorMessages.add("Invalid phone number format.");

		String newEncryptedPassword = null;
		if (newPassword != null && !newPassword.trim().isEmpty()) { // ... password validation and encryption ...
			if (!validationUtil.isValidPassword(newPassword)) {
				errorMessages.add("New password does not meet complexity requirements.");
			} else if (!validationUtil.validatePasswordMatch(newPassword, retypeNewPassword)) {
				errorMessages.add("New passwords do not match.");
			} else {
				try {
					String keyForPassword = usernameChanged ? newUsername.trim() : currentUsernameFromSession;
					newEncryptedPassword = PasswordUtil.encrypt(keyForPassword, newPassword);
					if (newEncryptedPassword == null) {
						errorMessages.add("Password encryption failed.");
					}
				} catch (Exception e) {
					errorMessages.add("Error processing new password.");
					e.printStackTrace();
				}
			}
		}

		String newProfilePictureDbPath = null;
		String uniqueFileNameForProfilePic = null;
		if (profilePicPart != null && profilePicPart.getSize() > 0) { // ... file upload logic ...
			String contentType = profilePicPart.getContentType();
			if (!contentType.startsWith("image/")) {
				errorMessages.add("New profile picture must be an image file.");
			} else {
				long maxFileSize = 2 * 1024 * 1024;
				if (profilePicPart.getSize() > maxFileSize) {
					errorMessages.add("New profile picture size cannot exceed 2MB.");
				} else {
					String originalFileName = Paths.get(profilePicPart.getSubmittedFileName()).getFileName().toString();
					originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
					uniqueFileNameForProfilePic = "user_" + userId + "_" + System.currentTimeMillis() + "_"
							+ originalFileName;
					newProfilePictureDbPath = PROFILE_PIC_UPLOAD_DIR.replace(File.separator, "/") + "/"
							+ uniqueFileNameForProfilePic;
					String uploadDirPathAbsolute = getServletContext().getRealPath("") + File.separator
							+ PROFILE_PIC_UPLOAD_DIR;
					File uploadDir = new File(uploadDirPathAbsolute);
					if (!uploadDir.exists()) {
						if (!uploadDir.mkdirs()) {
							errorMessages.add("Server error creating storage for profile picture.");
							newProfilePictureDbPath = null;
						}
					}
					if (uploadDir.exists() && newProfilePictureDbPath != null) {
						Path destinationFilePath = Paths.get(uploadDirPathAbsolute, uniqueFileNameForProfilePic);
						try {
							Files.copy(profilePicPart.getInputStream(), destinationFilePath,
									StandardCopyOption.REPLACE_EXISTING);
							if (currentProfilePicPath != null && !currentProfilePicPath.isEmpty()) {
								cleanupUploadedFile(getServletContext().getRealPath(""), currentProfilePicPath);
							}
						} catch (IOException e) {
							newProfilePictureDbPath = null;
							errorMessages.add("Error saving new profile picture.");
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			newProfilePictureDbPath = currentProfilePicPath;
		}

		if (!errorMessages.isEmpty()) {
			System.err.println("PortfolioController: Validation errors on update: " + errorMessages);
			req.setAttribute("errorMessagesList", errorMessages);
			req.getRequestDispatcher(PORTFOLIO_JSP_PATH).forward(req, resp);
			return;
		}

		boolean updateSuccess = false;
		try {
			updateSuccess = portfolioService.updateUserProfile(userId, newUsername.trim(), fullName.trim(),
					email.trim(), (phoneNumber != null ? phoneNumber.trim() : null), newProfilePictureDbPath,
					newEncryptedPassword);
		} catch (SQLException e) {
			System.err.println("PortfolioController (doPost): SQLException during profile update - " + e.getMessage());
			e.printStackTrace();
			errorMessages.add("A database error occurred while updating your profile. Please try again.");
			if (newProfilePictureDbPath != null && !newProfilePictureDbPath.equals(currentProfilePicPath)
					&& uniqueFileNameForProfilePic != null) {
				cleanupUploadedFile(getServletContext().getRealPath("") + File.separator + PROFILE_PIC_UPLOAD_DIR,
						uniqueFileNameForProfilePic);
			}
		}

		if (updateSuccess) {
			req.setAttribute("success", "Profile updated successfully!");
			UserModel updatedUserInSession = null;
			try {
				updatedUserInSession = portfolioService.getUserDetails(userId);
			} catch (SQLException e) {
				System.err.println("PortfolioController (doPost): SQLException fetching updated user for session - "
						+ e.getMessage());
			}
			if (updatedUserInSession != null) {
				SessionUtil.setUser(req, updatedUserInSession);
			} else {
				sessionUser.setUsername(newUsername.trim());
				sessionUser.setFullName(fullName.trim());
				sessionUser.setEmail(email.trim());
				sessionUser.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);
				if (newProfilePictureDbPath != null)
					sessionUser.setProfilePicturePath(newProfilePictureDbPath);
				SessionUtil.setUser(req, sessionUser);
				System.err.println(
						"PortfolioController: Could not refresh full user in session, updated with form data.");
			}
		} else {
			if (errorMessages.isEmpty()) {
				String serviceError = portfolioService.getLastErrorMessage();
				errorMessages.add(
						serviceError != null ? serviceError : "Failed to update profile due to an unexpected error.");
			}
			req.setAttribute("errorMessagesList", errorMessages);
		}

		UserModel finalUserToDisplay = null;
		try {
			finalUserToDisplay = portfolioService.getUserDetails(userId);
		} catch (SQLException e) {
			System.err.println("Error fetching final user details: " + e.getMessage());
		}

		req.setAttribute("user", finalUserToDisplay != null ? finalUserToDisplay : sessionUser);
		req.getRequestDispatcher(PORTFOLIO_JSP_PATH).forward(req, resp);
	}

	/**
	 * Cleans up an uploaded file from the server filesystem.
	 *
	 * @param appRootPath      the application root path
	 * @param relativeFilePath the relative file path to delete
	 */
	private void cleanupUploadedFile(String appRootPath, String relativeFilePath) {
		// ... (keep this method as is) ...
	}
}