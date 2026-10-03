package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.exception.AccountDisabledException;
import com.ridelink.account.exception.AccountNotFoundException;
import com.ridelink.account.exception.AccountSuspendedException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(AccountService accountService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Account account;
        try {
            account = accountService.findByEmail(request.email());
        } catch (AccountNotFoundException exception) {
            throw new InvalidCredentialsException();
        }
        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException();
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountDisabledException();
        }
        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return new LoginResponse(jwtService.generateToken(account), accountService.toResponse(account));
    }
}