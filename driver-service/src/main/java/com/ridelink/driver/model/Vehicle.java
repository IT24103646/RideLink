package com.ridelink.driver.model;

public class Vehicle {
    private VehicleType vehicleType;
    private String brand;
    private String model;
    private String registrationNumber;
    private String color;

    protected Vehicle() {
    }

    public Vehicle(VehicleType vehicleType, String brand, String model, String registrationNumber, String color) {
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.model = model;
        this.registrationNumber = registrationNumber;
        this.color = color;
    }

    public VehicleType getVehicleType() { return vehicleType; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getColor() { return color; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public void setColor(String color) { this.color = color; }
}
