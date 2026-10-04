package com.ridelink.ride.exception;

public class ForbiddenRideAccessException extends RuntimeException {
    public ForbiddenRideAccessException(String message) {
        super(message);
    }
}
