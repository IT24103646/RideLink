package com.ridelink.ride.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.ridelink.ride.client.AvailableDriverResponse;
import com.ridelink.ride.client.DriverClient;
import com.ridelink.ride.client.DriverProfileResponse;
import com.ridelink.ride.dto.CreateRideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.exception.DriverServiceUnavailableException;
import com.ridelink.ride.exception.ForbiddenRideAccessException;
import com.ridelink.ride.exception.InvalidRideTransitionException;
import com.ridelink.ride.exception.NoAvailableDriverException;
import com.ridelink.ride.exception.RideNotFoundException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.repository.RideRepository;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final DriverClient driverClient;

    public RideService(RideRepository rideRepository, DriverClient driverClient) {
        this.rideRepository = rideRepository;
        this.driverClient = driverClient;
    }

    public Ride createRide(String passengerId, CreateRideRequest request) {
        return createRide(passengerId, request, null);
    }

    public Ride createRide(String passengerId, CreateRideRequest request, String bearerToken) {
        List<AvailableDriverResponse> availableDrivers = bearerToken == null
                ? driverClient.getAvailableDrivers()
                : driverClient.getAvailableDrivers(bearerToken);
        if (availableDrivers == null || availableDrivers.isEmpty()) {
            throw new NoAvailableDriverException("No available driver is currently available. Please try again later.");
        }

        AvailableDriverResponse selected = availableDrivers.stream()
                .sorted(Comparator.comparing(AvailableDriverResponse::id))
                .findFirst()
                .orElseThrow(() -> new NoAvailableDriverException("No available driver is currently available. Please try again later."));

        try {
            driverClient.markDriverUnavailable(selected.id());
        } catch (DriverServiceUnavailableException exception) {
            throw exception;
        }

        Ride ride = new Ride();
        ride.setPassengerId(passengerId);
        ride.setDriverId(selected.id());
        ride.setPickup(normalizeText(request.pickup()));
        ride.setDestination(normalizeText(request.destination()));
        ride.setStatus(RideStatus.ASSIGNED);
        LocalDateTime now = LocalDateTime.now();
        ride.setRequestedAt(now);
        ride.setAssignedAt(now);
        return rideRepository.save(ride);
    }

    public Ride getRideById(String rideId, String accountId, String role, String bearerToken) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + rideId));

        if ("ROLE_ADMIN".equals(role)) {
            return ride;
        }
        if ("ROLE_PASSENGER".equals(role) && Objects.equals(ride.getPassengerId(), accountId)) {
            return ride;
        }
        if ("ROLE_DRIVER".equals(role) && isAssignedDriver(ride, bearerToken)) {
            return ride;
        }

        throw new ForbiddenRideAccessException("You are not allowed to access this ride.");
    }

    public List<Ride> getPassengerRides(String passengerId) {
        return rideRepository.findByPassengerId(passengerId).stream()
                .sorted(Comparator.comparing(Ride::getRequestedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Ride> getAssignedDriverRides(String bearerToken) {
        String driverId = resolveDriverIdFromToken(bearerToken);
        return rideRepository.findByDriverId(driverId).stream()
                .sorted(Comparator.comparing(Ride::getRequestedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Ride acceptRide(String rideId, String bearerToken) {
        Ride ride = findRideOrThrow(rideId);
        ensureDriverOwnsRide(ride, bearerToken);
        ensureTransition(ride, RideStatus.ASSIGNED, RideStatus.ACCEPTED, "accept");
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());
        return rideRepository.save(ride);
    }

    public Ride startRide(String rideId, String bearerToken) {
        Ride ride = findRideOrThrow(rideId);
        ensureDriverOwnsRide(ride, bearerToken);
        ensureTransition(ride, RideStatus.ACCEPTED, RideStatus.IN_PROGRESS, "start");
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        return rideRepository.save(ride);
    }

    public Ride completeRide(String rideId, String bearerToken) {
        Ride ride = findRideOrThrow(rideId);
        ensureDriverOwnsRide(ride, bearerToken);
        ensureTransition(ride, RideStatus.IN_PROGRESS, RideStatus.COMPLETED, "complete");
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        return rideRepository.save(ride);
    }

    public Ride cancelRide(String rideId, String currentUserId, String role, String bearerToken) {
        Ride ride = findRideOrThrow(rideId);

        if ("ROLE_ADMIN".equals(role)) {
            ensureCancellationAllowed(ride);
        } else if ("ROLE_PASSENGER".equals(role)) {
            if (!Objects.equals(ride.getPassengerId(), currentUserId)) {
                throw new ForbiddenRideAccessException("You are not allowed to cancel this ride.");
            }
            ensureCancellationAllowed(ride);
        } else if ("ROLE_DRIVER".equals(role)) {
            ensureDriverOwnsRide(ride, bearerToken);
            if (ride.getStatus() != RideStatus.ASSIGNED && ride.getStatus() != RideStatus.ACCEPTED) {
                throw new InvalidRideTransitionException("Cannot cancel ride from " + ride.getStatus() + " state.");
            }
        } else {
            throw new AccessDeniedException("Unsupported role");
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        return rideRepository.save(ride);
    }

    private void ensureCancellationAllowed(Ride ride) {
        if (ride.getStatus() != RideStatus.REQUESTED
                && ride.getStatus() != RideStatus.ASSIGNED
                && ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideTransitionException("Cannot cancel ride from " + ride.getStatus() + " state.");
        }
    }

    private Ride findRideOrThrow(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + rideId));
    }

    private void ensureTransition(Ride ride, RideStatus requiredCurrentStatus, RideStatus targetStatus, String operation) {
        if (ride.getStatus() != requiredCurrentStatus) {
            throw new InvalidRideTransitionException("Cannot " + operation + " ride from " + ride.getStatus() + " state.");
        }
    }

    private void ensureDriverOwnsRide(Ride ride, String bearerToken) {
        String driverId = resolveDriverIdFromToken(bearerToken);
        if (!Objects.equals(ride.getDriverId(), driverId)) {
            throw new ForbiddenRideAccessException("You are not allowed to update this ride.");
        }
    }

    private boolean isAssignedDriver(Ride ride, String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return false;
        }
        try {
            String driverId = resolveDriverIdFromToken(bearerToken);
            return Objects.equals(ride.getDriverId(), driverId);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String resolveDriverIdFromToken(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new ForbiddenRideAccessException("Driver authentication is required.");
        }
        DriverProfileResponse driverProfile = driverClient.getCurrentDriverProfile(bearerToken);
        if (driverProfile == null || driverProfile.id() == null || driverProfile.id().isBlank()) {
            throw new ForbiddenRideAccessException("Driver identity could not be resolved.");
        }
        return driverProfile.id();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    public RideResponse toResponse(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getPassengerId(),
                ride.getDriverId(),
                ride.getPickup(),
                ride.getDestination(),
                ride.getStatus(),
                ride.getRequestedAt(),
                ride.getAssignedAt(),
                ride.getAcceptedAt(),
                ride.getStartedAt(),
                ride.getCompletedAt(),
                ride.getCancelledAt());
    }
}
