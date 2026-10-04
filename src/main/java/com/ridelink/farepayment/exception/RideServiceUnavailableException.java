package com.ridelink.farepayment.exception;

public class RideServiceUnavailableException extends FarePaymentException {
    public RideServiceUnavailableException(String message) { super("RIDE_SERVICE_UNAVAILABLE", message, 503); }
}
