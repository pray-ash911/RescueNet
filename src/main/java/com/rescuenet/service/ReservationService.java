package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.ReservationModel;
import com.rescuenet.model.UserModel;
import com.rescuenet.model.VehicleModel;

/**
 * @author Prayash Rawal
 */

/**
 * Service class for managing reservation operations in the RescueNet
 * application. Handles retrieval, creation, updating, and deletion of
 * reservations, including vehicle availability checks and user/vehicle data
 * integration.
 */
public class ReservationService {

	private String lastErrorMessage;

	/**
	 * Retrieves the last error message encountered during a service operation.
	 *
	 * @return the last error message, or null if no error occurred
	 */
	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

	/**
	 * Retrieves all reservations with associated user and vehicle details.
	 *
	 * @return a list of ReservationModel objects with populated details
	 * @throws SQLException if a database access error occurs
	 */
	public List<ReservationModel> getAllReservationsWithDetails() throws SQLException {
		List<ReservationModel> reservations = new ArrayList<>();
		String sql = "SELECT r.reservation_id, r.user_id, r.vehicle_id, r.reservation_date, r.status, "
				+ "u.username, v.brand_name, v.model, v.serial_number " + "FROM Reservations r "
				+ "JOIN Users u ON r.user_id = u.user_id " + "JOIN Vehicles v ON r.vehicle_id = v.vehicle_id "
				+ "ORDER BY r.reservation_date DESC, r.reservation_id DESC";

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		// --- Query Reservations ---
		try {
			conn = DbConfig.getDbConnection();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ReservationModel reservation = new ReservationModel();
				reservation.setReservationId(rs.getInt("reservation_id"));
				reservation.setUserId(rs.getInt("user_id"));
				reservation.setVehicleId(rs.getInt("vehicle_id"));
				Date sqlDate = rs.getDate("reservation_date");
				if (sqlDate != null) {
					reservation.setReservationDate(sqlDate.toLocalDate());
				}
				reservation.setStatus(rs.getString("status"));
				reservation.setUsername(rs.getString("username"));
				reservation.setVehicleInfo(rs.getString("brand_name") + " " + rs.getString("model") + " ("
						+ rs.getString("serial_number") + ")");
				reservations.add(reservation);
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error fetching reservations: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			closeResources(conn, stmt, rs);
		}
		return reservations;
	}

	/**
	 * Retrieves a reservation by its ID with associated user and vehicle details.
	 *
	 * @param reservationId the ID of the reservation to retrieve
	 * @return a ReservationModel object with populated details, or null if not
	 *         found
	 * @throws SQLException if a database access error occurs
	 */
	public ReservationModel getReservationById(int reservationId) throws SQLException {
		ReservationModel reservation = null;
		String sql = "SELECT r.reservation_id, r.user_id, r.vehicle_id, r.reservation_date, r.status, "
				+ "u.username, v.brand_name, v.model, v.serial_number " + "FROM Reservations r "
				+ "JOIN Users u ON r.user_id = u.user_id " + "JOIN Vehicles v ON r.vehicle_id = v.vehicle_id "
				+ "WHERE r.reservation_id = ?";

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		// --- Query Reservation ---
		try {
			conn = DbConfig.getDbConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, reservationId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				reservation = new ReservationModel();
				reservation.setReservationId(rs.getInt("reservation_id"));
				reservation.setUserId(rs.getInt("user_id"));
				reservation.setVehicleId(rs.getInt("vehicle_id"));
				Date sqlDate = rs.getDate("reservation_date");
				if (sqlDate != null) {
					reservation.setReservationDate(sqlDate.toLocalDate());
				}
				reservation.setStatus(rs.getString("status"));
				reservation.setUsername(rs.getString("username"));
				reservation.setVehicleInfo(rs.getString("brand_name") + " " + rs.getString("model") + " ("
						+ rs.getString("serial_number") + ")");
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error fetching reservation by ID: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			closeResources(conn, stmt, rs);
		}
		return reservation;
	}

	/**
	 * Creates a new reservation for a specific vehicle and date.
	 *
	 * @param reservation the ReservationModel object containing reservation details
	 * @return true if the reservation was created successfully, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean createReservation(ReservationModel reservation) throws SQLException {
		// --- Validate Vehicle Availability ---
		if (!isVehicleAvailableForDate(reservation.getVehicleId(), reservation.getReservationDate(), -1)) {
			return false;
		}

		String sql = "INSERT INTO Reservations (user_id, vehicle_id, reservation_date, status) VALUES (?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean success = false;

		// --- Create Reservation ---
		try {
			conn = DbConfig.getDbConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, reservation.getUserId());
			stmt.setInt(2, reservation.getVehicleId());
			stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
			stmt.setString(4, reservation.getStatus());

			int rows = stmt.executeUpdate();
			success = rows > 0;

			if (success && "Confirmed".equalsIgnoreCase(reservation.getStatus())) {
				// --- Update Vehicle Status ---
				if (!updateVehicleStatus(conn, reservation.getVehicleId(), "Reserved")) {
					conn.rollback();
					lastErrorMessage = "Failed to update vehicle status after creating reservation.";
					System.err.println("ReservationService: " + lastErrorMessage);
					return false;
				}
				conn.commit();
			} else if (!success) {
				conn.rollback();
				lastErrorMessage = "Failed to create reservation.";
				System.err.println("ReservationService: " + lastErrorMessage);
			} else {
				conn.commit();
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error creating reservation: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException ex) {
				System.err.println("ReservationService: Rollback failed: " + ex.getMessage());
			}
			throw new SQLException(lastErrorMessage, e);
		} finally {
			try {
				if (conn != null)
					conn.setAutoCommit(true);
			} catch (SQLException ex) {
				System.err.println("ReservationService: Failed to reset auto-commit: " + ex.getMessage());
			}
			closeResources(conn, stmt, null);
		}
		return success;
	}

	/**
	 * Updates an existing reservation with new details.
	 *
	 * @param reservation the ReservationModel object with updated details
	 * @return true if the reservation was updated successfully, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean updateReservation(ReservationModel reservation) throws SQLException {
		// --- Validate Vehicle Availability ---
		if (!isVehicleAvailableForDate(reservation.getVehicleId(), reservation.getReservationDate(),
				reservation.getReservationId())) {
			return false;
		}

		// --- Fetch Previous Vehicle ID ---
		ReservationModel oldReservation = getReservationById(reservation.getReservationId());
		if (oldReservation == null) {
			lastErrorMessage = "Reservation not found for update.";
			System.err.println("ReservationService: " + lastErrorMessage);
			return false;
		}
		int previousVehicleId = oldReservation.getVehicleId();

		String sql = "UPDATE Reservations SET user_id = ?, vehicle_id = ?, reservation_date = ?, status = ? WHERE reservation_id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean success = false;

		// --- Update Reservation ---
		try {
			conn = DbConfig.getDbConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, reservation.getUserId());
			stmt.setInt(2, reservation.getVehicleId());
			stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
			stmt.setString(4, reservation.getStatus());
			stmt.setInt(5, reservation.getReservationId());

			int rows = stmt.executeUpdate();
			success = rows > 0;

			if (success) {
				boolean statusUpdateOk = true;
				// --- Update Vehicle Statuses ---
				if (previousVehicleId != reservation.getVehicleId()) {
					if (!updateVehicleStatusBasedOnOtherReservations(conn, previousVehicleId)) {
						statusUpdateOk = false;
					}
				}
				if (statusUpdateOk) {
					String targetStatus = "Confirmed".equalsIgnoreCase(reservation.getStatus()) ? "Reserved"
							: "Available";
					if (!updateVehicleStatus(conn, reservation.getVehicleId(), targetStatus)) {
						statusUpdateOk = false;
					}
				}
				if (statusUpdateOk) {
					conn.commit();
				} else {
					conn.rollback();
					lastErrorMessage = "Failed to update vehicle status during reservation update.";
					System.err.println("ReservationService: " + lastErrorMessage);
					success = false;
				}
			} else {
				conn.rollback();
				lastErrorMessage = "Failed to update reservation.";
				System.err.println("ReservationService: " + lastErrorMessage);
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error updating reservation: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException ex) {
				System.err.println("ReservationService: Rollback failed: " + ex.getMessage());
			}
			throw new SQLException(lastErrorMessage, e);
		} finally {
			try {
				if (conn != null)
					conn.setAutoCommit(true);
			} catch (SQLException ex) {
				System.err.println("ReservationService: Failed to reset auto-commit: " + ex.getMessage());
			}
			closeResources(conn, stmt, null);
		}
		return success;
	}

	/**
	 * Deletes a reservation by its ID and updates the vehicle status if necessary.
	 *
	 * @param reservationId the ID of the reservation to delete
	 * @return true if the reservation was deleted successfully, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	public boolean deleteReservation(int reservationId) throws SQLException {
		// --- Fetch Reservation Details ---
		ReservationModel reservation = getReservationById(reservationId);
		if (reservation == null) {
			lastErrorMessage = "Reservation not found for deletion.";
			System.err.println("ReservationService: " + lastErrorMessage);
			return false;
		}
		int vehicleId = reservation.getVehicleId();

		String sql = "DELETE FROM Reservations WHERE reservation_id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean success = false;

		// --- Delete Reservation ---
		try {
			conn = DbConfig.getDbConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, reservationId);

			int rows = stmt.executeUpdate();
			success = rows > 0;

			if (success) {
				// --- Update Vehicle Status ---
				if (!updateVehicleStatusBasedOnOtherReservations(conn, vehicleId)) {
					conn.rollback();
					lastErrorMessage = "Failed to update vehicle status after deletion.";
					System.err.println("ReservationService: " + lastErrorMessage);
					success = false;
				} else {
					conn.commit();
				}
			} else {
				conn.rollback();
				lastErrorMessage = "Failed to delete reservation.";
				System.err.println("ReservationService: " + lastErrorMessage);
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error deleting reservation: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException ex) {
				System.err.println("ReservationService: Rollback failed: " + ex.getMessage());
			}
			throw new SQLException(lastErrorMessage, e);
		} finally {
			try {
				if (conn != null)
					conn.setAutoCommit(true);
			} catch (SQLException ex) {
				System.err.println("ReservationService: Failed to reset auto-commit: " + ex.getMessage());
			}
			closeResources(conn, stmt, null);
		}
		return success;
	}

	/**
	 * Checks if a vehicle is available for reservation on a specific date.
	 *
	 * @param vehicleId            the ID of the vehicle to check
	 * @param date                 the reservation date
	 * @param excludeReservationId the ID of the reservation to exclude (for
	 *                             updates), or -1
	 * @return true if the vehicle is available, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	private boolean isVehicleAvailableForDate(int vehicleId, LocalDate date, int excludeReservationId)
			throws SQLException {
		// --- Check Vehicle Status ---
		String vehicleStatus = getVehicleStatus(vehicleId);
		if (!"Available".equalsIgnoreCase(vehicleStatus) && !"Reserved".equalsIgnoreCase(vehicleStatus)) {
			lastErrorMessage = "Vehicle ID " + vehicleId + " is not available (Status: " + vehicleStatus + ").";
			System.err.println("ReservationService: " + lastErrorMessage);
			return false;
		}

		// --- Check for Conflicting Reservations ---
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DbConfig.getDbConnection();
			String sql = "SELECT COUNT(*) FROM Reservations "
					+ "WHERE vehicle_id = ? AND reservation_date = ? AND status = ? AND reservation_id != ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, vehicleId);
			stmt.setDate(2, Date.valueOf(date));
			stmt.setString(3, "Confirmed");
			stmt.setInt(4, excludeReservationId > 0 ? excludeReservationId : -1);

			rs = stmt.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) {
				lastErrorMessage = "Vehicle ID " + vehicleId + " is reserved for " + date + ".";
				System.err.println("ReservationService: " + lastErrorMessage);
				return false;
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error checking vehicle availability: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			closeResources(conn, stmt, rs);
		}
		return true;
	}

	/**
	 * Retrieves the status of a vehicle by its ID.
	 *
	 * @param vehicleId the ID of the vehicle
	 * @return the vehicle status, or null if not found or an error occurs
	 * @throws SQLException if a database access error occurs
	 */
	private String getVehicleStatus(int vehicleId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String status = null;

		// --- Query Vehicle Status ---
		try {
			conn = DbConfig.getDbConnection();
			String sql = "SELECT status FROM Vehicles WHERE vehicle_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, vehicleId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				status = rs.getString("status");
			} else {
				lastErrorMessage = "Vehicle not found for ID: " + vehicleId;
				System.err.println("ReservationService: " + lastErrorMessage);
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error checking vehicle status: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			closeResources(conn, stmt, rs);
		}
		return status;
	}

	/**
	 * Updates the status of a vehicle within a transaction.
	 *
	 * @param conn      the database connection (within a transaction)
	 * @param vehicleId the ID of the vehicle to update
	 * @param status    the new status to set
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	private boolean updateVehicleStatus(Connection conn, int vehicleId, String status) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE Vehicles SET status = ? WHERE vehicle_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, status);
			stmt.setInt(2, vehicleId);
			int rows = stmt.executeUpdate();
			if (rows == 0) {
				System.err.println("ReservationService: No rows updated for vehicle ID " + vehicleId);
			}
			return rows > 0;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("ReservationService: Error closing PreparedStatement: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Updates a vehicle's status based on the existence of other confirmed
	 * reservations.
	 *
	 * @param conn      the database connection (within a transaction)
	 * @param vehicleId the ID of the vehicle to update
	 * @return true if the update was successful, false otherwise
	 * @throws SQLException if a database access error occurs
	 */
	private boolean updateVehicleStatusBasedOnOtherReservations(Connection conn, int vehicleId) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			// --- Check for Other Reservations ---
			String sqlCheck = "SELECT 1 FROM Reservations WHERE vehicle_id = ? AND status = ? LIMIT 1";
			stmt = conn.prepareStatement(sqlCheck);
			stmt.setInt(1, vehicleId);
			stmt.setString(2, "Confirmed");
			rs = stmt.executeQuery();
			boolean hasOtherReservations = rs.next();

			// --- Update Vehicle Status ---
			String targetStatus = hasOtherReservations ? "Reserved" : "Available";
			System.out.println(
					"ReservationService: Setting vehicle ID " + vehicleId + " status to '" + targetStatus + "'.");
			return updateVehicleStatus(conn, vehicleId, targetStatus);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					System.err.println("ReservationService: Error closing ResultSet: " + e.getMessage());
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("ReservationService: Error closing PreparedStatement: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Retrieves all available vehicles for dropdown selection.
	 *
	 * @return a list of VehicleModel objects representing available vehicles
	 * @throws SQLException if a database access error occurs
	 */
	public List<VehicleModel> getAvailableVehiclesForDropdown() throws SQLException {
		List<VehicleModel> vehicles = new ArrayList<>();
		String sql = "SELECT vehicle_id, brand_name, model, serial_number FROM Vehicles WHERE status = 'Available' ORDER BY brand_name, model";

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		// --- Query Available Vehicles ---
		try {
			conn = DbConfig.getDbConnection();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				VehicleModel vehicle = new VehicleModel();
				vehicle.setVehicleId(rs.getInt("vehicle_id"));
				vehicle.setBrandName(rs.getString("brand_name"));
				vehicle.setModel(rs.getString("model"));
				vehicle.setSerialNumber(rs.getString("serial_number"));
				vehicles.add(vehicle);
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error fetching available vehicles: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			closeResources(conn, stmt, rs);
		}
		return vehicles;
	}

	/**
	 * Retrieves all active users for dropdown selection.
	 *
	 * @return a list of UserModel objects representing active users
	 * @throws SQLException if a database access error occurs
	 */
	public List<UserModel> getAllUsers() throws SQLException {
		List<UserModel> users = new ArrayList<>();
		String sql = "SELECT user_id, username, full_name FROM Users WHERE is_active = 1 ORDER BY username";

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		// --- Query Active Users ---
		try {
			conn = DbConfig.getDbConnection();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				UserModel user = new UserModel();
				user.setUserId(rs.getInt("user_id"));
				user.setUsername(rs.getString("username"));
				user.setFullName(rs.getString("full_name"));
				users.add(user);
			}
		} catch (SQLException | ClassNotFoundException e) {
			lastErrorMessage = "Error fetching users: " + e.getMessage();
			System.err.println("ReservationService: " + lastErrorMessage);
			throw new SQLException(lastErrorMessage, e);
		} finally {
			closeResources(conn, stmt, rs);
		}
		return users;
	}

	/**
	 * Closes database resources, preserving connections used in transactions.
	 *
	 * @param conn the database connection, or null if managed externally
	 * @param stmt the prepared statement to close, or null
	 * @param rs   the result set to close, or null
	 */
	private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				System.err.println("ReservationService: Error closing ResultSet: " + e.getMessage());
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				System.err.println("ReservationService: Error closing PreparedStatement: " + e.getMessage());
			}
		}
		if (conn != null) {
			try {
				if (conn.getAutoCommit()) {
					conn.close();
				}
			} catch (SQLException e) {
				System.err.println("ReservationService: Error closing Connection: " + e.getMessage());
			}
		}
	}
}