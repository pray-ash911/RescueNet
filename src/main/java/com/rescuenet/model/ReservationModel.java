package com.rescuenet.model;

import java.time.LocalDate; // Use LocalDate for dates

public class ReservationModel {
    private int reservationId;
    private int userId;
    private int vehicleId;
    private LocalDate reservationDate; // Changed from String to LocalDate
    private String status;

    // Optional: Add fields to hold related data for display (like username, vehicle info)
    // These are not directly mapped to the Reservations table columns but are useful
    private String username;     // Fetched via JOIN
    private String vehicleInfo;  // Constructed from vehicle details via JOIN or separate query

    // Optional: Timestamps if you need them in the model
    // private Timestamp createdAt;
    // private Timestamp updatedAt;

    // Constructor
    public ReservationModel() {}

    // Getters and Setters
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    // Helper to get date as String for forms if needed (use yyyy-MM-dd format)
    public String getReservationDateString() {
        return (this.reservationDate != null) ? this.reservationDate.toString() : "";
    }

     // Helper to set date from String from forms (use yyyy-MM-dd format)
    public void setReservationDateString(String dateString) {
        if (dateString != null && !dateString.trim().isEmpty()) {
            try {
                this.reservationDate = LocalDate.parse(dateString);
            } catch (java.time.format.DateTimeParseException e) {
                System.err.println("Error parsing date string: " + dateString);
                this.reservationDate = null; // Or handle error appropriately
            }
        } else {
            this.reservationDate = null;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // --- Getters/Setters for optional display fields ---
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    // Optional Getters/Setters for timestamps
    // public Timestamp getCreatedAt() { return createdAt; }
    // public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    // public Timestamp getUpdatedAt() { return updatedAt; }
    // public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}