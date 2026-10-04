package com.ridelink.driver.dto;

import com.ridelink.driver.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotNull VehicleType vehicleType,
        @NotBlank @Size(max = 50) String brand,
        @NotBlank @Size(max = 50) String model,
        @NotBlank @Size(max = 32) String registrationNumber,
        @NotBlank @Size(max = 30) String color) {
}
