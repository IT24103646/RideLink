package com.ridelink.account.exception;

public class ForbiddenAccountAccessException extends RuntimeException {
    public ForbiddenAccountAccessException(String message) { super(message); }
}