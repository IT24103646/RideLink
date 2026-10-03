package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRideRequest(
        @NotBlank(message = "Pickup is required")
        @Size(min = 2, max = 200, message = "Pickup must be between 2 and 200 characters")
        String pickup,

        @NotBlank(message = "Destination is required")
        @Size(min = 2, max = 200, message = "Destination must be between 2 and 200 characters")
        String destination) {
}
