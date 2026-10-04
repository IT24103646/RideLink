package com.ridelink.farepayment.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ridelink.farepayment.exception.RideNotFoundException;
import com.ridelink.farepayment.exception.RideServiceException;
import com.ridelink.farepayment.exception.RideServiceUnavailableException;

@Component
public class RideClient {

    private final RestClient restClient;

    public RideClient(@Value("${ride.service.base-url:http://localhost:8082}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));
        restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public RideResponse getRide(String rideId, String bearerToken) {
        try {
            return restClient.get()
                    .uri("/api/rides/{id}", rideId)
                    .header("Authorization", bearerToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 404,
                            (request, response) -> { throw new RideNotFoundException("Ride not found."); })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, response) -> { throw new RideServiceUnavailableException("Ride Service is unavailable."); })
                    .onStatus(HttpStatusCode::is4xxClientError,
                            (request, response) -> { throw new RideServiceException("Ride Service rejected the request."); })
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RideNotFoundException | RideServiceException | RideServiceUnavailableException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new RideServiceUnavailableException("Ride Service is unavailable.");
        } catch (RestClientException exception) {
            throw new RideServiceException("Ride Service returned an invalid response.");
        }
    }
}
