package com.ridelink.ride.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.ride.model.Ride;

public interface RideRepository extends MongoRepository<Ride, String> {

    List<Ride> findByPassengerId(String passengerId);

    List<Ride> findByDriverId(String driverId);
}