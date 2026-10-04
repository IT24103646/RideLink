package com.ridelink.driver.repository;

import com.ridelink.driver.model.Availability;
import com.ridelink.driver.model.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends MongoRepository<Driver, String> {

    Optional<Driver> findByAccountId(String accountId);

    List<Driver> findByAvailability(Availability availability);
}