package com.rescuenet.controller;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;

import com.rescuenet.model.ReservationModel;
import com.rescuenet.model.UserModel;
import com.rescuenet.model.VehicleModel;
import com.rescuenet.service.ReservationService;
import com.rescuenet.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Prayash Rawal
 */
/**
 * ReservationsController handles reservation management in the RescueNet
 * application. It processes requests to view, create, update, and delete
 * reservations for admin users, forwarding to the reservations JSP page or
 * redirecting based on session and role checks.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/reservations", "/reservations/create", "/reservations/update",
		"/reservations/delete" })
public class ReservationsController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ReservationService reservationService;
	private static final String RESERVATIONS_JSP_PATH = "/WEB-INF/pages/reservations.jsp";

	/**
	 * Initializes the ReservationsController with an instance of
	 * ReservationService.
	 *
	 * @throws ServletException if an error occurs during initialization
	 */
	@Override
	public void init() throws ServletException {
		this.reservationService = new ReservationService();
	}

	/**
	 * Handles GET requests for reservation management. Loads reservation data and
	 * forwards to reservations.jsp, or redirects if unauthorized.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getServletPath();
		System.out.println("ReservationsController: doGet called for path - " + path);

		UserModel sessionUser = SessionUtil.getUser(request);
		if (sessionUser == null || sessionUser.getRoleId() != 2) {
			response.sendRedirect(request.getContextPath()
					+ (sessionUser == null ? "/login?message=" + java.net.URLEncoder.encode("Please log in.", "UTF-8")
							: "/home?error=" + java.net.URLEncoder.encode("Access Denied.", "UTF-8")));
			return;
		}

		String actionError = null;

		try {
			if (path.equals("/reservations")) {
				if (!loadCommonData(request)) {
					actionError = reservationService.getLastErrorMessage() != null
							? reservationService.getLastErrorMessage()
							: "Error loading reservation data.";
				}
			} else if (path.equals("/reservations/update")) {
				if (!loadCommonData(request)) {
					actionError = reservationService.getLastErrorMessage() != null
							? reservationService.getLastErrorMessage()
							: "Error loading data for edit form.";
				}
				if (actionError == null) {
					int reservationId;
					try {
						reservationId = Integer.parseInt(request.getParameter("id"));
						ReservationModel reservation = reservationService.getReservationById(reservationId);
						if (reservation == null) {
							actionError = reservationService.getLastErrorMessage() != null
									? reservationService.getLastErrorMessage()
									: "Reservation not found for ID: " + reservationId;
						} else {
							request.setAttribute("reservation", reservation);
						}
					} catch (NumberFormatException | NullPointerException e) {
						actionError = "Invalid or missing reservation ID for update.";
					}
				}
			} else {
				actionError = "Invalid page requested.";
			}
		} catch (SQLException e) {
			System.err.println("ReservationsController (doGet): SQLException occurred - " + e.getMessage());
			e.printStackTrace();
			actionError = "A database error occurred. Please try again later.";
		}

		if (actionError != null) {
			handleError(actionError, request, response);
		} else {
			request.getRequestDispatcher(RESERVATIONS_JSP_PATH).forward(request, response);
		}
	}

	/**
	 * Loads common data (reservations, vehicles, users) for the reservations JSP.
	 *
	 * @param request the HttpServletRequest object
	 * @return true if data is loaded successfully, false otherwise
	 * @throws SQLException if a database error occurs
	 */
	private boolean loadCommonData(HttpServletRequest request) throws SQLException {
		try {
			List<ReservationModel> reservations = reservationService.getAllReservationsWithDetails();
			request.setAttribute("reservations", reservations);

			List<VehicleModel> availableVehicles = reservationService.getAvailableVehiclesForDropdown();
			request.setAttribute("availableVehicles", availableVehicles);

			List<UserModel> users = reservationService.getAllUsers();
			request.setAttribute("users", users);
			return true; // Success
		} catch (SQLException e) {
			System.err.println("ReservationsController (loadCommonData): SQLException - " + e.getMessage());
			e.printStackTrace();
			return false; // Indicate failure
		}
	}

	/**
	 * Handles POST requests for creating, updating, or deleting reservations.
	 * Processes form submissions and redirects or forwards based on operation
	 * success.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getServletPath();
		System.out.println("ReservationsController: doPost called for path - " + path);

		UserModel sessionUser = SessionUtil.getUser(request);
		if (sessionUser == null || sessionUser.getRoleId() != 2) {
			response.sendRedirect(request.getContextPath() + "/login?message="
					+ java.net.URLEncoder.encode("Unauthorized action.", "UTF-8"));
			return;
		}

		String redirectPath = request.getContextPath() + "/reservations";
		String successMessage = null;
		String errorMessage = null; // For redirect errors

		try {
			if (path.equals("/reservations/create") || path.equals("/reservations/update")) {
				boolean isUpdate = path.equals("/reservations/update");
				request.setAttribute("submittedVehicleId", request.getParameter("vehicleId"));
				request.setAttribute("submittedUserId", request.getParameter("userId"));
				request.setAttribute("submittedReservationDate", request.getParameter("reservationDate"));
				request.setAttribute("submittedStatus", request.getParameter("status"));
				if (isUpdate)
					request.setAttribute("submittedReservationId", request.getParameter("reservationId"));

				String vehicleIdStr = request.getParameter("vehicleId");
				String userIdStr = request.getParameter("userId");
				String reservationDateStr = request.getParameter("reservationDate");
				String status = request.getParameter("status");

				if (vehicleIdStr == null || vehicleIdStr.trim().isEmpty() || userIdStr == null
						|| userIdStr.trim().isEmpty() || reservationDateStr == null
						|| reservationDateStr.trim().isEmpty() || status == null || status.trim().isEmpty()) {
					handleError("Vehicle, User, Reservation Date, and Status are required!", request, response);
					return;
				}

				int vehicleId;
				int userId;
				LocalDate reservationDate;
				ReservationModel reservation = new ReservationModel();
				try {
					vehicleId = Integer.parseInt(vehicleIdStr);
					userId = Integer.parseInt(userIdStr);
					reservationDate = LocalDate.parse(reservationDateStr);
				} catch (NumberFormatException e) {
					handleError("Invalid Vehicle or User ID format!", request, response);
					return;
				} catch (DateTimeParseException e) {
					handleError("Invalid Reservation Date format. Use YYYY-MM-DD.", request, response);
					return;
				}

				reservation.setVehicleId(vehicleId);
				reservation.setUserId(userId);
				reservation.setReservationDate(reservationDate);
				reservation.setStatus(status);

				boolean operationSuccess = false;

				if (isUpdate) {
					String reservationIdStr = request.getParameter("reservationId");
					if (reservationIdStr == null || reservationIdStr.trim().isEmpty()) {
						handleError("Reservation ID missing for update!", request, response);
						return;
					}
					try {
						reservation.setReservationId(Integer.parseInt(reservationIdStr));
					} catch (NumberFormatException e) {
						handleError("Invalid Reservation ID format for update!", request, response);
						return;
					}
					operationSuccess = reservationService.updateReservation(reservation);
					if (operationSuccess)
						successMessage = "Reservation updated successfully";
					else
						errorMessage = reservationService.getLastErrorMessage() != null
								? reservationService.getLastErrorMessage()
								: "Failed to update reservation.";
				} else { // Create
					operationSuccess = reservationService.createReservation(reservation);
					if (operationSuccess)
						successMessage = "Reservation created successfully";
					else
						errorMessage = reservationService.getLastErrorMessage() != null
								? reservationService.getLastErrorMessage()
								: "Failed to create reservation.";
				}

				if (operationSuccess) {
					response.sendRedirect(
							redirectPath + "?success=" + java.net.URLEncoder.encode(successMessage, "UTF-8"));
				} else {
					handleError(errorMessage, request, response); // Forward with error
				}
				return;

			} else if (path.equals("/reservations/delete")) {
				String reservationIdStr = request.getParameter("reservationId");
				if (reservationIdStr == null || reservationIdStr.trim().isEmpty()) {
					response.sendRedirect(redirectPath + "?error=Invalid+ID+for+delete");
					return;
				}
				int reservationId;
				try {
					reservationId = Integer.parseInt(reservationIdStr);
				} catch (NumberFormatException e) {
					response.sendRedirect(redirectPath + "?error=Invalid+ID+format+for+delete");
					return;
				}

				boolean success = reservationService.deleteReservation(reservationId);
				if (success) {
					response.sendRedirect(redirectPath + "?success=Reservation+deleted+successfully");
				} else {
					errorMessage = reservationService.getLastErrorMessage() != null
							? reservationService.getLastErrorMessage()
							: "Failed to delete reservation.";
					response.sendRedirect(redirectPath + "?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
				}
				return;
			}
			// If path not matched
			response.sendRedirect(redirectPath + "?error=Invalid+Action");

		} catch (SQLException e) {
			System.err.println("ReservationsController (doPost): SQLException occurred - " + e.getMessage());
			e.printStackTrace();
			handleError("A database error occurred: " + e.getMessage(), request, response);
		} catch (Exception e) {
			System.err.println("ReservationsController (doPost): Unexpected error - " + e.getMessage());
			e.printStackTrace();
			handleError("An unexpected error occurred. Please try again.", request, response);
		}
	}

	/**
	 * Handles errors by setting an error message and forwarding to
	 * reservations.jsp.
	 *
	 * @param errorMessage the error message to display
	 * @param request      the HttpServletRequest object
	 * @param response     the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	private void handleError(String errorMessage, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.err.println("ReservationsController Error: " + errorMessage);
		request.setAttribute("error", errorMessage); // JSP checks for ${requestScope.error} or ${param.error}
		try {
			loadCommonData(request); // Attempt to reload data for dropdowns
		} catch (Exception e) {
			System.err.println(
					"ReservationsController (handleError): Failed to reload common data. Error was: " + e.getMessage());
			request.setAttribute("error",
					(request.getAttribute("error") != null ? request.getAttribute("error") + " Additionally, " : "")
							+ "could not fully load form data due to a subsequent error.");
		}
		request.getRequestDispatcher(RESERVATIONS_JSP_PATH).forward(request, response);
	}
}