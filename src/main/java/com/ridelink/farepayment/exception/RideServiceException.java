package com.ridelink.farepayment.exception;

public class RideServiceException extends FarePaymentException {
    public RideServiceException(String message) { super("RIDE_SERVICE_ERROR", message, 502); }
}
