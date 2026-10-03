package com.ridelink.farepayment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.farepayment.model.Payment;
import com.ridelink.farepayment.model.PaymentStatus;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByRideIdAndPaymentStatus(String rideId, PaymentStatus paymentStatus);
    List<Payment> findByRideId(String rideId);
}
