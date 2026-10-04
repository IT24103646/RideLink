package com.ridelink.driver.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {
    private static final String SECRET = "phase-2-5-test-secret-at-least-32-bytes";
    private static final String OTHER_SECRET = "different-test-secret-at-least-32-bytes";

    private JwtAuthenticationFilter filter;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validDriverTokenCreatesDriverAuthentication() throws ServletException, IOException {
        String token = createToken(SECRET, "account-123", "driver@example.com", "DRIVER", new Date(System.currentTimeMillis() + 60_000));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(requestWithToken(token), new MockHttpServletResponse(), chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("account-123", authentication.getName());
        assertEquals("account-123", jwtService.extractSubject(token));
        assertEquals("driver@example.com", jwtService.extractEmail(token));
        assertEquals("DRIVER", jwtService.extractRole(token));
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DRIVER")));
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void expiredTokenIsRejected() throws ServletException, IOException {
        String token = createToken(SECRET, "account-123", "driver@example.com", "DRIVER", new Date(System.currentTimeMillis() - 1_000));

        filter.doFilter(requestWithToken(token), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertFalse(jwtService.isValid(token));
    }

    @Test
    void wrongSignatureIsRejected() throws ServletException, IOException {
        String token = createToken(OTHER_SECRET, "account-123", "driver@example.com", "DRIVER", futureExpiration());

        filter.doFilter(requestWithToken(token), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void malformedTokenIsRejected() throws ServletException, IOException {
        filter.doFilter(requestWithToken("not-a-jwt"), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void missingAuthorizationHeaderRemainsUnauthenticated() throws ServletException, IOException {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "PASSENGER"})
    void supportedRolesMapToSpringAuthorities(String role) throws ServletException, IOException {
        String token = createToken(SECRET, "account-123", "user@example.com", role, futureExpiration());

        filter.doFilter(requestWithToken(token), new MockHttpServletResponse(), new MockFilterChain());

        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role)));
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private Date futureExpiration() {
        return new Date(System.currentTimeMillis() + 60_000);
    }

    private String createToken(String secret, String subject, String email, String role, Date expiration) {
        SecretKey signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

}
