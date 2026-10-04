package com.ridelink.ride.client;

public record DriverProfileResponse(
        String id,
        String accountId,
        String name,
        String phone,
        VehicleResponse vehicle,
        String availability,
        LocationResponse location,
        ServiceAreaResponse serviceArea) {
}

record VehicleResponse(String vehicleType, String brand, String model, String registrationNumber, String color) {
}

record LocationResponse(Double latitude, Double longitude) {
}

record ServiceAreaResponse(String city, Double radiusKm) {
}
