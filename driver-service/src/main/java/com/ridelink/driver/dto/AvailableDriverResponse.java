package com.ridelink.driver.dto;

public record AvailableDriverResponse(
        String id,
        String name,
        VehicleResponse vehicle,
        LocationResponse location,
        ServiceAreaResponse serviceArea) {
}
