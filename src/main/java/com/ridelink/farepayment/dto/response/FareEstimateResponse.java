package com.ridelink.farepayment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FareEstimateResponse(
        String estimateId,
        String pickup,
        String destination,
        BigDecimal estimatedDistanceKm,
        BigDecimal estimatedDurationMinutes,
        BigDecimal baseFare,
        BigDecimal distanceCharge,
        BigDecimal durationCharge,
        BigDecimal estimatedTotal,
        LocalDateTime createdAt) {
}
