package com.ridelink.farepayment.exception;

public class ReceiptNotAvailableException extends FarePaymentException {
    public ReceiptNotAvailableException() { super("RECEIPT_NOT_AVAILABLE", "A receipt is not available for this payment.", 409); }
}
