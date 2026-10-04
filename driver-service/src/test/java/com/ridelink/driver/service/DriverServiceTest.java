package com.ridelink.driver.service;

import com.ridelink.driver.dto.CreateDriverRequest;
import com.ridelink.driver.dto.LocationRequest;
import com.ridelink.driver.dto.AvailabilityRequest;
import com.ridelink.driver.dto.ServiceAreaRequest;
import com.ridelink.driver.dto.VehicleRequest;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.DuplicateDriverException;
import com.ridelink.driver.model.Availability;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.VehicleType;
import com.ridelink.driver.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {
    @Mock
    private DriverRepository driverRepository;

    @Test
    void createsProfileUsingAuthenticatedAccountId() {
        Driver driver = new Driver("account-1", "Asha", "555", null, Availability.OFFLINE, null, null);
        when(driverRepository.findByAccountId("account-1")).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        DriverService service = new DriverService(driverRepository);

        var response = service.create("account-1", validRequest());

        assertEquals("account-1", response.accountId());
        assertEquals("Asha", response.name());
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void rejectsDuplicateAccountProfile() {
        when(driverRepository.findByAccountId("account-1"))
                .thenReturn(Optional.of(new Driver("account-1", "Existing", null, null, Availability.OFFLINE, null, null)));
        DriverService service = new DriverService(driverRepository);

        assertThrows(DuplicateDriverException.class, () -> service.create("account-1", validRequest()));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void missingProfileRaisesNotFound() {
        when(driverRepository.findByAccountId("missing")).thenReturn(Optional.empty());
        DriverService service = new DriverService(driverRepository);

        assertThrows(DriverNotFoundException.class, () -> service.findByAccountId("missing"));
    }

    @Test
    void updatesAvailabilityLocationAndServiceAreaForOwnProfile() {
        Driver driver = new Driver("account-1", "Asha", null, null, Availability.OFFLINE, null, null);
        when(driverRepository.findByAccountId("account-1")).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);
        DriverService service = new DriverService(driverRepository);

        assertEquals(Availability.ONLINE, service.updateAvailability("account-1", new AvailabilityRequest(Availability.ONLINE)).availability());
        assertEquals(12.9, service.updateLocation("account-1", new LocationRequest(12.9, 77.6)).location().latitude());
        assertEquals("Bengaluru", service.updateServiceArea("account-1", new ServiceAreaRequest("Bengaluru", 15)).serviceArea().city());
        verify(driverRepository, org.mockito.Mockito.times(3)).save(driver);
    }

    private CreateDriverRequest validRequest() {
        return new CreateDriverRequest("Asha", "555",
                new VehicleRequest(VehicleType.SEDAN, "Toyota", "Camry", "ABC-123", "Blue"),
                new LocationRequest(12.9, 77.6), new ServiceAreaRequest("Bengaluru", 15));
    }
}
