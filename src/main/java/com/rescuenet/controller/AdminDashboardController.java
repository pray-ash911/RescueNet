package com.rescuenet.controller;

import java.io.IOException;

import com.rescuenet.model.UserModel;
import com.rescuenet.service.DashboardService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author Prayash Rawal
 */
/**
 * AdminDashboardController handles requests for the admin dashboard in the
 * RescueNet application. It validates user sessions, fetches dashboard metrics,
 * and forwards requests to the admin dashboard page.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/admin" })
public class AdminDashboardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final DashboardService dashboardService;

	/**
	 * Initializes the AdminDashboardController with a DashboardService instance.
	 */
	public AdminDashboardController() {
		this.dashboardService = new DashboardService();
	}

	/**
	 * Handles GET requests for the admin dashboard. Validates the user session,
	 * retrieves dashboard metrics, and forwards to the admin_dashboard.jsp page.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("AdminDashboardController: doGet method called for /admin");

		HttpSession session = request.getSession(false);
		if (session == null) {
			System.out.println("AdminDashboardController: No session found, redirecting to /login");
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		UserModel user = (UserModel) session.getAttribute("user");
		if (user == null) {
			System.out.println("AdminDashboardController: No user in session, redirecting to /login");
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		System.out.println("AdminDashboardController: User found in session - " + user.getFullName());
		request.setAttribute("user", user);

		// Fetch metrics using the service layer
		int totalVehicles = dashboardService.getTotalVehicles();
		int availableVehicles = dashboardService.getAvailableVehicles();
		int vehiclesInService = dashboardService.getVehiclesInService();

		// Debug output to check fetched values
		System.out.println("Total Vehicles: " + totalVehicles);
		System.out.println("Available Vehicles: " + availableVehicles);
		System.out.println("Vehicles in Service: " + vehiclesInService);

		request.setAttribute("totalVehicles", totalVehicles);
		request.setAttribute("availableVehicles", availableVehicles);
		request.setAttribute("vehiclesInService", vehiclesInService);

		System.out.println("AdminDashboardController: Forwarding to admin_dashboard.jsp");
		request.getRequestDispatcher("WEB-INF/pages/admin_dashboard.jsp").forward(request, response);
	}

	/**
	 * Handles POST requests for the admin dashboard by delegating to doGet.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("AdminDashboardController: doPost method called, delegating to doGet");
		doGet(request, response);
	}
}