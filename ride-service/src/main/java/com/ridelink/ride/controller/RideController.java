package com.ridelink.ride.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.service.RideService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // =========================================================
    // PASSENGER - CREATE RIDE
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PASSENGER')")
    public ResponseEntity<RideResponse> createRide(
            @Valid @RequestBody CreateRideRequest request,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Ride ride = rideService.createRide(
                authentication.getName(),
                request,
                authorization
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(rideService.toResponse(ride));
    }

    // =========================================================
    // PASSENGER - MY RIDES
    // =========================================================

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_PASSENGER')")
    public List<RideResponse> getMyRides(
            Authentication authentication) {

        return rideService
                .getPassengerRides(authentication.getName())
                .stream()
                .map(rideService::toResponse)
                .toList();
    }

    // =========================================================
    // PASSENGER / DRIVER / ADMIN - GET RIDE
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("""
            hasAuthority('ROLE_PASSENGER')
            or hasAuthority('ROLE_DRIVER')
            or hasAuthority('ROLE_ADMIN')
            """)
    public RideResponse getRideById(
            @PathVariable String id,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");

        Ride ride = rideService.getRideById(
                id,
                authentication.getName(),
                role,
                authorization
        );

        return rideService.toResponse(ride);
    }

    // =========================================================
    // PASSENGER / DRIVER / ADMIN - CANCEL RIDE
    // =========================================================

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("""
            hasAuthority('ROLE_PASSENGER')
            or hasAuthority('ROLE_DRIVER')
            or hasAuthority('ROLE_ADMIN')
            """)
    public RideResponse cancelRide(
            @PathVariable String id,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");

        Ride ride = rideService.cancelRide(
                id,
                authentication.getName(),
                role,
                authorization
        );

        return rideService.toResponse(ride);
    }

    // =========================================================
    // DRIVER - ASSIGNED RIDES
    // =========================================================

    @GetMapping("/assigned")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public List<RideResponse> getAssignedDriverRides(
            @RequestHeader("Authorization") String authorization) {

        return rideService
                .getAssignedDriverRides(authorization)
                .stream()
                .map(rideService::toResponse)
                .toList();
    }

    // =========================================================
    // DRIVER - ACCEPT
    // =========================================================

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public RideResponse acceptRide(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {

        return rideService.toResponse(
                rideService.acceptRide(id, authorization)
        );
    }

    // =========================================================
    // DRIVER - START
    // =========================================================

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public RideResponse startRide(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {

        return rideService.toResponse(
                rideService.startRide(id, authorization)
        );
    }

    // =========================================================
    // DRIVER - COMPLETE
    // =========================================================

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ROLE_DRIVER')")
    public RideResponse completeRide(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {

        return rideService.toResponse(
                rideService.completeRide(id, authorization)
        );
    }
}