package com.ridelink.driver.controller;

import com.ridelink.driver.model.Availability;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.repository.DriverRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.crypto.SecretKey;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/driver_service_http_test_db",
        "app.jwt.secret=phase-http-test-secret-at-least-32-bytes",
        "driver.service.internal-key=internal-driver-test-key"
})
class DriverHttpSecurityTest {
    private static final String SECRET = "phase-http-test-secret-at-least-32-bytes";
    private static final String OTHER_SECRET = "wrong-http-test-secret-at-least-32-bytes";
    private static final String INTERNAL_KEY = "internal-driver-test-key";

    @LocalServerPort
    private int port;

    @MockitoBean
    private DriverRepository driverRepository;

    private HttpClient client;

    @BeforeEach
    void setUp() {
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @Test
    void missingTokenReturnsStandard401Response() throws Exception {
        HttpResponse<String> response = get("/api/drivers/me", null);

        assertEquals(401, response.statusCode());
        assertStandardErrorResponse(response.body(), 401);
    }

    @Test
    void malformedExpiredAndWrongSignatureTokensReturnStandard401Response() throws Exception {
        assertUnauthorized("malformed");
        assertUnauthorized(token(SECRET, "driver-1", "DRIVER", new Date(System.currentTimeMillis() - 1_000)));
        assertUnauthorized(token(OTHER_SECRET, "driver-1", "DRIVER", futureExpiration()));
    }

    @Test
    void passengerTokenReturns403ErrorResponse() throws Exception {
        HttpResponse<String> response = get("/api/drivers/me", token(SECRET, "passenger-1", "PASSENGER", futureExpiration()));

        assertEquals(403, response.statusCode());
        assertStandardErrorResponse(response.body(), 403);
    }

    @Test
    void driverCanAccessOwnProfileAndCannotAccessAnotherDriversProfile() throws Exception {
        when(driverRepository.findByAccountId("driver-1"))
                .thenReturn(Optional.of(driver("profile-1", "driver-1")));
        when(driverRepository.findById("profile-2"))
                .thenReturn(Optional.of(driver("profile-2", "driver-2")));

        HttpResponse<String> ownResponse = get("/api/drivers/me",
                token(SECRET, "driver-1", "DRIVER", futureExpiration()));
        HttpResponse<String> otherResponse = get("/api/drivers/profile-2",
                token(SECRET, "driver-1", "DRIVER", futureExpiration()));

        assertEquals(200, ownResponse.statusCode());
        assertEquals(403, otherResponse.statusCode());
        assertStandardErrorResponse(otherResponse.body(), 403);
    }

    @Test
    void adminCanAccessAnotherDriversProfile() throws Exception {
        when(driverRepository.findById("profile-2"))
                .thenReturn(Optional.of(driver("profile-2", "driver-2")));

        HttpResponse<String> response = get("/api/drivers/profile-2",
                token(SECRET, "admin-1", "ADMIN", futureExpiration()));

        assertEquals(200, response.statusCode());
    }

    @Test
    void invalidJsonAndInvalidEnumReturnStandard400Response() throws Exception {
        HttpResponse<String> invalidJson = send("POST", "/api/drivers", "{",
                token(SECRET, "driver-1", "DRIVER", futureExpiration()));
        HttpResponse<String> invalidEnum = send("PATCH", "/api/drivers/me/availability",
                "{\"availability\":\"INVALID\"}",
                token(SECRET, "driver-1", "DRIVER", futureExpiration()));

        assertEquals(400, invalidJson.statusCode());
        assertEquals(400, invalidEnum.statusCode());
        assertStandardErrorResponse(invalidJson.body(), 400);
        assertStandardErrorResponse(invalidEnum.body(), 400);
    }

        @Test
        void validServiceKeyUpdatesAvailability() throws Exception {
        when(driverRepository.findById("profile-1"))
            .thenReturn(Optional.of(driver("profile-1", "driver-1")));
        when(driverRepository.save(org.mockito.ArgumentMatchers.any(Driver.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        HttpResponse<String> response = send("PATCH", "/api/drivers/profile-1/availability/internal",
            "{\"availability\":\"OFFLINE\"}", null, INTERNAL_KEY);

        assertEquals(200, response.statusCode());
        }

        @Test
        void missingAndInvalidServiceKeysAreRejected() throws Exception {
        HttpResponse<String> missing = send("PATCH", "/api/drivers/profile-1/availability/internal",
            "{\"availability\":\"OFFLINE\"}", null, null);
        HttpResponse<String> invalid = send("PATCH", "/api/drivers/profile-1/availability/internal",
            "{\"availability\":\"OFFLINE\"}", null, "wrong-key");

        assertEquals(401, missing.statusCode());
        assertEquals(401, invalid.statusCode());
        }

        @Test
        void userJwtCannotUseInternalEndpointWithoutServiceKey() throws Exception {
        for (String role : new String[] {"PASSENGER", "DRIVER", "ADMIN"}) {
            HttpResponse<String> response = send("PATCH", "/api/drivers/profile-1/availability/internal",
                "{\"availability\":\"OFFLINE\"}",
                token(SECRET, "account-1", role, futureExpiration()), null);
            assertEquals(401, response.statusCode());
        }
        }

        @Test
        void validServiceKeyWithUnknownDriverReturns404() throws Exception {
        when(driverRepository.findById("missing")).thenReturn(Optional.empty());

        HttpResponse<String> response = send("PATCH", "/api/drivers/missing/availability/internal",
            "{\"availability\":\"OFFLINE\"}", null, INTERNAL_KEY);

        assertEquals(404, response.statusCode());
        }

    private void assertUnauthorized(String token) throws Exception {
        HttpResponse<String> response = get("/api/drivers/me", token);
        assertEquals(401, response.statusCode());
        assertStandardErrorResponse(response.body(), 401);
    }

    private void assertStandardErrorResponse(String body, int status) {
        assertTrue(body.contains("\"timestamp\""));
        assertTrue(body.contains("\"status\":" + status));
        assertTrue(body.contains("\"error\""));
        assertTrue(body.contains("\"message\""));
        assertTrue(body.contains("\"validationErrors\""));
    }

    private Driver driver(String id, String accountId) {
        Driver driver = new Driver(accountId, "Test Driver", null, null, Availability.OFFLINE, null, null);
        driver.setId(id);
        return driver;
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        return send("GET", path, null, token);
    }

    private HttpResponse<String> send(String method, String path, String body, String token) throws Exception {
        return send(method, path, body, token, null);
    }

    private HttpResponse<String> send(String method, String path, String body, String token, String serviceKey) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (serviceKey != null) {
            builder.header("X-Service-Key", serviceKey);
        }
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        return client.send(builder.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString());
    }

    private Date futureExpiration() {
        return new Date(System.currentTimeMillis() + 60_000);
    }

    private String token(String secret, String subject, String role, Date expiration) {
        SecretKey signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("email", "test@example.com")
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }
}
