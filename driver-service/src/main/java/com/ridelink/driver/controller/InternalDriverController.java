package com.ridelink.driver.controller;

import com.ridelink.driver.dto.AvailabilityRequest;
import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.exception.InvalidServiceKeyException;
import com.ridelink.driver.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/drivers")
public class InternalDriverController {
    private static final String SERVICE_KEY_HEADER = "X-Service-Key";

    private final DriverService driverService;
    private final String internalServiceKey;

    public InternalDriverController(
            DriverService driverService,
            @Value("${driver.service.internal-key:}") String internalServiceKey) {
        this.driverService = driverService;
        this.internalServiceKey = internalServiceKey;
    }

    @PatchMapping("/{id}/availability/internal")
    public DriverResponse updateAvailability(
            @PathVariable String id,
            @Valid @RequestBody AvailabilityRequest request,
            @RequestHeader(value = SERVICE_KEY_HEADER, required = false) String serviceKey) {
        if (!hasValidServiceKey(serviceKey)) {
            throw new InvalidServiceKeyException("A valid service credential is required");
        }
        return driverService.updateAvailabilityById(id, request);
    }

    private boolean hasValidServiceKey(String serviceKey) {
        if (internalServiceKey == null || internalServiceKey.isBlank()
                || serviceKey == null || serviceKey.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                internalServiceKey.getBytes(StandardCharsets.UTF_8),
                serviceKey.getBytes(StandardCharsets.UTF_8));
    }
}