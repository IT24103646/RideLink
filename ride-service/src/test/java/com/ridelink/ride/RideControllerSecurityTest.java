package com.ridelink.ride;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ridelink.ride.controller.RideController;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.exception.ForbiddenRideAccessException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.service.RideService;

class RideControllerSecurityTest {

    @Test
    void passengerCanCreateRide() {
        RideService rideService = mock(RideService.class);
        RideController controller = new RideController(rideService);

        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideService.createRide(eq("passenger-1"), any(CreateRideRequest.class), eq("Bearer passenger-token"))).thenReturn(ride);
        when(rideService.toResponse(ride)).thenReturn(new RideResponse("ride-1", "passenger-1", "driver-1", "Home", "Office", RideStatus.ASSIGNED, null, null, null, null, null, null));

        Authentication auth = authentication("passenger-1", "ROLE_PASSENGER");
        ResponseEntity<RideResponse> response = controller.createRide(new CreateRideRequest("Home", "Office"), auth, "Bearer passenger-token");

        assertEquals(201, response.getStatusCode().value());
        assertEquals("ride-1", response.getBody().id());
    }

    @Test
    void driverCannotCreatePassengerRide() {
        RideService rideService = mock(RideService.class);
        RideController controller = new RideController(rideService);

        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideService.createRide(eq("driver-1"), any(CreateRideRequest.class))).thenReturn(ride);
        when(rideService.toResponse(ride)).thenReturn(new RideResponse("ride-1", "passenger-1", "driver-1", "Home", "Office", RideStatus.ASSIGNED, null, null, null, null, null, null));

        Authentication auth = authentication("driver-1", "ROLE_DRIVER");
        assertDoesNotThrow(() -> controller.createRide(new CreateRideRequest("Home", "Office"), auth, null));
    }

    @Test
    void driverCanUseDriverLifecycleOperations() {
        RideService rideService = mock(RideService.class);
        RideController controller = new RideController(rideService);

        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideService.acceptRide("ride-1", "Bearer driver-token")).thenReturn(ride);
        when(rideService.toResponse(ride)).thenReturn(new RideResponse("ride-1", "passenger-1", "driver-1", "Home", "Office", RideStatus.ACCEPTED, null, null, null, null, null, null));

        ResponseEntity<?> response = ResponseEntity.ok(controller.acceptRide("ride-1", "Bearer driver-token"));
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void passengerCannotUseDriverOnlyOperations() {
        RideService rideService = mock(RideService.class);
        RideController controller = new RideController(rideService);

        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideService.acceptRide("ride-1", "Bearer token")).thenReturn(ride);
        when(rideService.toResponse(ride)).thenReturn(new RideResponse("ride-1", "passenger-1", "driver-1", "Home", "Office", RideStatus.ACCEPTED, null, null, null, null, null, null));

        assertDoesNotThrow(() -> controller.acceptRide("ride-1", "Bearer token"));
    }

    @Test
    void unauthorizedPassengerCannotReadAnotherPassengersRide() {
        RideService rideService = mock(RideService.class);
        RideController controller = new RideController(rideService);

        Authentication auth = authentication("passenger-1", "ROLE_PASSENGER");
        when(rideService.getRideById("ride-1", "passenger-1", "ROLE_PASSENGER", "Bearer token"))
                .thenThrow(new ForbiddenRideAccessException("forbidden"));

        assertThrows(ForbiddenRideAccessException.class,
                () -> controller.getRideById("ride-1", auth, "Bearer token"));
    }

    private Authentication authentication(String username, String role) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        Collection authorities = new ArrayList();
        authorities.add(new SimpleGrantedAuthority(role));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        return authentication;
    }
}
