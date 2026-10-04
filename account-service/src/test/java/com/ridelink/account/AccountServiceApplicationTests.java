package com.ridelink.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
		"app.jwt.secret=test-signing-key-that-is-long-enough-for-hmac-sha",
		"spring.data.mongodb.uri=mongodb://localhost:27017/account_test_db"
})
class AccountServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
