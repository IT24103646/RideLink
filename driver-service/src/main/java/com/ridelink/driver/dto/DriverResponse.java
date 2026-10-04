package com.ridelink.driver.dto;

import com.ridelink.driver.model.Availability;

public record DriverResponse(
        String id,
        String accountId,
        String name,
        String phone,
        VehicleResponse vehicle,
        Availability availability,
        LocationResponse location,
        ServiceAreaResponse serviceArea) {
}
