package com.ridelink.ride;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.ridelink.ride.client.AvailableDriverResponse;
import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.client.DriverProfileResponse;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.exception.DriverServiceUnavailableException;
import com.ridelink.ride.exception.DriverServiceAuthorizationException;
import com.ridelink.ride.exception.ForbiddenRideAccessException;
import com.ridelink.ride.exception.InvalidRideTransitionException;
import com.ridelink.ride.exception.NoAvailableDriverException;
import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.service.RideService;

class RideServiceTest {

    private RideRepository rideRepository;
    private DriverClient driverClient;
    private RideService rideService;

    @BeforeEach
    void setUp() {
        rideRepository = mock(RideRepository.class);
        driverClient = mock(DriverClient.class);
        rideService = new RideService(rideRepository, driverClient);
    }

    @Test
    void createRideAssignsFirstAvailableDriver() {
        when(driverClient.getAvailableDrivers()).thenReturn(List.of(new AvailableDriverResponse("driver-1", "Ada")));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride ride = rideService.createRide("passenger-1", new CreateRideRequest("Home", "Office"));

        assertEquals("passenger-1", ride.getPassengerId());
        assertEquals("driver-1", ride.getDriverId());
        assertEquals(RideStatus.ASSIGNED, ride.getStatus());
        assertNotNull(ride.getAssignedAt());
        verify(driverClient).markDriverUnavailable("driver-1");
    }

    @Test
    void createRideFailsWhenNoDriversAvailable() {
        when(driverClient.getAvailableDrivers()).thenReturn(List.of());

        assertThrows(NoAvailableDriverException.class,
                () -> rideService.createRide("passenger-1", new CreateRideRequest("Home", "Office")));
    }

    @Test
    void createRideFailsWhenDriverServiceUnavailable() {
        when(driverClient.getAvailableDrivers()).thenReturn(List.of(new AvailableDriverResponse("driver-1", "Ada")));
        doThrow(new DriverServiceUnavailableException("downstream unavailable")).when(driverClient).markDriverUnavailable("driver-1");

        assertThrows(DriverServiceUnavailableException.class,
                () -> rideService.createRide("passenger-1", new CreateRideRequest("Home", "Office")));
    }

            @Test
            void createRideUsesPassengerTokenForAvailableDriverLookup() {
            when(driverClient.getAvailableDrivers("Bearer passenger-token"))
                .thenReturn(List.of(new AvailableDriverResponse("driver-1", "Ada")));
            when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

            rideService.createRide("passenger-1", new CreateRideRequest("Home", "Office"), "Bearer passenger-token");

            verify(driverClient).getAvailableDrivers("Bearer passenger-token");
            verify(driverClient).markDriverUnavailable("driver-1");
            }

            @Test
            void createRidePropagatesUnauthorizedInternalAssignment() {
            when(driverClient.getAvailableDrivers()).thenReturn(List.of(new AvailableDriverResponse("driver-1", "Ada")));
            doThrow(new DriverServiceAuthorizationException("rejected"))
                .when(driverClient).markDriverUnavailable("driver-1");

            assertThrows(DriverServiceAuthorizationException.class,
                () -> rideService.createRide("passenger-1", new CreateRideRequest("Home", "Office")));
            verify(rideRepository, never()).save(any(Ride.class));
            }

    @Test
    void getRideByIdThrowsWhenRideMissing() {
        when(rideRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () -> rideService.getRideById("missing", "passenger-1", "ROLE_PASSENGER", null));
    }

    @Test
    void acceptRideTransitionsAssignedToAccepted() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setRequestedAt(LocalDateTime.now());
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token")).thenReturn(new DriverProfileResponse("driver-1", "account-1", "Ada", "123", null, null, null, null));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride updated = rideService.acceptRide("ride-1", "Bearer token");

        assertEquals(RideStatus.ACCEPTED, updated.getStatus());
        assertNotNull(updated.getAcceptedAt());
    }

    @Test
    void invalidAcceptTransitionIsRejected() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token")).thenReturn(new DriverProfileResponse("driver-1", "account-1", "Ada", "123", null, null, null, null));

        assertThrows(InvalidRideTransitionException.class, () -> rideService.acceptRide("ride-1", "Bearer token"));
    }

    @Test
    void startRideTransitionsAcceptedToInProgress() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token")).thenReturn(new DriverProfileResponse("driver-1", "account-1", "Ada", "123", null, null, null, null));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride updated = rideService.startRide("ride-1", "Bearer token");

        assertEquals(RideStatus.IN_PROGRESS, updated.getStatus());
        assertNotNull(updated.getStartedAt());
    }

    @Test
    void invalidStartTransitionIsRejected() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token")).thenReturn(new DriverProfileResponse("driver-1", "account-1", "Ada", "123", null, null, null, null));

        assertThrows(InvalidRideTransitionException.class, () -> rideService.startRide("ride-1", "Bearer token"));
    }

    @Test
    void completeRideTransitionsInProgressToCompleted() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.IN_PROGRESS);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token")).thenReturn(new DriverProfileResponse("driver-1", "account-1", "Ada", "123", null, null, null, null));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride updated = rideService.completeRide("ride-1", "Bearer token");

        assertEquals(RideStatus.COMPLETED, updated.getStatus());
        assertNotNull(updated.getCompletedAt());
    }

    @Test
    void invalidCompleteTransitionIsRejected() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token")).thenReturn(new DriverProfileResponse("driver-1", "account-1", "Ada", "123", null, null, null, null));

        assertThrows(InvalidRideTransitionException.class, () -> rideService.completeRide("ride-1", "Bearer token"));
    }

    @Test
    void passengerCanCancelRequestedRide() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride updated = rideService.cancelRide("ride-1", "passenger-1", "ROLE_PASSENGER", null);

        assertEquals(RideStatus.CANCELLED, updated.getStatus());
        assertNotNull(updated.getCancelledAt());
    }

    @Test
    void passengerCanCancelAssignedRide() {
        assertPassengerCanCancelFrom(RideStatus.ASSIGNED);
    }

    @Test
    void passengerCanCancelAcceptedRide() {
        assertPassengerCanCancelFrom(RideStatus.ACCEPTED);
    }

    @Test
    void passengerCannotCancelAnotherPassengersRide() {
        Ride ride = rideWithStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));

        assertThrows(ForbiddenRideAccessException.class,
                () -> rideService.cancelRide("ride-1", "passenger-2", "ROLE_PASSENGER", null));
    }

    @Test
    void driverCanCancelAssignedRide() {
        assertDriverCanCancelFrom(RideStatus.ASSIGNED);
    }

    @Test
    void driverCanCancelAcceptedRide() {
        assertDriverCanCancelFrom(RideStatus.ACCEPTED);
    }

    @Test
    void unrelatedDriverCannotCancelRide() {
        Ride ride = rideWithStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token"))
                .thenReturn(new DriverProfileResponse("driver-2", "account-2", "Bob", "456", null, null, null, null));

        assertThrows(ForbiddenRideAccessException.class,
                () -> rideService.cancelRide("ride-1", "account-2", "ROLE_DRIVER", "Bearer token"));
    }

    @Test
    void passengerCannotCancelRideAfterItStarts() {
        assertThrowsInvalidCancellation(RideStatus.IN_PROGRESS);
    }

    @Test
    void unrelatedPassengerCannotAccessRide() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));

        assertThrows(RuntimeException.class, () -> rideService.getRideById("ride-1", "passenger-2", "ROLE_PASSENGER", null));
    }

    @Test
    void assignedDriverCanAccessAssignedRide() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token")).thenReturn(new DriverProfileResponse("driver-1", "account-1", "Ada", "123", null, null, null, null));

        Ride result = rideService.getRideById("ride-1", "account-1", "ROLE_DRIVER", "Bearer token");

        assertEquals("ride-1", result.getId());
    }

    @Test
    void unrelatedDriverCannotUpdateRide() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token")).thenReturn(new DriverProfileResponse("driver-2", "account-2", "Bob", "456", null, null, null, null));

        assertThrows(RuntimeException.class, () -> rideService.acceptRide("ride-1", "Bearer token"));
    }

    @Test
    void cancellationFromInvalidStateIsRejected() {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideTransitionException.class, () -> rideService.cancelRide("ride-1", "passenger-1", "ROLE_PASSENGER", null));
    }

    private void assertPassengerCanCancelFrom(RideStatus status) {
        Ride ride = rideWithStatus(status);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride updated = rideService.cancelRide("ride-1", "passenger-1", "ROLE_PASSENGER", null);

        assertEquals(RideStatus.CANCELLED, updated.getStatus());
        assertNotNull(updated.getCancelledAt());
    }

    private void assertDriverCanCancelFrom(RideStatus status) {
        Ride ride = rideWithStatus(status);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));
        when(driverClient.getCurrentDriverProfile("Bearer token"))
                .thenReturn(new DriverProfileResponse("driver-1", "account-1", "Ada", "123", null, null, null, null));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ride updated = rideService.cancelRide("ride-1", "account-1", "ROLE_DRIVER", "Bearer token");

        assertEquals(RideStatus.CANCELLED, updated.getStatus());
    }

    private void assertThrowsInvalidCancellation(RideStatus status) {
        Ride ride = rideWithStatus(status);
        when(rideRepository.findById("ride-1")).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideTransitionException.class,
                () -> rideService.cancelRide("ride-1", "passenger-1", "ROLE_PASSENGER", null));
    }

    private Ride rideWithStatus(RideStatus status) {
        Ride ride = new Ride();
        ride.setId("ride-1");
        ride.setPassengerId("passenger-1");
        ride.setDriverId("driver-1");
        ride.setStatus(status);
        return ride;
    }
}
