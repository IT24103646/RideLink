package com.ridelink.driver.controller;

import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.model.Availability;
import com.ridelink.driver.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DriverControllerTest {
    private final DriverService driverService = mock(DriverService.class);
    private final DriverController driverController = new DriverController(driverService);

    @Test
    void meUsesAuthenticatedAccountId() {
        DriverResponse response = new DriverResponse("profile-1", "account-1", "Asha", null, null,
                Availability.OFFLINE, null, null);
        when(driverService.findByAccountId("account-1")).thenReturn(response);

        assertEquals(response, driverController.me(authentication("account-1", "ROLE_DRIVER")));
        verify(driverService).findByAccountId("account-1");
    }

    @Test
    void idLookupPassesAdminFlagToService() {
        DriverResponse response = new DriverResponse("profile-1", "account-2", "Other", null, null,
                Availability.OFFLINE, null, null);
        when(driverService.findByIdForRequester("profile-1", "admin-1", true)).thenReturn(response);

        assertEquals(response, driverController.findById("profile-1", authentication("admin-1", "ROLE_ADMIN")));
        verify(driverService).findByIdForRequester("profile-1", "admin-1", true);
    }

    private UsernamePasswordAuthenticationToken authentication(String subject, String authority) {
        return new UsernamePasswordAuthenticationToken(subject, null,
                List.of(new SimpleGrantedAuthority(authority)));
    }
}