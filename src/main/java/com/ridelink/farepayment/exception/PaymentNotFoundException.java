package com.ridelink.farepayment.exception;

public class PaymentNotFoundException extends FarePaymentException {
    public PaymentNotFoundException() { super("PAYMENT_NOT_FOUND", "Payment not found.", 404); }
}
