package com.rescuenet.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Prayash Rawal
 */
/**
 * VehicleModel represents a vehicle entity in the RescueNet application. It
 * stores vehicle details such as serial number, brand, model, type, status,
 * manufactured date, image path, description, and price for vehicle management
 * and reservation purposes.
 */
public class VehicleModel {

	private int vehicleId;
	private String serialNumber;
	private String brandName;
	private String model;
	private String type;
	private String status;
	private LocalDate manufacturedDate;
	private String imagePath;
	private String description;
	private BigDecimal price;

	/**
	 * Default constructor for VehicleModel.
	 */
	public VehicleModel() {
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
	 * Gets the serial number.
	 *
	 * @return the serial number
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Sets the serial number.
	 *
	 * @param serialNumber the serial number to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * Gets the brand name.
	 *
	 * @return the brand name
	 */
	public String getBrandName() {
		return brandName;
	}

	/**
	 * Sets the brand name.
	 *
	 * @param brandName the brand name to set
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	/**
	 * Gets the model.
	 *
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * Sets the model.
	 *
	 * @param model the model to set
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * Gets the vehicle type.
	 *
	 * @return the vehicle type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the vehicle type.
	 *
	 * @param type the vehicle type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the vehicle status.
	 *
	 * @return the vehicle status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the vehicle status.
	 *
	 * @param status the vehicle status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Gets the manufactured date.
	 *
	 * @return the manufactured date
	 */
	public LocalDate getManufacturedDate() {
		return manufacturedDate;
	}

	/**
	 * Sets the manufactured date.
	 *
	 * @param manufacturedDate the manufactured date to set
	 */
	public void setManufacturedDate(LocalDate manufacturedDate) {
		this.manufacturedDate = manufacturedDate;
	}

	/**
	 * Gets the image path.
	 *
	 * @return the image path
	 */
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * Sets the image path.
	 *
	 * @param imagePath the image path to set
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the price.
	 *
	 * @return the price
	 */
	public BigDecimal getPrice() {
		return price;
	}

	/**
	 * Sets the price.
	 *
	 * @param price the price to set
	 */
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}