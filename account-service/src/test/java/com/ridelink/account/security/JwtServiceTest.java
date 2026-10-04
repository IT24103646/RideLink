package com.ridelink.account.security;

import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(
            "test-signing-key-that-is-long-enough-for-hmac-sha", 3_600_000);

    @Test
    void tokenContainsRideLinkClaims() {
        Account account = new Account("Asha", "asha@example.com", "hashed", Role.DRIVER, AccountStatus.ACTIVE);
        account.setId("account-1");

        String token = jwtService.generateToken(account);

        assertEquals("account-1", jwtService.extractSubject(token));
        assertEquals("asha@example.com", jwtService.extractEmail(token));
        assertEquals("DRIVER", jwtService.extractRole(token));
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void invalidTokenIsRejected() {
        assertFalse(jwtService.isValid("not-a-jwt"));
    }
}