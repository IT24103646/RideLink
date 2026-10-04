package com.ridelink.driver.repository;

import com.ridelink.driver.model.Availability;
import com.ridelink.driver.model.Driver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/driver_service_test_db",
    "spring.data.mongodb.auto-index-creation=true",
    "app.jwt.secret=repository-test-secret-at-least-32-bytes"
})
class DriverRepositoryTest {
    @Autowired
    private DriverRepository driverRepository;

    @Test
    void findsDriverByAccountId() {
        String accountId = "repository-test-account";
        Driver saved = driverRepository.save(
            new Driver(accountId, "Test Driver", null, null, Availability.OFFLINE, null, null));

        var result = driverRepository.findByAccountId(accountId);

        assertTrue(result.isPresent());
        assertEquals(accountId, result.get().getAccountId());
        driverRepository.deleteById(saved.getId());
    }
}
