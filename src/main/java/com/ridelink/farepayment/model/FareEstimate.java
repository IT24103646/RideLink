package com.ridelink.farepayment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fare_estimates")
public class FareEstimate {

    @Id
    private String id;
    @Indexed
    private String passengerId;
    private String pickup;
    private String destination;
    private BigDecimal estimatedDistanceKm;
    private BigDecimal estimatedDurationMinutes;
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal durationCharge;
    private BigDecimal estimatedTotal;
    @Indexed
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
    public String getPickup() { return pickup; }
    public void setPickup(String pickup) { this.pickup = pickup; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public BigDecimal getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(BigDecimal value) { estimatedDistanceKm = value; }
    public BigDecimal getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(BigDecimal value) { estimatedDurationMinutes = value; }
    public BigDecimal getBaseFare() { return baseFare; }
    public void setBaseFare(BigDecimal value) { baseFare = value; }
    public BigDecimal getDistanceCharge() { return distanceCharge; }
    public void setDistanceCharge(BigDecimal value) { distanceCharge = value; }
    public BigDecimal getDurationCharge() { return durationCharge; }
    public void setDurationCharge(BigDecimal value) { durationCharge = value; }
    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal value) { estimatedTotal = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
}
