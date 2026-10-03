package com.ridelink.ride.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AvailableDriverResponse(String id, String name) {
}
