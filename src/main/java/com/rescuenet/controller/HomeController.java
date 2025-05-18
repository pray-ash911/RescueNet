package com.rescuenet.controller;

import com.rescuenet.model.UserModel;
import com.rescuenet.model.VehicleModel;
import com.rescuenet.service.VehicleService;
import com.rescuenet.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Prayash Rawal
 */
/**
 * HomeController handles requests for the home page in the RescueNet
 * application. It checks user roles, manages database connections, processes
 * search queries, and forwards requests to the home.jsp page with either search
 * results or categorized vehicle data.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/home" })
public class HomeController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private VehicleService vehicleService;

	/**
	 * Initializes the HomeController with a VehicleService instance. Handles
	 * potential SQLException from VehicleService constructor.
	 */
	@Override
	public void init() throws ServletException {
		try {
			this.vehicleService = new VehicleService();
		} catch (Exception e) {
			System.err.println("HomeController: CRITICAL - Failed to initialize VehicleService: " + e.getMessage());
			e.printStackTrace();
			throw new ServletException("HomeController could not initialize VehicleService.", e);
		}
	}

	/**
	 * Handles GET requests for the home page. Checks user role for admin
	 * redirection, verifies database connection, processes search queries or
	 * fetches categorized vehicles, and forwards to home.jsp.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println("HomeController: doGet called for path /home");

		UserModel loggedInUser = SessionUtil.getUser(request);

		// Redirect if user is admin
		if (loggedInUser != null && loggedInUser.getRoleId() == 2) {
			System.out.println("HomeController: Admin user (RoleID: " + loggedInUser.getRoleId()
					+ ") detected, redirecting to /admin");
			response.sendRedirect(request.getContextPath() + "/admin");
			return;
		}
		// If loggedInUser is null, AuthenticationFilter should have already redirected
		// to /login.

		if (vehicleService.isConnectionError()) {
			System.err.println("HomeController: VehicleService reported a database connection error. Last Msg: "
					+ vehicleService.getLastErrorMessage());
			request.setAttribute("errorMessage",
					vehicleService.getLastErrorMessage() != null ? vehicleService.getLastErrorMessage()
							: "Site experiencing technical difficulties. Please try again later.");
			request.getRequestDispatcher("/WEB-INF/pages/home.jsp").forward(request, response);
			return;
		}

		String searchQuery = request.getParameter("searchQuery");
		String pageError = null; // For errors from service calls below

		if (searchQuery != null && !searchQuery.trim().isEmpty()) {
			System.out.println("HomeController: Performing search for query: '" + searchQuery + "'");
			List<VehicleModel> searchResults = vehicleService.searchAvailableVehicles(searchQuery);
			if (vehicleService.getLastErrorMessage() != null) {
				pageError = vehicleService.getLastErrorMessage();
			}
			request.setAttribute("searchResults", searchResults);
			request.setAttribute("searchQuery", searchQuery);
			System.out.println("HomeController: Found " + (searchResults != null ? searchResults.size() : "null list")
					+ " search results.");
		} else {
			System.out.println("HomeController: Fetching categorized available vehicles.");
			Map<String, List<VehicleModel>> categorizedVehicles = vehicleService.getCategorizedAvailableVehicles();
			if (vehicleService.getLastErrorMessage() != null) {
				pageError = vehicleService.getLastErrorMessage();
			}
			request.setAttribute("categorizedVehicles", categorizedVehicles);
			System.out.println("HomeController: Found "
					+ (categorizedVehicles != null ? categorizedVehicles.size() : "null map") + " categories.");
		}

		if (pageError != null) {
			request.setAttribute("errorMessage", pageError);
		}

		System.out.println("HomeController: Forwarding to home.jsp");
		request.getRequestDispatcher("/WEB-INF/pages/home.jsp").forward(request, response);
	}
}