package com.ridelink.farepayment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FareCalculator {

    private static final int MONEY_SCALE = 2;
    private final BigDecimal baseFare;
    private final BigDecimal perKmRate;
    private final BigDecimal perMinuteRate;

    public FareCalculator(
            @Value("${fare.base-fare:100.00}") BigDecimal baseFare,
            @Value("${fare.per-km-rate:80.00}") BigDecimal perKmRate,
            @Value("${fare.per-minute-rate:10.00}") BigDecimal perMinuteRate) {
        this.baseFare = money(baseFare);
        this.perKmRate = perKmRate;
        this.perMinuteRate = perMinuteRate;
    }

    public FareBreakdown calculate(BigDecimal distanceKm, BigDecimal durationMinutes) {
        BigDecimal distanceCharge = money(distanceKm.multiply(perKmRate));
        BigDecimal durationCharge = money(durationMinutes.multiply(perMinuteRate));
        BigDecimal total = money(baseFare.add(distanceCharge).add(durationCharge));
        return new FareBreakdown(baseFare, distanceCharge, durationCharge, total);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public record FareBreakdown(BigDecimal baseFare, BigDecimal distanceCharge,
                                BigDecimal durationCharge, BigDecimal total) { }
}
