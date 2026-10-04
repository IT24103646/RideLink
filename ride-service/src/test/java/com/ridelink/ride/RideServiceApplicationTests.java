package com.ridelink.ride;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.jwt.secret=ride-context-test-secret-at-least-32-bytes",
		"driver.service.internal-key=ride-context-internal-test-key",
		"spring.data.mongodb.uri=mongodb://localhost:27017/ride_service_context_test_db"
})
class RideServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
