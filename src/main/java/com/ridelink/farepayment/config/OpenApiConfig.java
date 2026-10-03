package com.ridelink.farepayment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI farePaymentOpenApi() {
        return new OpenAPI()
                .info(new Info().title("RideLink Fare and Payment API")
                        .description("Fare estimation and simulated payment APIs. Fare = base fare + distance x per-km rate + duration x per-minute rate. Default rates are project configuration.")
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
