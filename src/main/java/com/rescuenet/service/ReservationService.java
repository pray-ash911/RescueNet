package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date; // Use java.sql.Date for PreparedStatement
import java.time.LocalDate; // Use java.time.LocalDate for logic
import java.util.ArrayList;
import java.util.List;

import com.rescuenet.config.DbConfig;
import com.rescuenet.model.ReservationModel;
import com.rescuenet.model.UserModel;
import com.rescuenet.model.VehicleModel; // Import VehicleModel

public class ReservationService {

    private String lastErrorMessage;

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    // Fetch all reservations with user and vehicle details for display
    public List<ReservationModel> getAllReservationsWithDetails() {
        List<ReservationModel> reservations = new ArrayList<>();
        // SQL joining Reservations, Users, and Vehicles
        String sql = "SELECT r.reservation_id, r.user_id, r.vehicle_id, r.reservation_date, r.status, " +
                     "u.username, v.brand_name, v.model, v.serial_number " + // Include vehicle details
                     "FROM Reservations r " +
                     "JOIN Users u ON r.user_id = u.user_id " +
                     "JOIN Vehicles v ON r.vehicle_id = v.vehicle_id " + // Join on vehicle_id
                     "ORDER BY r.reservation_date DESC, r.reservation_id DESC"; // Example ordering

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DbConfig.getDbConnection(); // Assuming DbConfig handles ClassNotFoundException internally now
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ReservationModel reservation = new ReservationModel();
                reservation.setReservationId(rs.getInt("reservation_id"));
                reservation.setUserId(rs.getInt("user_id"));
                reservation.setVehicleId(rs.getInt("vehicle_id"));

                // Convert java.sql.Date to java.time.LocalDate
                Date sqlDate = rs.getDate("reservation_date");
                if (sqlDate != null) {
                    reservation.setReservationDate(sqlDate.toLocalDate());
                }

                reservation.setStatus(rs.getString("status"));

                // Populate display fields
                reservation.setUsername(rs.getString("username"));
                reservation.setVehicleInfo(rs.getString("brand_name") + " " +
                                           rs.getString("model") + " (" +
                                           rs.getString("serial_number") + ")"); // Example vehicle info string

                reservations.add(reservation);
            }
        } catch (SQLException e) {
            lastErrorMessage = "Database error fetching reservations: " + e.getMessage();
            System.err.println("ReservationService: Error fetching reservations: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
             lastErrorMessage = "Database driver error: " + e.getMessage();
             System.err.println("ReservationService: Error loading DB driver: " + e.getMessage());
             e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        return reservations;
    }


    public ReservationModel getReservationById(int reservationId) {
        ReservationModel reservation = null;
        String sql = "SELECT r.reservation_id, r.user_id, r.vehicle_id, r.reservation_date, r.status, " +
                     "u.username, v.brand_name, v.model, v.serial_number " +
                     "FROM Reservations r " +
                     "JOIN Users u ON r.user_id = u.user_id " +
                     "JOIN Vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "WHERE r.reservation_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

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
                reservation.setVehicleInfo(rs.getString("brand_name") + " " +
                                           rs.getString("model") + " (" +
                                           rs.getString("serial_number") + ")");
            }
        } catch (SQLException | ClassNotFoundException e) {
            lastErrorMessage = "Database error fetching reservation by ID: " + e.getMessage();
            System.err.println("ReservationService: Error fetching reservation by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        return reservation;
    }

    // Simplified: Create a reservation for a specific date
    public boolean createReservation(ReservationModel reservation) {
        // Simplified availability check: Is the vehicle itself marked 'Available'?
        // And is it NOT reserved by someone else *on the exact same day*?
        if (!isVehicleAvailableForDate(reservation.getVehicleId(), reservation.getReservationDate(), -1)) {
             // lastErrorMessage should be set by isVehicleAvailableForDate
            return false;
        }

        String sql = "INSERT INTO Reservations (user_id, vehicle_id, reservation_date, status) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DbConfig.getDbConnection();
            conn.setAutoCommit(false); // Start transaction

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reservation.getUserId());
            stmt.setInt(2, reservation.getVehicleId());
            // Convert LocalDate to sql.Date
            stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            stmt.setString(4, reservation.getStatus()); // e.g., "Confirmed", "Pending"

            int rows = stmt.executeUpdate();
            success = rows > 0;

            if (success) {
                // If reservation is confirmed, update vehicle status
                if ("Confirmed".equalsIgnoreCase(reservation.getStatus())) {
                    if (!updateVehicleStatus(conn, reservation.getVehicleId(), "Reserved")) {
                        // Rollback if vehicle status update fails
                        conn.rollback();
                        lastErrorMessage = "Failed to update vehicle status after creating reservation.";
                        return false;
                    }
                }
                conn.commit(); // Commit transaction
            } else {
                 conn.rollback(); // Rollback if insert failed
                 lastErrorMessage = "Reservation insert failed.";
            }

        } catch (SQLException | ClassNotFoundException e) {
            lastErrorMessage = "Database error creating reservation: " + e.getMessage();
            System.err.println("ReservationService: Error creating reservation: " + e.getMessage());
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            success = false;
        } finally {
             try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { System.err.println("Failed to reset auto-commit: " + ex.getMessage()); }
            closeResources(null, stmt, null); // Connection closed separately or by DbConfig pool
             try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return success;
    }

     // Simplified: Update a reservation
     public boolean updateReservation(ReservationModel reservation) {
        // Check if the *potentially new* vehicle is available on the date, excluding the current reservation
         if (!isVehicleAvailableForDate(reservation.getVehicleId(), reservation.getReservationDate(), reservation.getReservationId())) {
             // lastErrorMessage should be set by isVehicleAvailableForDate
             return false;
         }

         String sql = "UPDATE Reservations SET user_id = ?, vehicle_id = ?, reservation_date = ?, status = ? WHERE reservation_id = ?";
         Connection conn = null;
         PreparedStatement stmt = null;
         boolean success = false;
         String oldVehicleId = null; // To potentially update status of old vehicle if changed

         // Need to know the old vehicle ID before update
         ReservationModel oldReservation = getReservationById(reservation.getReservationId());
         if (oldReservation == null) {
             lastErrorMessage = "Original reservation not found for update.";
             return false;
         }
         int previousVehicleId = oldReservation.getVehicleId();


         try {
             conn = DbConfig.getDbConnection();
             conn.setAutoCommit(false); // Start transaction

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
                 // If vehicle changed, set old one to Available (if no other confirmed reservations exist for it)
                 if (previousVehicleId != reservation.getVehicleId()) {
                     if (!updateVehicleStatusBasedOnOtherReservations(conn, previousVehicleId)) {
                          statusUpdateOk = false;
                     }
                 }
                 // Update status of the current/new vehicle based on the reservation status
                 if (statusUpdateOk) {
                     String targetStatus = "Confirmed".equalsIgnoreCase(reservation.getStatus()) ? "Reserved" : "Available";
                      if (!updateVehicleStatus(conn, reservation.getVehicleId(), targetStatus)) {
                         statusUpdateOk = false;
                     }
                 }

                 if (statusUpdateOk) {
                     conn.commit();
                 } else {
                     conn.rollback();
                     lastErrorMessage = "Failed to update vehicle status during reservation update.";
                     success = false;
                 }
             } else {
                 conn.rollback();
                 lastErrorMessage = "Reservation update failed (no rows affected).";
             }

         } catch (SQLException | ClassNotFoundException e) {
             lastErrorMessage = "Database error updating reservation: " + e.getMessage();
             System.err.println("ReservationService: Error updating reservation: " + e.getMessage());
             e.printStackTrace();
              try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
             success = false;
         } finally {
              try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { System.err.println("Failed to reset auto-commit: " + ex.getMessage()); }
             closeResources(null, stmt, null);
              try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
         }
         return success;
     }


     // Simplified: Delete a reservation
     public boolean deleteReservation(int reservationId) {
         Connection conn = null;
         PreparedStatement stmt = null;
         boolean success = false;

         // Need vehicle ID to update its status after deletion
         ReservationModel reservation = getReservationById(reservationId);
          if (reservation == null) {
             lastErrorMessage = "Reservation not found for deletion.";
             return false;
         }
         int vehicleId = reservation.getVehicleId();


         try {
             conn = DbConfig.getDbConnection();
             conn.setAutoCommit(false); // Transaction

             String sql = "DELETE FROM Reservations WHERE reservation_id = ?";
             stmt = conn.prepareStatement(sql);
             stmt.setInt(1, reservationId);

             int rows = stmt.executeUpdate();
             success = rows > 0;

             if (success) {
                  // Update vehicle status back to available *if* no other confirmed reservations exist
                  if (updateVehicleStatusBasedOnOtherReservations(conn, vehicleId)) {
                      conn.commit();
                  } else {
                      conn.rollback();
                      lastErrorMessage = "Failed to update vehicle status after deletion.";
                      success = false;
                  }
             } else {
                 conn.rollback();
                 lastErrorMessage = "Reservation delete failed (no rows affected).";
             }

         } catch (SQLException | ClassNotFoundException e) {
             lastErrorMessage = "Database error deleting reservation: " + e.getMessage();
             System.err.println("ReservationService: Error deleting reservation: " + e.getMessage());
             e.printStackTrace();
             try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
             success = false;
         } finally {
             try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { System.err.println("Failed to reset auto-commit: " + ex.getMessage()); }
             closeResources(null, stmt, null);
             try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
         }
         return success;
     }


    // --- Helper Methods (Simplified / Adapted) ---

    // Simplified check: Is vehicle status 'Available' and no confirmed reservation for this exact date?
    private boolean isVehicleAvailableForDate(int vehicleId, LocalDate date, int excludeReservationId) {
        String vehicleStatus = getVehicleStatus(vehicleId);
        if (!"Available".equalsIgnoreCase(vehicleStatus) && !"Reserved".equalsIgnoreCase(vehicleStatus)) { // Allow checking if already Reserved for update scenarios
            lastErrorMessage = "Vehicle (ID: " + vehicleId +") is not Available or Reserved (Current Status: "+vehicleStatus+"). Cannot make new reservation.";
            System.err.println(lastErrorMessage);
            return false;
        }

        // Check if another CONFIRMED reservation exists for this vehicle on this date
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean conflictingReservationExists = false;
        try {
            conn = DbConfig.getDbConnection();
            // Count conflicting reservations (Confirmed status on the same date, excluding self)
            String sql = "SELECT COUNT(*) FROM Reservations " +
                         "WHERE vehicle_id = ? AND reservation_date = ? AND status = ? AND reservation_id != ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vehicleId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, "Confirmed"); // Only consider confirmed reservations as conflicts
            stmt.setInt(4, excludeReservationId > 0 ? excludeReservationId : -1); // Exclude current reservation if updating

            rs = stmt.executeQuery();
            if (rs.next()) {
                conflictingReservationExists = rs.getInt(1) > 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            lastErrorMessage = "Database error checking for conflicting reservations: " + e.getMessage();
            System.err.println("ReservationService: Error checking conflicting reservations: " + e.getMessage());
            e.printStackTrace();
            return false; // Fail safe
        } finally {
            closeResources(conn, stmt, rs);
        }

        if (conflictingReservationExists) {
             lastErrorMessage = "Vehicle (ID: " + vehicleId + ") already has a confirmed reservation for " + date + ".";
             System.err.println(lastErrorMessage);
            return false;
        }

        // Optional: Add check against Services table here if needed (requires Services schema)
        // if (isVehicleInService(vehicleId, date)) { return false; }

        return true; // Vehicle status is OK and no conflicting reservation found
    }

    private String getVehicleStatus(int vehicleId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String status = null;
        try {
            conn = DbConfig.getDbConnection();
            String sql = "SELECT status FROM Vehicles WHERE vehicle_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, vehicleId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                status = rs.getString("status");
            } else {
                 lastErrorMessage = "Vehicle not found (ID: " + vehicleId + ")";
            }
        } catch (SQLException | ClassNotFoundException e) {
            lastErrorMessage = "Database error checking vehicle status: " + e.getMessage();
            System.err.println("ReservationService: Error checking vehicle status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        return status;
    }


    // Update vehicle status directly
    private boolean updateVehicleStatus(Connection conn, int vehicleId, String status) throws SQLException {
        // Use the provided connection within a transaction
        PreparedStatement stmt = null;
         boolean success = false;
        try {
            String sql = "UPDATE Vehicles SET status = ? WHERE vehicle_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, vehicleId);
            int rows = stmt.executeUpdate();
            success = rows > 0;
             if (!success) {
                 System.err.println("ReservationService: Failed to update status for vehicle ID " + vehicleId + " (no rows affected).");
             }
        } finally {
            // Don't close connection here, managed by caller transaction
             if (stmt != null) {
                 try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
             }
        }
        return success;
    }

     // Helper to set vehicle status (Available/Reserved) based on *other* existing confirmed reservations
     private boolean updateVehicleStatusBasedOnOtherReservations(Connection conn, int vehicleId) throws SQLException {
         PreparedStatement stmt = null;
         ResultSet rs = null;
         boolean hasOtherReservations = false;
         try {
             // Check if any *other* confirmed reservations exist for this vehicle
             String sqlCheck = "SELECT 1 FROM Reservations WHERE vehicle_id = ? AND status = ? LIMIT 1";
             stmt = conn.prepareStatement(sqlCheck);
             stmt.setInt(1, vehicleId);
             stmt.setString(2, "Confirmed");
             rs = stmt.executeQuery();
             hasOtherReservations = rs.next();
         } finally {
             if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
             if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
         }

         String targetStatus = hasOtherReservations ? "Reserved" : "Available";
         System.out.println("ReservationService: Setting vehicle " + vehicleId + " status to '" + targetStatus + "' based on other reservations.");
         return updateVehicleStatus(conn, vehicleId, targetStatus);
     }


    // Fetch available vehicles (ID and display info) for dropdowns
    public List<VehicleModel> getAvailableVehiclesForDropdown() {
        List<VehicleModel> vehicles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        // Select vehicles that are currently 'Available'
        String sql = "SELECT vehicle_id, brand_name, model, serial_number FROM Vehicles WHERE status = 'Available' ORDER BY brand_name, model";

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
                // Construct display info (or do it in JSP)
                 // vehicle.setDisplayInfo(vehicle.getBrandName() + " " + vehicle.getModel() + " ("+vehicle.getSerialNumber()+")");
                vehicles.add(vehicle);
            }
        } catch (SQLException | ClassNotFoundException e) {
            lastErrorMessage = "Database error fetching available vehicles: " + e.getMessage();
            System.err.println("ReservationService: Error fetching available vehicles: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        return vehicles;
    }

    // Fetch all users for dropdowns
    public List<UserModel> getAllUsers() {
        List<UserModel> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        // Select users who are active? Or all users? Assuming all for now.
        String sql = "SELECT user_id, username, full_name FROM Users WHERE is_active = 1 ORDER BY username";

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
            lastErrorMessage = "Database error fetching users: " + e.getMessage();
            System.err.println("ReservationService: Error fetching users: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }
        return users;
    }

    // Standard resource closing utility
    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.err.println("ReservationService: Error closing ResultSet: " + e.getMessage());
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("ReservationService: Error closing PreparedStatement: " + e.getMessage());
        }
        // Only close connection if it wasn't passed in for a transaction
        if (conn != null) {
             try {
                // Check if connection is part of a transaction (autoCommit is false)
                // If so, don't close it here - let the calling method handle commit/rollback/close
                if (conn.getAutoCommit()) {
                     conn.close();
                }
             } catch (SQLException e) {
                System.err.println("ReservationService: Error closing Connection: " + e.getMessage());
             }
        }
    }
}