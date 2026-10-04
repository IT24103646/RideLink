package com.ridelink.farepayment.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FareEstimateRequest(
        @NotBlank @Size(max = 200) String pickup,
        @NotBlank @Size(max = 200) String destination,
        @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "1000") @Digits(integer = 4, fraction = 2)
        BigDecimal estimatedDistanceKm,
        @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "1440") @Digits(integer = 4, fraction = 2)
        BigDecimal estimatedDurationMinutes) {
}
