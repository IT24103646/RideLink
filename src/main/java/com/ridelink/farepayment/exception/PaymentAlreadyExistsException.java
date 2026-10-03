package com.ridelink.farepayment.exception;

public class PaymentAlreadyExistsException extends FarePaymentException {
    public PaymentAlreadyExistsException() { super("PAYMENT_ALREADY_EXISTS", "A successful payment already exists for this ride.", 409); }
}
