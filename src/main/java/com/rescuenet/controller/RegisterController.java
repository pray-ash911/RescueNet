package com.rescuenet.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.rescuenet.model.UserModel;
import com.rescuenet.service.RegisterService;
import com.rescuenet.util.PasswordUtil;
import com.rescuenet.util.RedirectionUtil;
import com.rescuenet.util.ValidationUtil;

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
 * RegisterController handles user registration requests for the RescueNet
 * application. It processes registration form submissions, validates input,
 * handles profile picture uploads, and redirects to the login page on success
 * or forwards back to the registration page on failure.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/register" })
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1, // 1MB
		maxFileSize = 1024 * 1024 * 2, // 2MB (Max profile pic size)
		maxRequestSize = 1024 * 1024 * 5 // Max total request size
)
public class RegisterController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ValidationUtil validationUtil;
	private RegisterService registerService;
	private RedirectionUtil redirectionUtil;
	private static final String PROFILE_PIC_UPLOAD_DIR = "Uploads" + File.separator + "profile_pictures";
	private static final String REGISTER_JSP_PATH = "/WEB-INF/pages/register.jsp";

	/**
	 * Initializes the RegisterController with instances of ValidationUtil,
	 * RegisterService, and RedirectionUtil.
	 *
	 * @throws ServletException if an error occurs during initialization
	 */
	@Override
	public void init() throws ServletException {
		this.validationUtil = new ValidationUtil();
		this.registerService = new RegisterService();
		this.redirectionUtil = new RedirectionUtil();
	}

	/**
	 * Handles GET requests for the registration page. Forwards the request to the
	 * register.jsp page for rendering.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("RegisterController: GET request to /register");
		req.getRequestDispatcher(REGISTER_JSP_PATH).forward(req, resp);
	}

	/**
	 * Handles POST requests for user registration. Validates form input, processes
	 * profile picture uploads, encrypts passwords, registers the user, and
	 * redirects to login on success or forwards to register.jsp on failure.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("RegisterController: POST request to /register");

		req.setAttribute("submittedFullName", req.getParameter("full_name"));
		req.setAttribute("submittedUsername", req.getParameter("username"));
		req.setAttribute("submittedEmail", req.getParameter("email"));
		req.setAttribute("submittedPhoneNumber", req.getParameter("phone_number"));
		req.setAttribute("submittedRoleId", req.getParameter("role_id"));
		req.setAttribute("submittedIsActive", "true".equals(req.getParameter("is_active")));

		String fullName = req.getParameter("full_name");
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		String retypePassword = req.getParameter("retype_password");
		String email = req.getParameter("email");
		String phoneNumber = req.getParameter("phone_number");
		String roleIdStr = req.getParameter("role_id");
		String isActiveParam = req.getParameter("is_active");
		Part profilePicPart = null;

		try {
			profilePicPart = req.getPart("profile_picture");
		} catch (ServletException | IOException e) {
			System.err.println("RegisterController: Error getting profile_picture part - " + e.getMessage());
			List<String> errorList = new ArrayList<>();
			errorList.add("Error processing uploaded file. Please ensure it's a valid image and not too large.");
			req.setAttribute("errorMessagesList", errorList);
			req.getRequestDispatcher(REGISTER_JSP_PATH).forward(req, resp);
			return;
		}

		if (registerService.isConnectionError()) {
			System.err.println("RegisterController: RegisterService reported a connection error from its constructor.");
			List<String> errorList = new ArrayList<>();
			errorList.add(registerService.getLastErrorMessage() != null ? registerService.getLastErrorMessage()
					: "Registration service initialization failed. Please try again later.");
			req.setAttribute("errorMessagesList", errorList);
			req.getRequestDispatcher(REGISTER_JSP_PATH).forward(req, resp);
			return;
		}

		List<String> errorList = new ArrayList<>();
		if (fullName == null || fullName.trim().isEmpty())
			errorList.add("Full Name is required.");
		if (!validationUtil.validateUsername(username))
			errorList.add("Invalid username (3-50 chars, letters, numbers, _).");
		if (!validationUtil.isValidPassword(password))
			errorList.add("Password: 8+ chars, 1 upper, 1 lower, 1 digit, 1 symbol (@$!%*?&_-).");
		if (!validationUtil.validatePasswordMatch(password, retypePassword))
			errorList.add("Passwords do not match.");
		if (email == null || email.trim().isEmpty() || !validationUtil.validateEmail(email))
			errorList.add("Invalid or missing email format.");
		if (phoneNumber != null && !phoneNumber.trim().isEmpty() && !validationUtil.validatePhoneNumber(phoneNumber))
			errorList.add("Invalid phone number format.");

		int roleId = 0;
		if (roleIdStr == null || roleIdStr.trim().isEmpty()) {
			errorList.add("Role is required.");
		} else {
			try {
				roleId = Integer.parseInt(roleIdStr);
				if (roleId != 1 && roleId != 2) {
					errorList.add("Invalid role selected.");
					roleId = 0; // Mark as invalid to prevent proceeding with bad role
				}
			} catch (NumberFormatException e) {
				errorList.add("Invalid role format.");
				roleId = 0; // Mark as invalid
			}
		}
		boolean isActive = "true".equals(isActiveParam);

		String profilePictureDbPath = null;
		String uniqueFileNameForProfilePic = null;
		if (profilePicPart != null && profilePicPart.getSize() > 0) {
			String contentType = profilePicPart.getContentType();
			if (!contentType.startsWith("image/")) {
				errorList.add("Profile picture must be an image file (JPG, PNG, GIF, WEBP).");
			} else {
				long maxFileSize = 2 * 1024 * 1024; // Matches @MultipartConfig
				if (profilePicPart.getSize() > maxFileSize) {
					errorList.add("Profile picture size cannot exceed 2MB.");
				} else {
					String originalFileName = Paths.get(profilePicPart.getSubmittedFileName()).getFileName().toString();
					originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
					uniqueFileNameForProfilePic = "profile_" + System.currentTimeMillis() + "_" + originalFileName;
					profilePictureDbPath = PROFILE_PIC_UPLOAD_DIR.replace(File.separator, "/") + "/"
							+ uniqueFileNameForProfilePic;
					String uploadDirPathAbsolute = getServletContext().getRealPath("") + File.separator
							+ PROFILE_PIC_UPLOAD_DIR;
					File uploadDir = new File(uploadDirPathAbsolute);
					System.out.println(
							"RegisterController: Attempting to use/create upload directory: " + uploadDirPathAbsolute);
					if (!uploadDir.exists()) {
						if (!uploadDir.mkdirs()) {
							System.err.println(
									"RegisterController: Failed to create upload directory: " + uploadDirPathAbsolute);
							errorList.add("Server error: Could not create storage for profile picture.");
							profilePictureDbPath = null;
						} else {
							System.out
									.println("RegisterController: Upload directory created: " + uploadDirPathAbsolute);
						}
					}
					if (uploadDir.exists() && profilePictureDbPath != null) {
						Path destinationFilePath = Paths.get(uploadDirPathAbsolute, uniqueFileNameForProfilePic);
						try {
							Files.copy(profilePicPart.getInputStream(), destinationFilePath,
									StandardCopyOption.REPLACE_EXISTING);
							System.out.println("RegisterController: Profile picture saved to " + destinationFilePath);
						} catch (IOException e) {
							profilePictureDbPath = null; // Failed to save
							errorList.add("Error saving profile picture file: " + e.getMessage());
							System.err.println(
									"RegisterController: IOException saving profile picture: " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		}

		if (!errorList.isEmpty()) {
			System.err.println("RegisterController: Validation errors found: " + errorList);
			req.setAttribute("errorMessagesList", errorList);
			if (uniqueFileNameForProfilePic != null && (profilePictureDbPath == null || !errorList.isEmpty())) {
				cleanupUploadedFile(getServletContext().getRealPath("") + File.separator + PROFILE_PIC_UPLOAD_DIR,
						uniqueFileNameForProfilePic);
			}
			req.getRequestDispatcher(REGISTER_JSP_PATH).forward(req, resp);
			return;
		}

		String encryptedPassword;
		try {
			System.out.println("RegisterController: Encrypting password for user: " + username);
			encryptedPassword = PasswordUtil.encrypt(username, password);
			if (encryptedPassword == null) {
				System.err.println("RegisterController: PasswordUtil.encrypt returned null for user: " + username);
				throw new Exception("Password encryption returned null.");
			}
		} catch (Exception e) {
			cleanupUploadedFile(getServletContext().getRealPath("") + File.separator + PROFILE_PIC_UPLOAD_DIR,
					uniqueFileNameForProfilePic);
			System.err.println("RegisterController: Password encryption critical error - " + e.getMessage());
			e.printStackTrace();
			errorList.add("Registration processing error (P). Please try again.");
			req.setAttribute("errorMessagesList", errorList);
			req.getRequestDispatcher(REGISTER_JSP_PATH).forward(req, resp);
			return;
		}

		UserModel userModel = new UserModel(username.trim(), encryptedPassword, roleId, fullName.trim(), email.trim(),
				(phoneNumber != null ? phoneNumber.trim() : null), profilePictureDbPath, isActive);
		System.out.println("RegisterController: UserModel created. Username: " + userModel.getUsername() + ", PicPath: "
				+ userModel.getProfilePicturePath() + ", RoleID: " + userModel.getRoleId() + ", IsActive: "
				+ userModel.isActive());

		Boolean isAdded = registerService.addUser(userModel);
		System.out.println("RegisterController: Result of addUser from service: " + isAdded);

		if (isAdded == null) {
			cleanupUploadedFile(getServletContext().getRealPath("") + File.separator + PROFILE_PIC_UPLOAD_DIR,
					uniqueFileNameForProfilePic);
			String dbError = registerService.getLastErrorMessage();
			System.err.println("RegisterController: addUser service returned null. Error: "
					+ (dbError != null ? dbError : "Unknown DB error from service."));
			errorList.add(dbError != null ? dbError : "Database error occurred during registration. Please try again.");
			req.setAttribute("errorMessagesList", errorList);
			req.getRequestDispatcher(REGISTER_JSP_PATH).forward(req, resp);
		} else if (isAdded) {
			System.out.println("RegisterController: User registered successfully. Redirecting to login.");
			// For success, use RedirectionUtil to send to login page with a success param
			redirectionUtil.redirectToPage("success", "Your account is successfully created! Please log in.", "/login",
					req, resp);
		} else {
			cleanupUploadedFile(getServletContext().getRealPath("") + File.separator + PROFILE_PIC_UPLOAD_DIR,
					uniqueFileNameForProfilePic);
			String specificError = registerService.getLastErrorMessage();
			System.err.println("RegisterController: addUser service returned false. Error: "
					+ (specificError != null ? specificError : "Username/email exists."));
			errorList.add(specificError != null ? specificError
					: "Registration failed. Username or email may already be in use.");
			req.setAttribute("errorMessagesList", errorList);
			req.getRequestDispatcher(REGISTER_JSP_PATH).forward(req, resp);
		}
	}

	/**
	 * Cleans up an uploaded file from the server filesystem if registration fails.
	 *
	 * @param uploadDirPath the directory path where the file is stored
	 * @param fileName      the name of the file to delete
	 */
	private void cleanupUploadedFile(String uploadDirPath, String fileName) {
		if (fileName == null || uploadDirPath == null)
			return;
		try {
			Path orphanedFilePath = Paths.get(uploadDirPath, fileName);
			if (Files.exists(orphanedFilePath)) {
				Files.delete(orphanedFilePath);
				System.out.println("RegisterController: Cleaned up orphaned file: " + orphanedFilePath);
			} else {
				System.out.println("RegisterController: Orphaned file (" + fileName + ") not found for cleanup in "
						+ uploadDirPath);
			}
		} catch (IOException e) {
			System.err
					.println("RegisterController: Error deleting orphaned file (" + fileName + ") - " + e.getMessage());
		}
	}
}