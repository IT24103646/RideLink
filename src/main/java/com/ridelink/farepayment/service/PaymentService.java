package com.ridelink.farepayment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.ridelink.farepayment.client.RideClient;
import com.ridelink.farepayment.client.RideResponse;
import com.ridelink.farepayment.dto.request.PaymentRequest;
import com.ridelink.farepayment.dto.response.PaymentResponse;
import com.ridelink.farepayment.dto.response.ReceiptResponse;
import com.ridelink.farepayment.exception.ForbiddenPaymentAccessException;
import com.ridelink.farepayment.exception.PaymentAlreadyExistsException;
import com.ridelink.farepayment.exception.PaymentNotAllowedException;
import com.ridelink.farepayment.exception.PaymentNotFoundException;
import com.ridelink.farepayment.exception.ReceiptNotAvailableException;
import com.ridelink.farepayment.model.Payment;
import com.ridelink.farepayment.model.PaymentStatus;
import com.ridelink.farepayment.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RideClient rideClient;
    private final FareCalculator fareCalculator;

    public PaymentService(PaymentRepository paymentRepository, RideClient rideClient, FareCalculator fareCalculator) {
        this.paymentRepository = paymentRepository;
        this.rideClient = rideClient;
        this.fareCalculator = fareCalculator;
    }

    public Payment createPayment(String accountId, boolean admin, String bearerToken, PaymentRequest request) {
        RideResponse ride = rideClient.getRide(request.rideId(), bearerToken);
        if (!"COMPLETED".equals(ride.status())) {
            throw new PaymentNotAllowedException();
        }
        if (!admin && !accountId.equals(ride.passengerId())) {
            throw new ForbiddenPaymentAccessException();
        }
        if (paymentRepository.findByRideIdAndPaymentStatus(request.rideId(), PaymentStatus.SUCCESS).isPresent()) {
            throw new PaymentAlreadyExistsException();
        }

        FareCalculator.FareBreakdown fare = fareCalculator.calculate(
                request.actualDistanceKm(), request.actualDurationMinutes());
        Payment payment = new Payment();
        payment.setRideId(ride.id());
        payment.setPassengerId(ride.passengerId());
        payment.setDriverId(ride.driverId());
        payment.setPickup(ride.pickup());
        payment.setDestination(ride.destination());
        payment.setDistanceKm(request.actualDistanceKm());
        payment.setDurationMinutes(request.actualDurationMinutes());
        payment.setBaseFare(fare.baseFare());
        payment.setDistanceCharge(fare.distanceCharge());
        payment.setDurationCharge(fare.durationCharge());
        payment.setTotalAmount(fare.total());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setCreatedAt(LocalDateTime.now());

        if (request.simulateFailure()) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment declined by the simulation request.");
        } else {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionReference("SIM-" + UUID.randomUUID());
            payment.setReceiptNumber("RCPT-" + UUID.randomUUID());
            payment.setSuccessfulPaymentKey(ride.id());
            payment.setPaidAt(LocalDateTime.now());
        }

        try {
            return paymentRepository.save(payment);
        } catch (DataIntegrityViolationException exception) {
            throw new PaymentAlreadyExistsException();
        }
    }

    public PaymentResponse getPayment(String paymentId, String accountId, String role, String bearerToken) {
        Payment payment = findPayment(paymentId);
        authorizePayment(payment, accountId, role, bearerToken);
        return toResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByRide(String rideId, String accountId, String role, String bearerToken) {
        RideResponse ride = rideClient.getRide(rideId, bearerToken);
        authorizeRide(ride, accountId, role);
        return paymentRepository.findByRideId(rideId).stream().map(this::toResponse).toList();
    }

    public ReceiptResponse getReceipt(String paymentId, String accountId, String role, String bearerToken) {
        Payment payment = findPayment(paymentId);
        authorizePayment(payment, accountId, role, bearerToken);
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS || payment.getReceiptNumber() == null) {
            throw new ReceiptNotAvailableException();
        }
        return new ReceiptResponse(payment.getReceiptNumber(), payment.getId(), payment.getRideId(),
                payment.getPassengerId(), payment.getDriverId(), payment.getPickup(), payment.getDestination(),
                payment.getDistanceKm(), payment.getDurationMinutes(), payment.getBaseFare(), payment.getDistanceCharge(),
                payment.getDurationCharge(), payment.getTotalAmount(), payment.getPaymentMethod(), payment.getPaymentStatus(),
                payment.getPaidAt());
    }

    private Payment findPayment(String paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(PaymentNotFoundException::new);
    }

    private void authorizePayment(Payment payment, String accountId, String role, String bearerToken) {
        if ("ROLE_ADMIN".equals(role)) {
            return;
        }
        if ("ROLE_PASSENGER".equals(role) && accountId.equals(payment.getPassengerId())) {
            return;
        }
        if ("ROLE_DRIVER".equals(role)) {
            authorizeRide(rideClient.getRide(payment.getRideId(), bearerToken), accountId, role);
            return;
        }
        throw new ForbiddenPaymentAccessException();
    }

    private void authorizeRide(RideResponse ride, String accountId, String role) {
        if ("ROLE_ADMIN".equals(role)) {
            return;
        }
        if ("ROLE_PASSENGER".equals(role) && accountId.equals(ride.passengerId())) {
            return;
        }
        if ("ROLE_DRIVER".equals(role)) {
            return;
        }
        throw new ForbiddenPaymentAccessException();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getRideId(), payment.getPassengerId(), payment.getDriverId(),
                payment.getPickup(), payment.getDestination(), payment.getDistanceKm(), payment.getDurationMinutes(),
                payment.getBaseFare(), payment.getDistanceCharge(), payment.getDurationCharge(), payment.getTotalAmount(),
                payment.getPaymentMethod(), payment.getPaymentStatus(), payment.getTransactionReference(),
                payment.getReceiptNumber(), payment.getFailureReason(), payment.getCreatedAt(), payment.getPaidAt());
    }
}
