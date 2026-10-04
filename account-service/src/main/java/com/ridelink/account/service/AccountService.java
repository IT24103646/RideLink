package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.ProfileUpdateRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.exception.AccountNotFoundException;
import com.ridelink.account.exception.DuplicateAccountException;
import com.ridelink.account.exception.InvalidRoleException;
import com.ridelink.account.exception.InvalidStatusException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.repository.AccountRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateAccountException("An account with this email already exists");
        }
        Account account = new Account(request.name().trim(), email,
            passwordEncoder.encode(request.password()), registrationRole(request.role()), AccountStatus.ACTIVE);
        try {
            return toResponse(accountRepository.save(account));
        } catch (DuplicateKeyException exception) {
            throw new DuplicateAccountException("An account with this email already exists");
        }
    }

    public Account findByEmail(String email) {
        return accountRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public AccountResponse getResponseById(String id) {
        return toResponse(accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found")));
    }

    public AccountResponse updateProfile(String id, ProfileUpdateRequest request) {
        Account account = findById(id);
        account.setName(request.name().trim());
        account.setPhone(normalizePhone(request.phone()));
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse updateRole(String id, String requestedRole) {
        Account account = findById(id);
        account.setRole(parseRole(requestedRole));
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse updateStatus(String id, String requestedStatus) {
        Account account = findById(id);
        account.setStatus(parseStatus(requestedStatus));
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getName(), account.getEmail(), account.getPhone(), account.getRole(), account.getStatus());
    }

    private Account findById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    private Role registrationRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return Role.PASSENGER;
        }
        Role role = parseRole(requestedRole);
        if (role == Role.ADMIN) {
            throw new InvalidRoleException("Public registration cannot create an ADMIN account");
        }
        return role;
    }

    private Role parseRole(String requestedRole) {
        try {
            return Role.valueOf(requestedRole.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidRoleException("Role must be PASSENGER, DRIVER, or ADMIN");
        }
    }

    private AccountStatus parseStatus(String requestedStatus) {
        try {
            return AccountStatus.valueOf(requestedStatus.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidStatusException("Status must be ACTIVE, SUSPENDED, or DISABLED");
        }
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}