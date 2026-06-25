package com.premisave.booking.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility for extracting claims from Authorization header.
 * Delegates actual JWT parsing to JwtService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtService jwtService;

    /**
     * Extract userId from Authorization header.
     *
     * @param authorizationHeader e.g. "Bearer eyJhbGci..."
     * @return userId or null if invalid/missing
     */
    public String extractUserId(String authorizationHeader) {
        String token = stripBearer(authorizationHeader);
        if (token == null) return null;

        try {
            String userId = jwtService.extractUserId(token);
            log.debug("Extracted userId: {}", userId);
            return userId;
        } catch (Exception e) {
            log.warn("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract role from Authorization header.
     *
     * @param authorizationHeader e.g. "Bearer eyJhbGci..."
     * @return role (e.g. "CLIENT") or null if invalid
     */
    public String extractRole(String authorizationHeader) {
        String token = stripBearer(authorizationHeader);
        if (token == null) return null;

        try {
            String role = jwtService.extractRole(token);
            log.debug("Extracted role: {}", role);
            return role;
        } catch (Exception e) {
            log.warn("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates if the token in the Authorization header is valid.
     */
    public boolean isTokenValid(String authorizationHeader) {
        String token = stripBearer(authorizationHeader);
        if (token == null) return false;
        return jwtService.isTokenValid(token);
    }

    /**
     * Strips "Bearer " prefix from Authorization header.
     *
     * @param header full Authorization header value
     * @return clean JWT token or null if invalid
     */
    public String stripBearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7).trim();
    }
}