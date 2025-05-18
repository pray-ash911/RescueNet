package com.rescuenet.model;

import java.time.LocalDate;

/**
 * @author Prayash Rawal
 */
/**
 * ReservationModel represents a vehicle reservation in the RescueNet
 * application. It stores reservation details such as user, vehicle, date, and
 * status, along with optional display fields for username and vehicle
 * information fetched via database joins.
 */
public class ReservationModel {
	private int reservationId;
	private int userId;
	private int vehicleId;
	private LocalDate reservationDate;
	private String status;

	private String username; // Fetched via JOIN
	private String vehicleInfo; // Constructed from vehicle details via JOIN or separate query

	/**
	 * Default constructor for ReservationModel.
	 */
	public ReservationModel() {
	}

	/**
	 * Gets the reservation ID.
	 *
	 * @return the reservation ID
	 */
	public int getReservationId() {
		return reservationId;
	}

	/**
	 * Sets the reservation ID.
	 *
	 * @param reservationId the reservation ID to set
	 */
	public void setReservationId(int reservationId) {
		this.reservationId = reservationId;
	}

	/**
	 * Gets the user ID.
	 *
	 * @return the user ID
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Sets the user ID.
	 *
	 * @param userId the user ID to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * Gets the vehicle ID.
	 *
	 * @return the vehicle ID
	 */
	public int getVehicleId() {
		return vehicleId;
	}

	/**
	 * Sets the vehicle ID.
	 *
	 * @param vehicleId the vehicle ID to set
	 */
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	/**
	 * Gets the reservation date.
	 *
	 * @return the reservation date as a LocalDate
	 */
	public LocalDate getReservationDate() {
		return reservationDate;
	}

	/**
	 * Sets the reservation date.
	 *
	 * @param reservationDate the reservation date to set
	 */
	public void setReservationDate(LocalDate reservationDate) {
		this.reservationDate = reservationDate;
	}

	/**
	 * Gets the reservation date as a string in yyyy-MM-dd format.
	 *
	 * @return the reservation date as a string, or an empty string if null
	 */
	public String getReservationDateString() {
		return (this.reservationDate != null) ? this.reservationDate.toString() : "";
	}

	/**
	 * Sets the reservation date from a string in yyyy-MM-dd format.
	 *
	 * @param dateString the date string to parse
	 */
	public void setReservationDateString(String dateString) {
		if (dateString != null && !dateString.trim().isEmpty()) {
			try {
				this.reservationDate = LocalDate.parse(dateString);
			} catch (java.time.format.DateTimeParseException e) {
				System.err.println("Error parsing date string: " + dateString);
				this.reservationDate = null;
			}
		} else {
			this.reservationDate = null;
		}
	}

	/**
	 * Gets the reservation status.
	 *
	 * @return the reservation status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the reservation status.
	 *
	 * @param status the reservation status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Gets the username associated with the reservation.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username associated with the reservation.
	 *
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the vehicle information for display.
	 *
	 * @return the vehicle information
	 */
	public String getVehicleInfo() {
		return vehicleInfo;
	}

	/**
	 * Sets the vehicle information for display.
	 *
	 * @param vehicleInfo the vehicle information to set
	 */
	public void setVehicleInfo(String vehicleInfo) {
		this.vehicleInfo = vehicleInfo;
	}
}