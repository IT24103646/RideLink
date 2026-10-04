package com.ridelink.driver.model;

public class ServiceArea {
    private String city;
    private double radiusKm;

    protected ServiceArea() {
    }

    public ServiceArea(String city, double radiusKm) {
        this.city = city;
        this.radiusKm = radiusKm;
    }

    public String getCity() { return city; }
    public double getRadiusKm() { return radiusKm; }
    public void setCity(String city) { this.city = city; }
    public void setRadiusKm(double radiusKm) { this.radiusKm = radiusKm; }
}
