package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rescuenet.config.DbConfig;

/**
 * @author Prayash Rawal
 */
/**
 * DashboardService provides methods to retrieve vehicle statistics for the
 * RescueNet application's dashboard. It interacts with the database to count
 * total vehicles, available vehicles, and vehicles in service.
 */
public class DashboardService {

	/**
	 * Retrieves the total number of vehicles in the database.
	 *
	 * @return the total number of vehicles
	 */
	public int getTotalVehicles() {
		return getCount("SELECT COUNT(*) FROM vehicles");
	}

	/**
	 * Retrieves the number of vehicles with an 'available' status.
	 *
	 * @return the number of available vehicles
	 */
	public int getAvailableVehicles() {
		return getCount("SELECT COUNT(*) FROM vehicles WHERE status = 'available'");
	}

	/**
	 * Retrieves the number of vehicles currently in service based on active service
	 * dates.
	 *
	 * @return the number of vehicles in service
	 */
	public int getVehiclesInService() {
		return getCount("SELECT COUNT(*) FROM services WHERE service_date >= CURDATE()");
	}

	/**
	 * Executes a SQL query to retrieve a count from the database.
	 *
	 * @param query the SQL query to execute
	 * @return the count result from the query, or 0 if an error occurs
	 */
	private int getCount(String query) {
		int count = 0;
		Connection conn = null;
		try {
			conn = DbConfig.getDbConnection();
			if (conn == null) {
				System.err.println("Failed to establish database connection.");
				return count;
			}
			try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					count = rs.getInt(1);
				}
			}
		} catch (SQLException | ClassNotFoundException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.err.println("Error closing connection: " + e.getMessage());
				}
			}
		}
		return count;
	}
}