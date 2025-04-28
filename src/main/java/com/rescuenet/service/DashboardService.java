package com.rescuenet.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.rescuenet.config.DbConfig;

public class DashboardService {

    public int getTotalVehicles() {
        return getCount("SELECT COUNT(*) FROM vehicles");
    }

    public int getAvailableVehicles() {
        return getCount("SELECT COUNT(*) FROM vehicles WHERE status = 'available'");
    }

    public int getVehiclesInService() {
        return getCount("SELECT COUNT(*) FROM services WHERE service_date >= CURDATE()");
    }

    private int getCount(String query) {
        int count = 0;
        Connection conn = null;
        try {
            conn = DbConfig.getDbConnection();
            if (conn == null) {
                System.err.println("Failed to establish database connection.");
                return count;
            }
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
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