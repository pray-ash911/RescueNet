package com.rescuenet.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VehicleModel {

    // --- ADD THIS FIELD ---
    private int vehicleId;
    // --- END ADD ---

    private String serialNumber;
    private String brandName;
    private String model;
    private String type;
    private String status;
    private LocalDate manufacturedDate;
    private String imagePath;
    private String description;
    private BigDecimal price;

    // Constructors (keep existing ones or add if needed)
    public VehicleModel() {
    }

    // Existing getters and setters...

    // --- ADD GETTER AND SETTER for vehicleId ---
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }
    // --- END ADD ---

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getManufacturedDate() {
        return manufacturedDate;
    }

    public void setManufacturedDate(LocalDate manufacturedDate) {
        this.manufacturedDate = manufacturedDate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Optional: toString() method, etc.
}