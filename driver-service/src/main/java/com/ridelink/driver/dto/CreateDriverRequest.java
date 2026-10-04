package com.ridelink.driver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDriverRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 32) String phone,
        @NotNull @Valid VehicleRequest vehicle,
        @Valid LocationRequest location,
        @Valid ServiceAreaRequest serviceArea) {
}
