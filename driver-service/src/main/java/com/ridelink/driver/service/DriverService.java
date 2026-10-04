package com.ridelink.driver.service;

import com.ridelink.driver.dto.AvailabilityRequest;
import com.ridelink.driver.dto.AvailableDriverResponse;
import com.ridelink.driver.dto.CreateDriverRequest;
import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.dto.LocationRequest;
import com.ridelink.driver.dto.ServiceAreaRequest;
import com.ridelink.driver.dto.UpdateDriverRequest;
import com.ridelink.driver.dto.VehicleRequest;
import com.ridelink.driver.dto.VehicleResponse;
import com.ridelink.driver.dto.LocationResponse;
import com.ridelink.driver.dto.ServiceAreaResponse;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DuplicateDriverException;
import com.ridelink.driver.model.Availability;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.Location;
import com.ridelink.driver.model.ServiceArea;
import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.repository.DriverRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public DriverResponse create(
            String accountId,
            CreateDriverRequest request
    ) {
        if (driverRepository.findByAccountId(accountId).isPresent()) {
            throw new DuplicateDriverException(
                    "A driver profile already exists for this account"
            );
        }

        Driver driver = new Driver(
                accountId,
                request.name().trim(),
                request.phone(),
                toVehicle(request.vehicle()),
                Availability.OFFLINE,
                toLocation(request.location()),
                toServiceArea(request.serviceArea())
        );

        try {
            return toResponse(driverRepository.save(driver));
        } catch (DuplicateKeyException exception) {
            throw new DuplicateDriverException(
                    "A driver profile already exists for this account"
            );
        }
    }

    private DriverResponse findById(String id) {
        return toResponse(
                driverRepository.findById(id)
                        .orElseThrow(() -> notFound(id))
        );
    }

    public DriverResponse findByIdForRequester(
            String id,
            String accountId,
            boolean admin
    ) {
        DriverResponse response = findById(id);

        if (!admin && !response.accountId().equals(accountId)) {
            throw new AccessDeniedException(
                    "You cannot access this driver profile"
            );
        }

        return response;
    }

    public DriverResponse findByAccountId(String accountId) {
        return toResponse(
                driverRepository.findByAccountId(accountId)
                        .orElseThrow(() -> notFound(accountId))
        );
    }

    /**
     * Returns drivers who are currently ONLINE.
     *
     * This endpoint is used by Ride Service
     * to obtain eligible drivers for ride assignment.
     */
    public List<AvailableDriverResponse> findAvailableDrivers() {
        return driverRepository.findByAvailability(Availability.ONLINE)
                .stream()
                .map(this::toAvailableResponse)
                .toList();
    }

    public DriverResponse updateOwn(
            String accountId,
            UpdateDriverRequest request
    ) {
        Driver driver = getByAccountId(accountId);

        driver.setName(request.name().trim());
        driver.setPhone(request.phone());

        if (request.vehicle() != null) {
            driver.setVehicle(toVehicle(request.vehicle()));
        }

        if (request.location() != null) {
            driver.setLocation(toLocation(request.location()));
        }

        if (request.serviceArea() != null) {
            driver.setServiceArea(toServiceArea(request.serviceArea()));
        }

        return toResponse(driverRepository.save(driver));
    }

    public DriverResponse updateVehicle(
            String accountId,
            VehicleRequest request
    ) {
        Driver driver = getByAccountId(accountId);

        driver.setVehicle(toVehicle(request));

        return toResponse(driverRepository.save(driver));
    }

    /**
     * Allows the authenticated driver to update
     * their own availability.
     */
    public DriverResponse updateAvailability(
            String accountId,
            AvailabilityRequest request
    ) {
        Driver driver = getByAccountId(accountId);

        driver.setAvailability(request.availability());

        return toResponse(driverRepository.save(driver));
    }

    /**
     * Updates availability using the driver document ID.
     *
     * This method is intended for controlled service/admin
     * operations such as assigning a driver to a ride.
     */
    public DriverResponse updateAvailabilityById(
            String id,
            AvailabilityRequest request
    ) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> notFound(id));

        driver.setAvailability(request.availability());

        return toResponse(driverRepository.save(driver));
    }

    public DriverResponse updateLocation(
            String accountId,
            LocationRequest request
    ) {
        Driver driver = getByAccountId(accountId);

        driver.setLocation(toLocation(request));

        return toResponse(driverRepository.save(driver));
    }

    public DriverResponse updateServiceArea(
            String accountId,
            ServiceAreaRequest request
    ) {
        Driver driver = getByAccountId(accountId);

        driver.setServiceArea(toServiceArea(request));

        return toResponse(driverRepository.save(driver));
    }

    private Driver getByAccountId(String accountId) {
        return driverRepository.findByAccountId(accountId)
                .orElseThrow(() -> notFound(accountId));
    }

    private DriverNotFoundException notFound(String identifier) {
        return new DriverNotFoundException(
                "Driver not found: " + identifier
        );
    }

    private Vehicle toVehicle(VehicleRequest request) {
        return request == null
                ? null
                : new Vehicle(
                        request.vehicleType(),
                        request.brand().trim(),
                        request.model().trim(),
                        request.registrationNumber().trim(),
                        request.color().trim()
                );
    }

    private Location toLocation(LocationRequest request) {
        return request == null
                ? null
                : new Location(
                        request.latitude(),
                        request.longitude()
                );
    }

    private ServiceArea toServiceArea(ServiceAreaRequest request) {
        return request == null
                ? null
                : new ServiceArea(
                        request.city().trim(),
                        request.radiusKm()
                );
    }

    private DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getAccountId(),
                driver.getName(),
                driver.getPhone(),
                toVehicleResponse(driver.getVehicle()),
                driver.getAvailability(),
                toLocationResponse(driver.getLocation()),
                toServiceAreaResponse(driver.getServiceArea())
        );
    }

    /**
     * Response specifically intended for driver discovery.
     *
     * accountId is intentionally not exposed here.
     */
    private AvailableDriverResponse toAvailableResponse(
            Driver driver
    ) {
        return new AvailableDriverResponse(
                driver.getId(),
                driver.getName(),
                toVehicleResponse(driver.getVehicle()),
                toLocationResponse(driver.getLocation()),
                toServiceAreaResponse(driver.getServiceArea())
        );
    }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
        return vehicle == null
                ? null
                : new VehicleResponse(
                        vehicle.getVehicleType(),
                        vehicle.getBrand(),
                        vehicle.getModel(),
                        vehicle.getRegistrationNumber(),
                        vehicle.getColor()
                );
    }

    private LocationResponse toLocationResponse(Location location) {
        return location == null
                ? null
                : new LocationResponse(
                        location.getLatitude(),
                        location.getLongitude()
                );
    }

    private ServiceAreaResponse toServiceAreaResponse(
            ServiceArea serviceArea
    ) {
        return serviceArea == null
                ? null
                : new ServiceAreaResponse(
                        serviceArea.getCity(),
                        serviceArea.getRadiusKm()
                );
    }
}