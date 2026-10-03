package com.ridelink.farepayment.exception;

public class SimulatedPaymentFailedException extends FarePaymentException {
    public SimulatedPaymentFailedException() { super("SIMULATED_PAYMENT_FAILED", "The simulated payment failed.", 402); }
}
