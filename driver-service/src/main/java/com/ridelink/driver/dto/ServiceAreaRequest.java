package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ServiceAreaRequest(
        @NotBlank @Size(max = 100) String city,
        @Positive double radiusKm) {
}
