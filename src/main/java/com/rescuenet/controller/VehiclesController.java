package com.rescuenet.controller;

import java.io.File; // Import File class
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; // Potentially useful if overwriting/moving
import java.time.LocalDate;
import java.time.format.DateTimeParseException; // Specific exception for date parsing

import com.rescuenet.model.VehicleModel;
import com.rescuenet.service.VehicleService;
import com.rescuenet.util.CookiesUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet(asyncSupported = true, urlPatterns = { "/vehicles", "/vehicles/create" })
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB - files smaller than this are stored in memory
    maxFileSize = 1024 * 1024 * 5,  // 5MB - max size of a single uploaded file
    maxRequestSize = 1024 * 1024 * 10 // 10MB - max size of the entire multipart request
)
public class VehiclesController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final VehicleService vehicleService;
    // Define UPLOAD_DIR relative to the webapp root
    private static final String UPLOAD_DIR = "uploads" + File.separator + "vehicles";

    public VehiclesController() {
        this.vehicleService = new VehicleService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        System.out.println("VehiclesController: doGet called for path - " + path);

        // --- Role Check (Keep as is) ---
        String role = CookiesUtil.getCookie(request, "role") != null ? CookiesUtil.getCookie(request, "role").getValue() : null;
        if (!"admin".equals(role)) {
            System.out.println("VehiclesController: Non-admin user, redirecting to /home");
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        // --- End Role Check ---

        if (path.equals("/vehicles")) {
            if (vehicleService.isConnectionError()) {
                System.out.println("VehiclesController: Database connection error, setting error message");
                // Set error message as request attribute for JSP display
                request.setAttribute("error", vehicleService.getLastErrorMessage());
            }
            System.out.println("VehiclesController: Forwarding to vehicles.jsp");
            // Always forward to the JSP, let JSP handle displaying errors/form
            request.getRequestDispatcher("/WEB-INF/pages/vehicles.jsp").forward(request, response);
        } else {
            System.out.println("VehiclesController: Unhandled GET path - " + path);
            // Redirect back to the main vehicles page with an error
            response.sendRedirect(request.getContextPath() + "/vehicles?error=" + java.net.URLEncoder.encode("Invalid Page Requested", "UTF-8"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        System.out.println("VehiclesController: doPost called for path - " + path);

        // --- Role Check (Keep as is) ---
        String role = CookiesUtil.getCookie(request, "role") != null ? CookiesUtil.getCookie(request, "role").getValue() : null;
        if (!"admin".equals(role)) {
            System.out.println("VehiclesController: Non-admin user, redirecting to /home");
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        // --- End Role Check ---

        if (path.equals("/vehicles/create")) {
            if (vehicleService.isConnectionError()) {
                System.out.println("VehiclesController: Database connection error, redirecting with error message");
                response.sendRedirect(request.getContextPath() + "/vehicles?error=" + java.net.URLEncoder.encode(vehicleService.getLastErrorMessage(), "UTF-8"));
                return;
            }

            // Get parameters
            String serialNumber = request.getParameter("serialNumber");
            String brandName = request.getParameter("brandName");
            String model = request.getParameter("model");
            String type = request.getParameter("type");
            String status = request.getParameter("status");
            String manufacturedDateStr = request.getParameter("manufacturedDate");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            Part filePart = null;
             try {
                filePart = request.getPart("image"); // Get the file part
             } catch (ServletException e) {
                 // Handle cases where the request might not be multipart correctly
                 System.err.println("VehiclesController: Error getting file part - " + e.getMessage());
                 response.sendRedirect(request.getContextPath() + "/vehicles?error=" + java.net.URLEncoder.encode("Error processing form data.", "UTF-8"));
                 return;
             }


            // --- Basic Field Validation ---
            if (serialNumber == null || serialNumber.trim().isEmpty() ||
                brandName == null || brandName.trim().isEmpty() ||
                model == null || model.trim().isEmpty() ||
                type == null || type.trim().isEmpty() ||
                status == null || status.trim().isEmpty()) {
                 // It's better to forward back to the form with errors and retain values
                 request.setAttribute("error", "Required fields (Serial, Brand, Model, Type, Status) cannot be empty.");
                 // Set submitted values back to the request to repopulate the form
                 request.setAttribute("submittedSerialNumber", serialNumber);
                 request.setAttribute("submittedBrandName", brandName);
                 // ... set others ...
                 request.getRequestDispatcher("/WEB-INF/pages/vehicles.jsp").forward(request, response);
                 return;
            }
            // --- End Basic Validation ---


            // --- Process Uploaded File ---
            String imagePath = null; // This will store the RELATIVE path for the database
            String uniqueFileName = null; // Holds the generated unique filename

            if (filePart != null && filePart.getSize() > 0) {
                String contentType = filePart.getContentType();
                if (!contentType.startsWith("image/")) {
                    request.setAttribute("error", "Uploaded file must be an image (e.g., PNG, JPG, GIF).");
                    request.getRequestDispatcher("/WEB-INF/pages/vehicles.jsp").forward(request, response);
                    return;
                }

                String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                // Sanitize filename to remove potentially harmful characters
                originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");

                // Generate unique filename component ONCE
                uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

                // Build the RELATIVE path for the database (use forward slashes for web paths)
                imagePath = UPLOAD_DIR.replace(File.separator, "/") + "/" + uniqueFileName; // Example: "uploads/vehicles/1716000000000_mycar.jpg"

                // Get the ABSOLUTE directory path for saving on the server's filesystem
                String uploadDirPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
                File uploadDir = new File(uploadDirPath);

                // Create the directories if they don't exist
                if (!uploadDir.exists()) {
                    boolean created = uploadDir.mkdirs(); // Create parent directories if needed
                    if (!created) {
                        System.err.println("VehiclesController: Failed to create upload directory: " + uploadDirPath);
                        response.sendRedirect(request.getContextPath() + "/vehicles?error=" + java.net.URLEncoder.encode("Error creating storage directory on server.", "UTF-8"));
                        return; // Stop if directory cannot be created
                    }
                }

                // Build the ABSOLUTE file path for saving
                Path destinationFilePath = Paths.get(uploadDirPath, uniqueFileName);

                try {
                    // Write the file to the destination using the absolute path
                    // Using Files.copy is often more robust than part.write
                     Files.copy(filePart.getInputStream(), destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                    // filePart.write(destinationFilePath.toString()); // Alternative if Files.copy causes issues

                    System.out.println("VehiclesController: File saved successfully to " + destinationFilePath);
                } catch (IOException e) {
                    System.err.println("VehiclesController: Error writing uploaded file to " + destinationFilePath + " - " + e.getMessage());
                    e.printStackTrace();
                    // Don't save the path to DB if file write failed
                    imagePath = null;
                    response.sendRedirect(request.getContextPath() + "/vehicles?error=" + java.net.URLEncoder.encode("Error saving uploaded file: " + e.getMessage(), "UTF-8"));
                    return; // Stop processing if file save fails
                }
            }
            // --- End Process Uploaded File ---


            // --- Parse Optional Fields ---
            LocalDate manufacturedDate = null;
            if (manufacturedDateStr != null && !manufacturedDateStr.trim().isEmpty()) {
                try {
                    manufacturedDate = LocalDate.parse(manufacturedDateStr); // Assumes YYYY-MM-DD format
                } catch (DateTimeParseException e) {
                    request.setAttribute("error", "Invalid manufactured date format. Please use YYYY-MM-DD.");
                    request.getRequestDispatcher("/WEB-INF/pages/vehicles.jsp").forward(request, response);
                    return;
                }
            }

            BigDecimal price = null;
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    price = new BigDecimal(priceStr);
                    if (price.compareTo(BigDecimal.ZERO) < 0) {
                         request.setAttribute("error", "Price cannot be negative.");
                         request.getRequestDispatcher("/WEB-INF/pages/vehicles.jsp").forward(request, response);
                         return;
                    }
                } catch (NumberFormatException e) {
                     request.setAttribute("error", "Invalid price format. Please enter a valid number.");
                     request.getRequestDispatcher("/WEB-INF/pages/vehicles.jsp").forward(request, response);
                     return;
                }
            }
            // --- End Parse Optional Fields ---

            // --- Create VehicleModel and Save ---
            VehicleModel vehicle = new VehicleModel();
            vehicle.setSerialNumber(serialNumber);
            vehicle.setBrandName(brandName);
            vehicle.setModel(model);
            vehicle.setType(type);
            vehicle.setStatus(status);
            vehicle.setManufacturedDate(manufacturedDate);
            vehicle.setImagePath(imagePath); // Set the relative path saved for DB
            vehicle.setDescription(description);
            vehicle.setPrice(price);

            boolean success = vehicleService.addVehicle(vehicle);
            if (success) {
                System.out.println("VehiclesController: Vehicle added successfully. Redirecting...");
                response.sendRedirect(request.getContextPath() + "/vehicles?success=Vehicle+added+successfully");
            } else {
                String errorMessage = vehicleService.getLastErrorMessage() != null ? vehicleService.getLastErrorMessage() : "Failed to add vehicle to database.";
                System.err.println("VehiclesController: Failed to add vehicle - " + errorMessage);
                // If saving failed, try to delete the orphaned uploaded file (best effort)
                if (imagePath != null && uniqueFileName != null) {
                    try {
                        String uploadDirPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
                        Path orphanedFilePath = Paths.get(uploadDirPath, uniqueFileName);
                        Files.deleteIfExists(orphanedFilePath);
                        System.out.println("VehiclesController: Cleaned up orphaned file: " + orphanedFilePath);
                    } catch (IOException e) {
                         System.err.println("VehiclesController: Error deleting orphaned file - " + e.getMessage());
                    }
                }
                response.sendRedirect(request.getContextPath() + "/vehicles?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
            }
            // --- End Create VehicleModel and Save ---

        } else {
            System.out.println("VehiclesController: Unhandled POST path - " + path);
            response.sendRedirect(request.getContextPath() + "/vehicles?error=" + java.net.URLEncoder.encode("Invalid Action Requested", "UTF-8"));
        }
    }
}