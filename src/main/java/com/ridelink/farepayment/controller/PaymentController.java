package com.ridelink.farepayment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.farepayment.dto.request.PaymentRequest;
import com.ridelink.farepayment.dto.response.PaymentResponse;
import com.ridelink.farepayment.dto.response.ReceiptResponse;
import com.ridelink.farepayment.model.Payment;
import com.ridelink.farepayment.model.PaymentStatus;
import com.ridelink.farepayment.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PASSENGER', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication,
            @RequestHeader("Authorization") String bearerToken) {
        boolean admin = isAdmin(authentication);
        Payment payment = paymentService.createPayment(authentication.getName(), admin, bearerToken, request);
        HttpStatus status = payment.getPaymentStatus() == PaymentStatus.SUCCESS ? HttpStatus.CREATED : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(toResponse(payment));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PASSENGER', 'ROLE_DRIVER', 'ROLE_ADMIN')")
    public PaymentResponse getPayment(
            @PathVariable String paymentId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        return paymentService.getPayment(paymentId, authentication.getName(), role(authentication), bearerToken);
    }

    @GetMapping("/ride/{rideId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PASSENGER', 'ROLE_DRIVER', 'ROLE_ADMIN')")
    public List<PaymentResponse> getPaymentsByRide(
            @PathVariable String rideId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        return paymentService.getPaymentsByRide(rideId, authentication.getName(), role(authentication), bearerToken);
    }

    @GetMapping("/{paymentId}/receipt")
    @PreAuthorize("hasAnyAuthority('ROLE_PASSENGER', 'ROLE_DRIVER', 'ROLE_ADMIN')")
    public ReceiptResponse getReceipt(
            @PathVariable String paymentId,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        return paymentService.getReceipt(paymentId, authentication.getName(), role(authentication), bearerToken);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private String role(Authentication authentication) {
        return authentication.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse("");
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getRideId(), payment.getPassengerId(), payment.getDriverId(),
                payment.getPickup(), payment.getDestination(), payment.getDistanceKm(), payment.getDurationMinutes(),
                payment.getBaseFare(), payment.getDistanceCharge(), payment.getDurationCharge(), payment.getTotalAmount(),
                payment.getPaymentMethod(), payment.getPaymentStatus(), payment.getTransactionReference(),
                payment.getReceiptNumber(), payment.getFailureReason(), payment.getCreatedAt(), payment.getPaidAt());
    }
}
