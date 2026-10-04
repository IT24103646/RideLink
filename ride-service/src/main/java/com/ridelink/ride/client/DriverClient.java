package com.ridelink.ride.client;

import java.time.Duration;
import java.net.http.HttpClient;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ridelink.ride.exception.DriverServiceUnavailableException;
import com.ridelink.ride.exception.DriverServiceAuthorizationException;
import com.ridelink.ride.exception.ForbiddenRideAccessException;
import com.ridelink.ride.exception.NoAvailableDriverException;

@Component
public class DriverClient {
    private static final Logger logger = LoggerFactory.getLogger(DriverClient.class);

    private final RestClient restClient;
    private final String driverServiceInternalKey;

    @Autowired
    public DriverClient(
            @Value("${driver.service.base-url:http://localhost:8081}") String baseUrl,
            @Value("${driver.service.internal-key:}") String driverServiceInternalKey) {
        this(createBuilder(baseUrl), driverServiceInternalKey);
    }

    DriverClient(RestClient.Builder builder, String driverServiceInternalKey) {
        this.restClient = builder.build();
        this.driverServiceInternalKey = driverServiceInternalKey;
    }

    private static RestClient.Builder createBuilder(String baseUrl) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                ;
    }

    public List<AvailableDriverResponse> getAvailableDrivers() {
        return getAvailableDrivers(null);
    }

    public List<AvailableDriverResponse> getAvailableDrivers(String bearerToken) {
        try {
            var requestSpec = restClient.get()
                    .uri("/api/drivers/available")
                    .accept(MediaType.APPLICATION_JSON);
            if (bearerToken != null && !bearerToken.isBlank()) {
                requestSpec.header("Authorization", bearerToken);
            }
            return requestSpec.retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException exception) {
            logger.warn("Driver discovery rejected with HTTP {}", exception.getStatusCode().value());
            throw new ForbiddenRideAccessException("Driver service rejected the request.");
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            logger.warn("Driver discovery unavailable: {}", exception.getClass().getSimpleName());
            throw new DriverServiceUnavailableException("A driver cannot be assigned now. Please retry.");
        } catch (RestClientException exception) {
            logger.warn("Driver discovery failed: {}", exception.getClass().getSimpleName());
            throw new DriverServiceUnavailableException("A driver cannot be assigned now. Please retry.");
        }
    }

    public void markDriverUnavailable(String driverId) {
        if (driverServiceInternalKey == null || driverServiceInternalKey.isBlank()) {
            throw new DriverServiceAuthorizationException("Driver Service availability update requires a configured service credential.");
        }

        var request = new AvailabilityUpdateRequest("OFFLINE");
        try {
            restClient.patch()
                    .uri("/api/drivers/{id}/availability/internal", driverId)
                    .header("X-Service-Key", driverServiceInternalKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException exception) {
            logger.warn("Driver availability update rejected with HTTP {}", exception.getStatusCode().value());
            if (exception.getStatusCode().value() == 401 || exception.getStatusCode().value() == 403) {
                throw new DriverServiceAuthorizationException("Driver assignment request rejected by Driver Service.");
            }
            throw new ForbiddenRideAccessException("Driver assignment request rejected by Driver Service.");
        } catch (HttpServerErrorException | ResourceAccessException exception) {
            logger.warn("Driver availability update unavailable: {}", exception.getMessage(), exception);
            throw new DriverServiceUnavailableException("A driver cannot be assigned now. Please retry.");
        } catch (RestClientException exception) {
            logger.warn("Driver availability update failed: {}", exception.getClass().getSimpleName());
            throw new DriverServiceUnavailableException("A driver cannot be assigned now. Please retry.");
        }
    }

    public DriverProfileResponse getCurrentDriverProfile(String bearerToken) {
        try {
            return restClient.get()
                    .uri("/api/drivers/me")
                    .header("Authorization", bearerToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ForbiddenRideAccessException("Driver access is forbidden.");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new DriverServiceUnavailableException("Driver service unavailable.");
                    })
                    .body(DriverProfileResponse.class);
        } catch (RestClientException exception) {
            throw new DriverServiceUnavailableException("Driver service unavailable.");
        }
    }
}
