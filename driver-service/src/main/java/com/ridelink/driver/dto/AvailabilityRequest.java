package com.ridelink.driver.dto;

import com.ridelink.driver.model.Availability;
import jakarta.validation.constraints.NotNull;

public record AvailabilityRequest(@NotNull Availability availability) {
}
