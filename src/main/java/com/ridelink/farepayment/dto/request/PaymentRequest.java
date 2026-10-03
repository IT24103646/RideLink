package com.ridelink.farepayment.dto.request;

import java.math.BigDecimal;

import com.ridelink.farepayment.model.PaymentMethod;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotBlank String rideId,
        @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "1000") @Digits(integer = 4, fraction = 2)
        BigDecimal actualDistanceKm,
        @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "1440") @Digits(integer = 4, fraction = 2)
        BigDecimal actualDurationMinutes,
        @NotNull PaymentMethod paymentMethod,
        boolean simulateFailure) {
}
