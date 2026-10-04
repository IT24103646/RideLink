package com.ridelink.farepayment.exception;

public class ForbiddenPaymentAccessException extends FarePaymentException {
    public ForbiddenPaymentAccessException() { super("FORBIDDEN_PAYMENT_ACCESS", "You are not allowed to access this resource.", 403); }
}
