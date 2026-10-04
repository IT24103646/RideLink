package com.ridelink.farepayment.exception;

public class PaymentNotAllowedException extends FarePaymentException {
    public PaymentNotAllowedException() { super("PAYMENT_NOT_ALLOWED_FOR_RIDE_STATE", "Payment is allowed only for completed rides.", 409); }
}
