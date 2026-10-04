package com.ridelink.farepayment.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtService.isTokenValid(token)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String role = jwtService.extractRole(token);
                    if (role != null && !role.isBlank()) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                jwtService.parseClaims(token).getSubject(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (RuntimeException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
