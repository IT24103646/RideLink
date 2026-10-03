package com.ridelink.farepayment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.ridelink.farepayment.service.FareCalculator;

class FareCalculatorTest {

    private final FareCalculator calculator = new FareCalculator(
            new BigDecimal("100.00"), new BigDecimal("80.00"), new BigDecimal("10.00"));

    @Test
    void calculatesConfiguredFare() {
        FareCalculator.FareBreakdown result = calculator.calculate(new BigDecimal("18.5"), new BigDecimal("42"));

        assertEquals(new BigDecimal("100.00"), result.baseFare());
        assertEquals(new BigDecimal("1480.00"), result.distanceCharge());
        assertEquals(new BigDecimal("420.00"), result.durationCharge());
        assertEquals(new BigDecimal("2000.00"), result.total());
    }

    @Test
    void roundsEachChargeToTwoDecimalPlaces() {
        FareCalculator.FareBreakdown result = calculator.calculate(new BigDecimal("1.234"), new BigDecimal("2.345"));

        assertEquals(new BigDecimal("98.72"), result.distanceCharge());
        assertEquals(new BigDecimal("23.45"), result.durationCharge());
        assertEquals(new BigDecimal("222.17"), result.total());
    }
}
