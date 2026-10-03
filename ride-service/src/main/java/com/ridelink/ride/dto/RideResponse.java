package com.ridelink.ride.dto;

import java.time.LocalDateTime;

import com.ridelink.ride.model.RideStatus;

public record RideResponse(
        String id,
        String passengerId,
        String driverId,
        String pickup,
        String destination,
        RideStatus status,
        LocalDateTime requestedAt,
        LocalDateTime assignedAt,
        LocalDateTime acceptedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt) {
}
