package com.ridelink.farepayment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;
    @Indexed
    private String rideId;
    @Indexed
    private String passengerId;
    @Indexed
    private String driverId;
    private String pickup;
    private String destination;
    private BigDecimal distanceKm;
    private BigDecimal durationMinutes;
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal durationCharge;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    @Indexed
    private PaymentStatus paymentStatus;
    private String transactionReference;
    @Indexed
    private String receiptNumber;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    @Indexed(unique = true, sparse = true)
    private String successfulPaymentKey;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRideId() { return rideId; }
    public void setRideId(String value) { rideId = value; }
    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String value) { passengerId = value; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String value) { driverId = value; }
    public String getPickup() { return pickup; }
    public void setPickup(String value) { pickup = value; }
    public String getDestination() { return destination; }
    public void setDestination(String value) { destination = value; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal value) { distanceKm = value; }
    public BigDecimal getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(BigDecimal value) { durationMinutes = value; }
    public BigDecimal getBaseFare() { return baseFare; }
    public void setBaseFare(BigDecimal value) { baseFare = value; }
    public BigDecimal getDistanceCharge() { return distanceCharge; }
    public void setDistanceCharge(BigDecimal value) { distanceCharge = value; }
    public BigDecimal getDurationCharge() { return durationCharge; }
    public void setDurationCharge(BigDecimal value) { durationCharge = value; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal value) { totalAmount = value; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod value) { paymentMethod = value; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus value) { paymentStatus = value; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String value) { transactionReference = value; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String value) { receiptNumber = value; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String value) { failureReason = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime value) { paidAt = value; }
    public String getSuccessfulPaymentKey() { return successfulPaymentKey; }
    public void setSuccessfulPaymentKey(String value) { successfulPaymentKey = value; }
}
