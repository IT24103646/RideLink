package com.ridelink.ride.security;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        // No Authorization header
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        String token = authorizationHeader.substring(7).trim();

        try {

            // Validate token
            if (jwtService.isTokenValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String subject = jwtService.extractSubject(token);
                String role = jwtService.extractRole(token);

                // DEBUG
                System.out.println("======================================");
                System.out.println("JWT SUBJECT = " + subject);
                System.out.println("JWT ROLE    = " + role);
                System.out.println("======================================");

                // Role must exist
                if (role == null || role.isBlank()) {
                    System.out.println("JWT ROLE IS NULL OR EMPTY");
                    SecurityContextHolder.clearContext();

                    filterChain.doFilter(request, response);
                    return;
                }

                /*
                 * Account Service may send:
                 *
                 * PASSENGER
                 * or
                 * ROLE_PASSENGER
                 *
                 * We normalize both to:
                 *
                 * ROLE_PASSENGER
                 */

                String normalizedRole = role.trim().toUpperCase(Locale.ROOT);

                if (!normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = "ROLE_" + normalizedRole;
                }

                System.out.println("SPRING AUTHORITY = " + normalizedRole);

                // Create Spring Security authentication
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                subject,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(normalizedRole)
                                )
                        );

                // Store authentication in SecurityContext
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                System.out.println("AUTHENTICATION SET SUCCESSFULLY");
            }

        } catch (RuntimeException exception) {

            System.out.println("======================================");
            System.out.println("JWT AUTHENTICATION FAILED");
            System.out.println("ERROR = " + exception.getMessage());
            System.out.println("======================================");

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}