package com.ridelink.driver.dto;

import com.ridelink.driver.model.VehicleType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverDtoValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validCreateRequestPassesValidation() {
        var request = new CreateDriverRequest("Asha", "555-0100",
                new VehicleRequest(VehicleType.SEDAN, "Toyota", "Camry", "ABC-123", "Blue"),
                new LocationRequest(12.9, 77.6), new ServiceAreaRequest("Bengaluru", 15));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void invalidNestedValuesFailValidation() {
        var request = new CreateDriverRequest("", null,
                new VehicleRequest(null, "", "", "", ""),
                new LocationRequest(91, 181), new ServiceAreaRequest("", 0));

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void oversizedTextFieldsFailValidation() {
        String oversizedName = "x".repeat(101);
        String oversizedPhone = "1".repeat(33);
        var request = new CreateDriverRequest(oversizedName, oversizedPhone,
                new VehicleRequest(VehicleType.SEDAN, "Toyota", "Camry", "ABC-123", "Blue"),
                null, new ServiceAreaRequest("Bengaluru", 15));

        assertFalse(validator.validate(request).isEmpty());
    }
}
