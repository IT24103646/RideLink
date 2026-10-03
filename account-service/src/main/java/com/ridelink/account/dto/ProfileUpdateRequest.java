package com.ridelink.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 30) @Pattern(regexp = "^[0-9+() .-]*$", message = "Phone contains invalid characters") String phone) {
}