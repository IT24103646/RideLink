package com.ridelink.account.dto;

public record LoginResponse(String token, AccountResponse account) {
}