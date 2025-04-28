package com.rescuenet.controller;

import java.io.IOException;
import java.util.List;
import java.time.LocalDate; // Import LocalDate
import java.time.format.DateTimeParseException; // Import exception

import com.rescuenet.model.ReservationModel;
import com.rescuenet.model.UserModel;
import com.rescuenet.model.VehicleModel; // Import VehicleModel
import com.rescuenet.service.ReservationService;
import com.rescuenet.util.CookiesUtil;
import com.rescuenet.util.RedirectionUtil; // Keep if used, otherwise remove
import com.rescuenet.util.ValidationUtil; // Keep if used, otherwise remove


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Update URL patterns if needed, keep base /reservations
@WebServlet(asyncSupported = true, urlPatterns = { "/reservations", "/reservations/create", "/reservations/update", "/reservations/delete" })
public class ReservationsController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ReservationService reservationService;
    
    public ReservationsController() {
        this.reservationService = new ReservationService();
        // this.validationUtil = new ValidationUtil();
        // this.redirectionUtil = new RedirectionUtil();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        System.out.println("ReservationsController: doGet called for path - " + path);

        // --- Role Check (Keep as is) ---
        String role = CookiesUtil.getCookie(request, "role") != null ? CookiesUtil.getCookie(request, "role").getValue() : null;
        if (!"admin".equals(role)) {
            System.out.println("ReservationsController: Non-admin user, redirecting to /home");
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        // --- End Role Check ---


        if (path.equals("/reservations")) {
            // Load data for the main listing page and the form dropdowns
            loadCommonData(request);
            System.out.println("ReservationsController: Forwarding to reservations.jsp (main view)");
            request.getRequestDispatcher("/WEB-INF/pages/reservations.jsp").forward(request, response);

        } else if (path.equals("/reservations/update")) {
             // Load data needed for the edit form
            loadCommonData(request); // Load dropdown data

            int reservationId;
            try {
                reservationId = Integer.parseInt(request.getParameter("id"));
            } catch (NumberFormatException | NullPointerException e) {
                 System.err.println("ReservationsController: Invalid or missing reservation ID for update.");
                 response.sendRedirect(request.getContextPath() + "/reservations?error=Invalid+reservation+ID");
                return;
            }

            // Fetch the specific reservation to edit
            ReservationModel reservation = reservationService.getReservationById(reservationId);

            if (reservation != null) {
                request.setAttribute("reservation", reservation); // Set the reservation object for the form
                System.out.println("ReservationsController: Forwarding to reservations.jsp (edit mode)");
                request.getRequestDispatcher("/WEB-INF/pages/reservations.jsp").forward(request, response);
            } else {
                 System.err.println("ReservationsController: Reservation not found for ID: " + reservationId);
                response.sendRedirect(request.getContextPath() + "/reservations?error=Reservation+not+found");
            }
        } else {
            System.out.println("ReservationsController: Unhandled GET path - " + path);
            response.sendRedirect(request.getContextPath() + "/reservations?error=Invalid+path");
        }
    }

    // Helper method to load data needed by the JSP (main list and form)
    private void loadCommonData(HttpServletRequest request) {
        List<ReservationModel> reservations = reservationService.getAllReservationsWithDetails();
        request.setAttribute("reservations", reservations);

        List<VehicleModel> availableVehicles = reservationService.getAvailableVehiclesForDropdown();
        List<UserModel> users = reservationService.getAllUsers();
        request.setAttribute("availableVehicles", availableVehicles);
        request.setAttribute("users", users);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        System.out.println("ReservationsController: doPost called for path - " + path);

        // --- Role Check (Keep as is) ---
        String role = CookiesUtil.getCookie(request, "role") != null ? CookiesUtil.getCookie(request, "role").getValue() : null;
        if (!"admin".equals(role)) {
            System.out.println("ReservationsController: Non-admin user, redirecting to /home");
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        // --- End Role Check ---


        if (path.equals("/reservations/create") || path.equals("/reservations/update")) {
            // Handle both Create and Update
            boolean isUpdate = path.equals("/reservations/update");

            // Get common parameters
            String vehicleIdStr = request.getParameter("vehicleId");
            String userIdStr = request.getParameter("userId");
            String reservationDateStr = request.getParameter("reservationDate");
            String status = request.getParameter("status");

            // --- Basic Validation ---
            if (vehicleIdStr == null || vehicleIdStr.trim().isEmpty() ||
                userIdStr == null || userIdStr.trim().isEmpty() ||
                reservationDateStr == null || reservationDateStr.trim().isEmpty() ||
                status == null || status.trim().isEmpty()) {
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
                reservationDate = LocalDate.parse(reservationDateStr); // Assumes YYYY-MM-DD
            } catch (NumberFormatException e) {
                handleError("Invalid Vehicle or User ID format!", request, response);
                return;
            } catch (DateTimeParseException e) {
                handleError("Invalid Reservation Date format. Use YYYY-MM-DD.", request, response);
                return;
            }

            // Populate model
            reservation.setVehicleId(vehicleId);
            reservation.setUserId(userId);
            reservation.setReservationDate(reservationDate);
            reservation.setStatus(status);

            boolean success = false;
            String successMessage = "";
            String failureMessage = "";

            if (isUpdate) {
                String reservationIdStr = request.getParameter("reservationId");
                 if (reservationIdStr == null || reservationIdStr.trim().isEmpty()) {
                    handleError("Reservation ID is missing for update!", request, response);
                    return;
                 }
                 try {
                     reservation.setReservationId(Integer.parseInt(reservationIdStr));
                 } catch (NumberFormatException e) {
                     handleError("Invalid Reservation ID format for update!", request, response);
                     return;
                 }
                 System.out.println("ReservationsController: Attempting to update reservation ID: " + reservation.getReservationId());
                 success = reservationService.updateReservation(reservation);
                 successMessage = "Reservation updated successfully";
                 failureMessage = "Failed to update reservation. Check logs and vehicle availability.";

            } else { // Create
                System.out.println("ReservationsController: Attempting to create new reservation.");
                 success = reservationService.createReservation(reservation);
                 successMessage = "Reservation created successfully";
                 failureMessage = "Failed to create reservation. Check logs and vehicle availability.";
            }

            // Redirect based on success/failure
            if (success) {
                response.sendRedirect(request.getContextPath() + "/reservations?success=" + java.net.URLEncoder.encode(successMessage, "UTF-8"));
            } else {
                String errorMessage = reservationService.getLastErrorMessage() != null ? reservationService.getLastErrorMessage() : failureMessage;
                // Redirect back to the main page with error
                 response.sendRedirect(request.getContextPath() + "/reservations?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
                 // Alternatively, forward back to the form with error and retain data (more complex)
                 // handleError(errorMessage, request, response);
            }

        } else if (path.equals("/reservations/delete")) {
            String reservationIdStr = request.getParameter("reservationId"); // Get ID from form

            if (reservationIdStr == null || reservationIdStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/reservations?error=Invalid+Reservation+ID+for+delete");
                return;
            }

            int reservationId;
            try {
                reservationId = Integer.parseInt(reservationIdStr);
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/reservations?error=Invalid+Reservation+ID+format+for+delete");
                return;
            }

            System.out.println("ReservationsController: Deleting reservation with ID - " + reservationId);
            boolean success = reservationService.deleteReservation(reservationId);
            if (success) {
                response.sendRedirect(request.getContextPath() + "/reservations?success=Reservation+deleted+successfully");
            } else {
                String errorMessage = reservationService.getLastErrorMessage() != null ? reservationService.getLastErrorMessage() : "Failed to delete reservation";
                response.sendRedirect(request.getContextPath() + "/reservations?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
            }
        } else {
            System.out.println("ReservationsController: Unhandled POST path - " + path);
            response.sendRedirect(request.getContextPath() + "/reservations?error=Invalid+Action");
        }
    }

     // Helper method to handle errors - forwards back to the JSP
     private void handleError(String errorMessage, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         System.err.println("ReservationsController Error: " + errorMessage);
         request.setAttribute("error", errorMessage);
         // Reload data needed for the form dropdowns before forwarding
         loadCommonData(request);
         request.getRequestDispatcher("/WEB-INF/pages/reservations.jsp").forward(request, response);
     }

    // Removed date validation methods as basic parsing is done inline

}