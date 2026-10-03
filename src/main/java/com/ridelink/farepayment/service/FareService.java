package com.ridelink.farepayment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ridelink.farepayment.dto.request.FareEstimateRequest;
import com.ridelink.farepayment.dto.response.FareEstimateResponse;
import com.ridelink.farepayment.exception.EstimateNotFoundException;
import com.ridelink.farepayment.exception.ForbiddenPaymentAccessException;
import com.ridelink.farepayment.model.FareEstimate;
import com.ridelink.farepayment.repository.FareEstimateRepository;

@Service
public class FareService {

    private final FareEstimateRepository repository;
    private final FareCalculator calculator;

    public FareService(FareEstimateRepository repository, FareCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    public FareEstimateResponse estimate(String passengerId, FareEstimateRequest request) {
        FareCalculator.FareBreakdown breakdown = calculator.calculate(
                request.estimatedDistanceKm(), request.estimatedDurationMinutes());
        FareEstimate estimate = new FareEstimate();
        estimate.setPassengerId(passengerId);
        estimate.setPickup(request.pickup().trim());
        estimate.setDestination(request.destination().trim());
        estimate.setEstimatedDistanceKm(request.estimatedDistanceKm());
        estimate.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        estimate.setBaseFare(breakdown.baseFare());
        estimate.setDistanceCharge(breakdown.distanceCharge());
        estimate.setDurationCharge(breakdown.durationCharge());
        estimate.setEstimatedTotal(breakdown.total());
        estimate.setCreatedAt(LocalDateTime.now());
        return toResponse(repository.save(estimate));
    }

    public FareEstimateResponse getEstimate(String id, String passengerId, boolean admin) {
        FareEstimate estimate = repository.findById(id).orElseThrow(EstimateNotFoundException::new);
        if (!admin && !estimate.getPassengerId().equals(passengerId)) {
            throw new ForbiddenPaymentAccessException();
        }
        return toResponse(estimate);
    }

    private FareEstimateResponse toResponse(FareEstimate estimate) {
        return new FareEstimateResponse(estimate.getId(), estimate.getPickup(), estimate.getDestination(),
                estimate.getEstimatedDistanceKm(), estimate.getEstimatedDurationMinutes(), estimate.getBaseFare(),
                estimate.getDistanceCharge(), estimate.getDurationCharge(), estimate.getEstimatedTotal(), estimate.getCreatedAt());
    }
}
