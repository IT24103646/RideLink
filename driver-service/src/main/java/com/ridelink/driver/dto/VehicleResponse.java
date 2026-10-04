package com.ridelink.driver.dto;

import com.ridelink.driver.model.VehicleType;

public record VehicleResponse(VehicleType vehicleType, String brand, String model, String registrationNumber, String color) {
}
