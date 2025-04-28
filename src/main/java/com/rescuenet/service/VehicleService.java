package com.rescuenet.service;

import com.rescuenet.model.VehicleModel;
import com.rescuenet.config.DbConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap; // Use LinkedHashMap to maintain insertion order of categories
import java.util.List;
import java.util.Map; // Import Map
import java.math.BigDecimal;
import java.time.LocalDate;

public class VehicleService {
    // Keep existing fields: dbConn, lastErrorMessage, isConnectionError
    private final Connection dbConn;
    private String lastErrorMessage;
    private boolean isConnectionError;


    public VehicleService() {
        Connection tempConn = null;
        try {
            tempConn = DbConfig.getDbConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            isConnectionError = true;
            lastErrorMessage = "Failed to establish database connection: " + e.getMessage();
        }
        this.dbConn = tempConn;
    }

    /**
     * Adds a new vehicle record to the database.
     * @param vehicle The VehicleModel object containing vehicle details.
     * @return true if the vehicle was added successfully, false otherwise.
     */
    public boolean addVehicle(VehicleModel vehicle) {
        // --- Keep existing addVehicle method ---
        if (isConnectionError || dbConn == null) {
            lastErrorMessage = "Cannot add vehicle due to database connection error";
            return false;
        }

       
        String  sql = "INSERT INTO Vehicles (serial_number, brand_name, model, type, status, manufactured_date, image_path, description, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getSerialNumber());
            pstmt.setString(2, vehicle.getBrandName());
            pstmt.setString(3, vehicle.getModel());
            pstmt.setString(4, vehicle.getType());
            pstmt.setString(5, vehicle.getStatus());
            pstmt.setObject(6, vehicle.getManufacturedDate() != null ? Date.valueOf(vehicle.getManufacturedDate()) : null);
            pstmt.setString(7, vehicle.getImagePath());
            pstmt.setString(8, vehicle.getDescription());
            pstmt.setBigDecimal(9, vehicle.getPrice());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            lastErrorMessage = "Error adding vehicle: " + e.getMessage(); // More specific error
            System.err.println("VehicleService: Error adding vehicle - " + lastErrorMessage); // Log as error
            e.printStackTrace(); // Print stack trace for debugging
            return false;
        }
        // --- End existing addVehicle method ---
    }

    /**
     * Fetches all vehicles with status 'Available' and groups them by type.
     * Used for the default categorized view on the homepage.
     * @return A Map where the key is the vehicle type (String) and the value is a List of VehicleModel objects of that type.
     */
    public Map<String, List<VehicleModel>> getCategorizedAvailableVehicles() {
        // Use LinkedHashMap to preserve the order in which categories appear (based on first encounter)
        Map<String, List<VehicleModel>> categorizedVehicles = new LinkedHashMap<>();

        if (isConnectionError || dbConn == null) {
            lastErrorMessage = "Cannot fetch vehicles due to database connection error";
            System.err.println("VehicleService: " + lastErrorMessage);
            return categorizedVehicles; // Return empty map
        }

        // SQL to select all necessary details for available vehicles, ordered by type for potential grouping optimization
        String sql = "SELECT vehicle_id, serial_number, brand_name, model, type, status, manufactured_date, image_path, description, price " +
                     "FROM Vehicles WHERE status = ? ORDER BY type, brand_name, model";

        try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
            pstmt.setString(1, "Available"); // Filter by status 'Available'
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                VehicleModel vehicle = mapResultSetToVehicleModel(rs); // Use helper method
                String vehicleType = vehicle.getType();

                // Add vehicle to the list for its type, creating the list if it's the first of its type
                categorizedVehicles.computeIfAbsent(vehicleType, k -> new ArrayList<>()).add(vehicle);
            }
        } catch (SQLException e) {
            lastErrorMessage = "Error fetching categorized vehicles: " + e.getMessage();
            System.err.println("VehicleService: Error fetching categorized vehicles - " + lastErrorMessage);
            e.printStackTrace();
        } finally {
             // Close connection in a real app or ensure DbConfig handles pooling/closing
             // For this example, assume connection is managed elsewhere or closed after multiple calls
        }
        return categorizedVehicles;
    }


    /**
     * Searches for vehicles based on a query string across multiple fields.
     * Only searches within vehicles having status 'Available'.
     * @param searchQuery The user's search term(s).
     * @return A List of VehicleModel objects matching the search query.
     */
    public List<VehicleModel> searchAvailableVehicles(String searchQuery) {
         List<VehicleModel> vehicles = new ArrayList<>();
         if (isConnectionError || dbConn == null) {
            lastErrorMessage = "Cannot search vehicles due to database connection error";
             System.err.println("VehicleService: " + lastErrorMessage);
            return vehicles; // Return empty list
        }
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return vehicles; // Return empty list if search query is empty
        }

        // Prepare query for searching across multiple fields
        String queryParam = "%" + searchQuery.toLowerCase() + "%"; // Add wildcards and make lowercase for case-insensitive search
        String sql = "SELECT vehicle_id, serial_number, brand_name, model, type, status, manufactured_date, image_path, description, price " +
                     "FROM Vehicles " +
                     "WHERE status = ? AND (" + // Must be available AND match search
                     "LOWER(brand_name) LIKE ? OR " +
                     "LOWER(model) LIKE ? OR " +
                     "LOWER(type) LIKE ? OR " +
                     "LOWER(serial_number) LIKE ? OR " +
                     "LOWER(description) LIKE ?" +
                     ") ORDER BY type, brand_name, model"; // Example ordering

        try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
            pstmt.setString(1, "Available"); // Parameter for status
            pstmt.setString(2, queryParam);   // Parameter for brand_name LIKE
            pstmt.setString(3, queryParam);   // Parameter for model LIKE
            pstmt.setString(4, queryParam);   // Parameter for type LIKE
            pstmt.setString(5, queryParam);   // Parameter for serial_number LIKE
            pstmt.setString(6, queryParam);   // Parameter for description LIKE

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                 vehicles.add(mapResultSetToVehicleModel(rs)); // Use helper method
            }
        } catch (SQLException e) {
            lastErrorMessage = "Error searching vehicles: " + e.getMessage();
            System.err.println("VehicleService: Error searching vehicles - " + lastErrorMessage);
             e.printStackTrace();
        } finally {
             // Close connection in a real app or ensure DbConfig handles pooling/closing
        }
        return vehicles;
    }


    /**
     * Helper method to map a ResultSet row to a VehicleModel object.
     * @param rs The ResultSet positioned at the current row.
     * @return A populated VehicleModel object.
     * @throws SQLException If a database access error occurs.
     */
    private VehicleModel mapResultSetToVehicleModel(ResultSet rs) throws SQLException {
        VehicleModel vehicle = new VehicleModel();
        vehicle.setVehicleId(rs.getInt("vehicle_id")); // Make sure vehicleId is selected
        vehicle.setSerialNumber(rs.getString("serial_number"));
        vehicle.setBrandName(rs.getString("brand_name"));
        vehicle.setModel(rs.getString("model"));
        vehicle.setType(rs.getString("type"));
        vehicle.setStatus(rs.getString("status"));
        Date manufacturedDateSql = rs.getDate("manufactured_date");
        vehicle.setManufacturedDate(manufacturedDateSql != null ? manufacturedDateSql.toLocalDate() : null);
        vehicle.setImagePath(rs.getString("image_path"));
        vehicle.setDescription(rs.getString("description"));
        vehicle.setPrice(rs.getBigDecimal("price"));
        return vehicle;
    }


    // --- Keep existing utility methods ---
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public boolean isConnectionError() {
        return isConnectionError;
    }
     // --- End existing utility methods ---
}