package com.ridelink.account.service;

import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.ProfileUpdateRequest;
import com.ridelink.account.exception.DuplicateAccountException;
import com.ridelink.account.exception.InvalidRoleException;
import com.ridelink.account.exception.InvalidStatusException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock AccountRepository accountRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AccountService accountService;

    @Test
    void registerHashesPasswordAndDefaultsToPassenger() {
        RegisterRequest request = new RegisterRequest(" Asha ", "ASHA@example.com ", "password123");
        when(accountRepository.existsByEmailIgnoreCase("asha@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.register(request);

        assertEquals("asha@example.com", response.email());
        assertEquals(Role.PASSENGER, response.role());
        verify(passwordEncoder).encode("password123");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(accountRepository.existsByEmailIgnoreCase("asha@example.com")).thenReturn(true);

        assertThrows(DuplicateAccountException.class,
                () -> accountService.register(new RegisterRequest("Asha", "asha@example.com", "password123")));

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void registerSupportsDriverRole() {
        when(accountRepository.existsByEmailIgnoreCase("driver@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.register(new RegisterRequest("Driver", "driver@example.com", "password123", "DRIVER"));

        assertEquals(Role.DRIVER, response.role());
    }

    @Test
    void registerRejectsAdminRole() {
        when(accountRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(false);

        assertThrows(InvalidRoleException.class,
                () -> accountService.register(new RegisterRequest("Admin", "admin@example.com", "password123", "ADMIN")));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void profileUpdateChangesOnlyAllowedFields() {
        Account account = new Account("Asha", "asha@example.com", "hashed", Role.PASSENGER, AccountStatus.ACTIVE);
        account.setId("account-1");
        when(accountRepository.findById("account-1")).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.updateProfile("account-1", new ProfileUpdateRequest(" Asha Updated ", "+1 555-0100"));

        assertEquals("Asha Updated", response.name());
        assertEquals("+1 555-0100", response.phone());
        assertEquals(Role.PASSENGER, response.role());
        assertEquals(AccountStatus.ACTIVE, response.status());
    }

    @Test
    void roleAndStatusUpdatesValidateValues() {
        Account account = new Account("Asha", "asha@example.com", "hashed", Role.PASSENGER, AccountStatus.ACTIVE);
        when(accountRepository.findById("account-1")).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(Role.ADMIN, accountService.updateRole("account-1", "ADMIN").role());
        assertThrows(InvalidRoleException.class, () -> accountService.updateRole("account-1", "UNKNOWN"));
        assertThrows(InvalidStatusException.class, () -> accountService.updateStatus("account-1", "UNKNOWN"));
    }
}