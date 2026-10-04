package com.ridelink.farepayment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ridelink.farepayment.model.PaymentMethod;
import com.ridelink.farepayment.model.PaymentStatus;

public record PaymentResponse(
        String paymentId,
        String rideId,
        String passengerId,
        String driverId,
        String pickup,
        String destination,
        BigDecimal distanceKm,
        BigDecimal durationMinutes,
        BigDecimal baseFare,
        BigDecimal distanceCharge,
        BigDecimal durationCharge,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String transactionReference,
        String receiptNumber,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime paidAt) {
}
