package com.premisave.booking.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility for extracting claims from a raw Authorization header value.
 *
 * FIX: Previously always returned null — now delegates to JwtService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtService jwtService;

    /**
     * Extract the userId from an Authorization header value.
     *
     * @param authorizationHeader e.g. "Bearer eyJhbGci..."
     * @return userId string, or null if the header is missing/invalid
     */
    public String extractUserId(String authorizationHeader) {
        String token = stripBearer(authorizationHeader);
        if (token == null) return null;

        try {
            return jwtService.extractUserId(token);
        } catch (Exception e) {
            log.warn("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract the role from an Authorization header value.
     *
     * @param authorizationHeader e.g. "Bearer eyJhbGci..."
     * @return role string (e.g. "CLIENT"), or null if extraction fails
     */
    public String extractRole(String authorizationHeader) {
        String token = stripBearer(authorizationHeader);
        if (token == null) return null;

        try {
            return jwtService.extractRole(token);
        } catch (Exception e) {
            log.warn("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }

    private String stripBearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }
}