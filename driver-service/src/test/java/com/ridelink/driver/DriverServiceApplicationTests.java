package com.ridelink.driver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.jwt.secret=driver-context-test-secret-at-least-32-bytes",
		"driver.service.internal-key=driver-context-internal-test-key",
		"spring.data.mongodb.uri=mongodb://localhost:27017/driver_service_context_test_db"
})
class DriverServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
