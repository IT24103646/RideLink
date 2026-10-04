package com.ridelink.farepayment.client;

import java.time.LocalDateTime;

public record RideResponse(
        String id,
        String passengerId,
        String driverId,
        String pickup,
        String destination,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime assignedAt,
        LocalDateTime acceptedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt) {
}
