package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.exception.AccountDisabledException;
import com.ridelink.account.exception.AccountSuspendedException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock AccountService accountService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthenticationService authenticationService;

    @Test
    void loginReturnsTokenForValidCredentials() {
        Account account = new Account("Asha", "asha@example.com", "hashed", Role.PASSENGER, AccountStatus.ACTIVE);
        when(accountService.findByEmail("asha@example.com")).thenReturn(account);
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(account)).thenReturn("jwt-token");
        when(accountService.toResponse(account)).thenCallRealMethod();

        var response = authenticationService.login(new LoginRequest("asha@example.com", "password123"));

        assertEquals("jwt-token", response.token());
        assertEquals("asha@example.com", response.account().email());
    }

    @Test
    void loginRejectsInvalidPassword() {
        Account account = new Account("Asha", "asha@example.com", "hashed", Role.PASSENGER, AccountStatus.ACTIVE);
        when(accountService.findByEmail("asha@example.com")).thenReturn(account);
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authenticationService.login(new LoginRequest("asha@example.com", "wrong")));
    }

    @Test
    void loginRejectsSuspendedAccount() {
        Account account = new Account("Asha", "asha@example.com", "hashed", Role.PASSENGER, AccountStatus.SUSPENDED);
        when(accountService.findByEmail("asha@example.com")).thenReturn(account);

        assertThrows(AccountSuspendedException.class,
                () -> authenticationService.login(new LoginRequest("asha@example.com", "password123")));
    }

    @Test
    void loginRejectsDisabledAccount() {
        Account account = new Account("Asha", "asha@example.com", "hashed", Role.PASSENGER, AccountStatus.DISABLED);
        when(accountService.findByEmail("asha@example.com")).thenReturn(account);

        assertThrows(AccountDisabledException.class,
                () -> authenticationService.login(new LoginRequest("asha@example.com", "password123")));
    }
}