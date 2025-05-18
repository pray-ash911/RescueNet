package com.rescuenet.service;

import com.rescuenet.model.VehicleModel;
import com.rescuenet.config.DbConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Prayash Rawal
 */

/**
 * Service class for managing vehicle operations in the RescueNet application.
 * Handles adding vehicles, retrieving categorized available vehicles, and
 * searching for available vehicles based on user queries.
 */
public class VehicleService {

	private Connection dbConn; // Instance variable for connection
	private String lastErrorMessage;
	private boolean isConnectionError = false; // Initialize to false

	/**
	 * Initializes the database connection for vehicle operations. Handles
	 * exceptions internally and sets flags.
	 */
	public VehicleService() {
		Connection tempConn = null;
		try {
			tempConn = DbConfig.getDbConnection(); // This might throw ClassNotFoundException or SQLException
			if (tempConn == null) { // Defensive check if DbConfig returns null
				this.isConnectionError = true;
				this.lastErrorMessage = "Database connection could not be established (DbConfig returned null).";
				System.err.println("VehicleService Constructor: " + this.lastErrorMessage);
			} else {
				System.out.println("VehicleService Constructor: Database connection appears to be established.");
			}
		} catch (ClassNotFoundException e) {
			this.isConnectionError = true;
			this.lastErrorMessage = "VehicleService: Database driver not found during initialization - "
					+ e.getMessage();
			System.err.println(this.lastErrorMessage);
			e.printStackTrace();
		} catch (SQLException e) {
			this.isConnectionError = true;
			this.lastErrorMessage = "VehicleService: SQL error during database connection initialization - "
					+ e.getMessage();
			System.err.println(this.lastErrorMessage);
			e.printStackTrace();
		} catch (Exception e) { // Catch any other unexpected runtime errors
			this.isConnectionError = true;
			this.lastErrorMessage = "VehicleService: Unexpected error during initialization - " + e.getMessage();
			System.err.println(this.lastErrorMessage);
			e.printStackTrace();
		}
		this.dbConn = tempConn; // Assign, even if null (isConnectionError will be true)
	}

	/**
	 * Retrieves the last error message encountered during a service operation.
	 *
	 * @return the last error message, or null if no error occurred
	 */
	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

	/**
	 * Checks if a database connection error has occurred.
	 *
	 * @return true if a connection error exists, false otherwise
	 */
	public boolean isConnectionError() {
		return isConnectionError;
	}

	/**
	 * Adds a new vehicle to the database.
	 *
	 * @param vehicle the VehicleModel object containing vehicle details
	 * @return true if the vehicle was added successfully, false otherwise
	 */
	public boolean addVehicle(VehicleModel vehicle) { // No longer throws SQLException
		lastErrorMessage = null;
		if (isConnectionError || dbConn == null) {
			lastErrorMessage = "Cannot add vehicle due to database connection error.";
			System.err.println("VehicleService: " + lastErrorMessage);
			return false;
		}

		String sql = "INSERT INTO Vehicles (serial_number, brand_name, model, type, status, manufactured_date, image_path, description, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
			pstmt.setString(1, vehicle.getSerialNumber());
			pstmt.setString(2, vehicle.getBrandName());
			pstmt.setString(3, vehicle.getModel());
			pstmt.setString(4, vehicle.getType());
			pstmt.setString(5, vehicle.getStatus());
			pstmt.setObject(6,
					vehicle.getManufacturedDate() != null ? Date.valueOf(vehicle.getManufacturedDate()) : null);
			pstmt.setString(7, vehicle.getImagePath());
			pstmt.setString(8, vehicle.getDescription());
			pstmt.setBigDecimal(9, vehicle.getPrice());
			int rowsAffected = pstmt.executeUpdate();
			System.out.println("VehicleService: Added vehicle, rows affected: " + rowsAffected);
			return rowsAffected > 0;
		} catch (SQLException e) {
			lastErrorMessage = "Error adding vehicle: " + e.getMessage();
			System.err.println("VehicleService: " + lastErrorMessage);
			e.printStackTrace(); // Log the full stack trace
			return false; // Indicate failure
		}
		// Note: Connection handling (closing) needs to be consistent.
		// If dbConn is a class member and the service is request-scoped,
		// it should be closed appropriately (e.g., in a finally block of the calling
		// controller's method,
		// or if the service implements AutoCloseable).
		// For now, assuming it's either pooled or managed elsewhere.
	}

	/**
	 * Retrieves all available vehicles, grouped by type for display.
	 *
	 * @return a Map with vehicle type as the key and a list of VehicleModel objects
	 *         as the value
	 */
	public Map<String, List<VehicleModel>> getCategorizedAvailableVehicles() { // No longer throws SQLException
		lastErrorMessage = null;
		Map<String, List<VehicleModel>> categorizedVehicles = new LinkedHashMap<>();
		if (isConnectionError || dbConn == null) {
			lastErrorMessage = "Cannot fetch vehicles due to database connection error.";
			System.err.println("VehicleService: " + lastErrorMessage);
			return categorizedVehicles; // Return empty map
		}

		String sql = "SELECT vehicle_id, serial_number, brand_name, model, type, status, manufactured_date, image_path, description, price "
				+ "FROM Vehicles WHERE status = ? ORDER BY type, brand_name, model";
		try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
			pstmt.setString(1, "Available");
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				VehicleModel vehicle = mapResultSetToVehicleModel(rs);
				String vehicleType = vehicle.getType();
				categorizedVehicles.computeIfAbsent(vehicleType, k -> new ArrayList<>()).add(vehicle);
			}
		} catch (SQLException e) {
			lastErrorMessage = "Error fetching categorized vehicles: " + e.getMessage();
			System.err.println("VehicleService: " + lastErrorMessage);
			e.printStackTrace(); // Log stack trace
			// Return empty map or rethrow as a runtime exception if preferred
		}
		return categorizedVehicles;
	}

	/**
	 * Searches for available vehicles matching the provided query across multiple
	 * fields.
	 *
	 * @param searchQuery the user's search term
	 * @return a list of VehicleModel objects matching the query
	 */
	public List<VehicleModel> searchAvailableVehicles(String searchQuery) { // No longer throws SQLException
		lastErrorMessage = null;
		List<VehicleModel> vehicles = new ArrayList<>();
		if (isConnectionError || dbConn == null) {
			lastErrorMessage = "Cannot search vehicles due to database connection error.";
			System.err.println("VehicleService: " + lastErrorMessage);
			return vehicles;
		}
		if (searchQuery == null || searchQuery.trim().isEmpty()) {
			// If search is empty, return all available vehicles categorized, or an empty
			// list.
			// For search context, usually means "no specific search", so could return all
			// or empty.
			// To return all available (not categorized):
			// return getAllAvailableVehicles(); // You'd need to create this method
			return vehicles; // Returning empty for now, consistent with search results.
		}

		String queryParam = "%" + searchQuery.toLowerCase() + "%";
		String sql = "SELECT vehicle_id, serial_number, brand_name, model, type, status, manufactured_date, image_path, description, price "
				+ "FROM Vehicles " + "WHERE status = ? AND (" + "LOWER(brand_name) LIKE ? OR "
				+ "LOWER(model) LIKE ? OR " + "LOWER(type) LIKE ? OR " + "LOWER(serial_number) LIKE ? OR "
				+ "LOWER(description) LIKE ?)" + " ORDER BY type, brand_name, model";
		try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
			pstmt.setString(1, "Available");
			pstmt.setString(2, queryParam);
			pstmt.setString(3, queryParam);
			pstmt.setString(4, queryParam);
			pstmt.setString(5, queryParam);
			pstmt.setString(6, queryParam);

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				vehicles.add(mapResultSetToVehicleModel(rs));
			}
		} catch (SQLException e) {
			lastErrorMessage = "Error searching vehicles: " + e.getMessage();
			System.err.println("VehicleService: " + lastErrorMessage);
			e.printStackTrace(); // Log stack trace
		}
		return vehicles;
	}

	/**
	 * Maps a ResultSet row to a VehicleModel object. (Keep this method as is)
	 */
	private VehicleModel mapResultSetToVehicleModel(ResultSet rs) throws SQLException {
		// ... (implementation from previous response) ...
		VehicleModel vehicle = new VehicleModel();
		vehicle.setVehicleId(rs.getInt("vehicle_id"));
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
}