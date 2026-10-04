package com.ridelink.farepayment.exception;

public abstract class FarePaymentException extends RuntimeException {
    private final String code;
    private final int status;

    protected FarePaymentException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public int getStatus() { return status; }
}
