package com.ridelink.farepayment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ridelink.farepayment.client.RideClient;
import com.ridelink.farepayment.client.RideResponse;
import com.ridelink.farepayment.dto.request.PaymentRequest;
import com.ridelink.farepayment.exception.PaymentAlreadyExistsException;
import com.ridelink.farepayment.exception.PaymentNotAllowedException;
import com.ridelink.farepayment.model.Payment;
import com.ridelink.farepayment.model.PaymentMethod;
import com.ridelink.farepayment.model.PaymentStatus;
import com.ridelink.farepayment.repository.PaymentRepository;
import com.ridelink.farepayment.service.FareCalculator;
import com.ridelink.farepayment.service.PaymentService;

class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RideClient rideClient;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(paymentRepository, rideClient,
                new FareCalculator(new BigDecimal("100"), new BigDecimal("80"), new BigDecimal("10")));
    }

    @Test
    void createsSuccessfulPaymentForCompletedRide() {
        when(rideClient.getRide("ride-1", "Bearer token")).thenReturn(completedRide());
        when(paymentRepository.findByRideIdAndPaymentStatus("ride-1", PaymentStatus.SUCCESS)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("payment-1");
            return payment;
        });

        Payment payment = paymentService.createPayment("passenger-1", false, "Bearer token",
                new PaymentRequest("ride-1", new BigDecimal("10.5"), new BigDecimal("30"), PaymentMethod.CARD, false));

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        assertEquals(new BigDecimal("1240.00"), payment.getTotalAmount());
        assertNotNull(payment.getReceiptNumber());
        assertNotNull(payment.getPaidAt());
    }

    @Test
    void recordsFailedSimulationWithoutReceipt() {
        when(rideClient.getRide("ride-1", "Bearer token")).thenReturn(completedRide());
        when(paymentRepository.findByRideIdAndPaymentStatus("ride-1", PaymentStatus.SUCCESS)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.createPayment("passenger-1", false, "Bearer token",
                new PaymentRequest("ride-1", new BigDecimal("10"), new BigDecimal("30"), PaymentMethod.ONLINE, true));

        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
        assertEquals(null, payment.getReceiptNumber());
        assertEquals(null, payment.getPaidAt());
    }

    @Test
    void rejectsPaymentForIncompleteRide() {
        RideResponse ride = new RideResponse("ride-1", "passenger-1", "driver-1", "A", "B", "IN_PROGRESS", null, null, null, null, null, null);
        when(rideClient.getRide("ride-1", "Bearer token")).thenReturn(ride);

        assertThrows(PaymentNotAllowedException.class, () -> paymentService.createPayment("passenger-1", false,
                "Bearer token", new PaymentRequest("ride-1", new BigDecimal("10"), new BigDecimal("30"), PaymentMethod.CARD, false)));
    }

    @Test
    void rejectsDuplicateSuccessfulPayment() {
        when(rideClient.getRide("ride-1", "Bearer token")).thenReturn(completedRide());
        when(paymentRepository.findByRideIdAndPaymentStatus("ride-1", PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(new Payment()));

        assertThrows(PaymentAlreadyExistsException.class, () -> paymentService.createPayment("passenger-1", false,
                "Bearer token", new PaymentRequest("ride-1", new BigDecimal("10"), new BigDecimal("30"), PaymentMethod.CARD, false)));
    }

    private RideResponse completedRide() {
        return new RideResponse("ride-1", "passenger-1", "driver-1", "A", "B", "COMPLETED", null, null, null, null, null, null);
    }
}
