package com.ridelink.farepayment.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.farepayment.dto.request.FareEstimateRequest;
import com.ridelink.farepayment.dto.response.FareEstimateResponse;
import com.ridelink.farepayment.service.FareService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    @PreAuthorize("hasAnyAuthority('ROLE_PASSENGER', 'ROLE_ADMIN')")
    public FareEstimateResponse estimate(@Valid @RequestBody FareEstimateRequest request, Authentication authentication) {
        return fareService.estimate(authentication.getName(), request);
    }

    @GetMapping("/estimates/{estimateId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PASSENGER', 'ROLE_ADMIN')")
    public FareEstimateResponse getEstimate(@PathVariable String estimateId, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return fareService.getEstimate(estimateId, authentication.getName(), admin);
    }
}
