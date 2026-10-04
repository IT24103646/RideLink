package com.ridelink.farepayment.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.farepayment.model.FareEstimate;

public interface FareEstimateRepository extends MongoRepository<FareEstimate, String> {
    Optional<FareEstimate> findByIdAndPassengerId(String id, String passengerId);
}
