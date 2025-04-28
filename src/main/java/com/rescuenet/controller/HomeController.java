package com.rescuenet.controller;

import com.rescuenet.model.VehicleModel;
import com.rescuenet.service.VehicleService;
import com.rescuenet.util.CookiesUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map; // Import Map

@WebServlet(asyncSupported = true, urlPatterns = { "/home"}) // Specific mapping
public class HomeController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final VehicleService vehicleService;

    public HomeController() {
        this.vehicleService = new VehicleService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("HomeController: doGet called for path /home");

        // --- Role Check ---
        String role = CookiesUtil.getCookie(request, "role") != null ? CookiesUtil.getCookie(request, "role").getValue() : null;
        if ("admin".equals(role)) {
            System.out.println("HomeController: Admin user detected, redirecting to /admin");
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }
        // --- End Role Check ---

        // --- Check DB Connection ---
        if (vehicleService.isConnectionError()) {
            System.err.println("HomeController: Database connection error. Setting error attribute.");
            // Set error message for the JSP to display
            request.setAttribute("errorMessage", vehicleService.getLastErrorMessage());
            request.getRequestDispatcher("/WEB-INF/pages/home.jsp").forward(request, response);
            return;
        }
        // --- End DB Connection Check ---


        // --- Decide between Search and Categorized View ---
        String searchQuery = request.getParameter("searchQuery");

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            // --- Handle Search ---
            System.out.println("HomeController: Performing search for query: '" + searchQuery + "'");
            List<VehicleModel> searchResults = vehicleService.searchAvailableVehicles(searchQuery);
            request.setAttribute("searchResults", searchResults);
            request.setAttribute("searchQuery", searchQuery); // Pass query back to JSP for display
            System.out.println("HomeController: Found " + searchResults.size() + " search results.");
            // Note: categorizedVehicles will be null/empty in this case

        } else {
            // --- Handle Default Categorized View ---
            System.out.println("HomeController: Fetching categorized available vehicles.");
            Map<String, List<VehicleModel>> categorizedVehicles = vehicleService.getCategorizedAvailableVehicles();
            request.setAttribute("categorizedVehicles", categorizedVehicles);
            System.out.println("HomeController: Found " + categorizedVehicles.size() + " categories.");
             // Note: searchResults and searchQuery will be null/empty in this case
        }
        // --- End Decision Logic ---

        // --- Forward to JSP ---
        System.out.println("HomeController: Forwarding to home.jsp");
        request.getRequestDispatcher("/WEB-INF/pages/home.jsp").forward(request, response);
    }
}