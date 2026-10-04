package com.ridelink.farepayment.exception;

public class RideNotFoundException extends FarePaymentException {
    public RideNotFoundException(String message) { super("RIDE_NOT_FOUND", message, 404); }
}
