package com.ridelink.farepayment.exception;

public class EstimateNotFoundException extends FarePaymentException {
    public EstimateNotFoundException() { super("ESTIMATE_NOT_FOUND", "Fare estimate not found.", 404); }
}
