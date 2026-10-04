package com.ridelink.ride.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;

class DriverClientTest {

    @Test
    void discoveryUsesUserBearerAndAssignmentUsesOnlyServiceKey() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://driver.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DriverClient client = new DriverClient(builder, "test-internal-key");

        server.expect(requestTo("http://driver.test/api/drivers/available"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer passenger-token"))
            .andRespond(withSuccess("[{\"id\":\"driver-1\",\"name\":\"Ada\",\"vehicle\":{},\"location\":{},\"serviceArea\":{}}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://driver.test/api/drivers/driver-1/availability/internal"))
                .andExpect(method(PATCH))
                .andExpect(header("X-Service-Key", "test-internal-key"))
                .andRespond(withSuccess());

        List<AvailableDriverResponse> drivers = client.getAvailableDrivers("Bearer passenger-token");
        client.markDriverUnavailable("driver-1");

        assertEquals("driver-1", drivers.get(0).id());
        server.verify();
    }
}