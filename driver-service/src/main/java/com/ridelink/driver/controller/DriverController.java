package com.ridelink.driver.controller;

import com.ridelink.driver.dto.AvailabilityRequest;
import com.ridelink.driver.dto.AvailableDriverResponse;
import com.ridelink.driver.dto.CreateDriverRequest;
import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.dto.LocationRequest;
import com.ridelink.driver.dto.ServiceAreaRequest;
import com.ridelink.driver.dto.UpdateDriverRequest;
import com.ridelink.driver.dto.VehicleRequest;
import com.ridelink.driver.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@PreAuthorize("hasAuthority('ROLE_DRIVER') or hasAuthority('ROLE_ADMIN')")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<DriverResponse> create(
            @Valid @RequestBody CreateDriverRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverService.create(
                        authentication.getName(),
                        request
                ));
    }

    /**
     * Returns currently ONLINE drivers.
     *
     * Ride Service uses this endpoint during
     * driver assignment.
     */
    @GetMapping("/available")
    @PreAuthorize("""
            hasAuthority('ROLE_PASSENGER')
            or hasAuthority('ROLE_DRIVER')
            or hasAuthority('ROLE_ADMIN')
            """)
    public List<AvailableDriverResponse> findAvailableDrivers() {
        return driverService.findAvailableDrivers();
    }

    @GetMapping("/{id}")
    public DriverResponse findById(
            @PathVariable String id,
            Authentication authentication) {

        return driverService.findByIdForRequester(
                id,
                authentication.getName(),
                isAdmin(authentication)
        );
    }

    @GetMapping("/me")
    public DriverResponse me(Authentication authentication) {
        return driverService.findByAccountId(
                authentication.getName()
        );
    }

    @PutMapping("/me")
    public DriverResponse update(
            @Valid @RequestBody UpdateDriverRequest request,
            Authentication authentication) {

        return driverService.updateOwn(
                authentication.getName(),
                request
        );
    }

    @PutMapping("/me/vehicle")
    public DriverResponse updateVehicle(
            @Valid @RequestBody VehicleRequest request,
            Authentication authentication) {

        return driverService.updateVehicle(
                authentication.getName(),
                request
        );
    }

    /**
     * Driver updates their own availability.
     */
    @PatchMapping("/me/availability")
    public DriverResponse updateAvailability(
            @Valid @RequestBody AvailabilityRequest request,
            Authentication authentication) {

        return driverService.updateAvailability(
                authentication.getName(),
                request
        );
    }

    /**
     * Controlled endpoint for updating a specific
     * driver's availability.
     *
     * Currently restricted to ADMIN.
     */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public DriverResponse updateAvailabilityById(
            @PathVariable String id,
            @Valid @RequestBody AvailabilityRequest request) {

        return driverService.updateAvailabilityById(
                id,
                request
        );
    }

    @PatchMapping("/me/location")
    public DriverResponse updateLocation(
            @Valid @RequestBody LocationRequest request,
            Authentication authentication) {

        return driverService.updateLocation(
                authentication.getName(),
                request
        );
    }

    @PatchMapping("/me/service-area")
    public DriverResponse updateServiceArea(
            @Valid @RequestBody ServiceAreaRequest request,
            Authentication authentication) {

        return driverService.updateServiceArea(
                authentication.getName(),
                request
        );
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"));
    }
}