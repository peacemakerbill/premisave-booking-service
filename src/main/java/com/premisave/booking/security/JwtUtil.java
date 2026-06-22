package com.premisave.booking.security;

import com.premisave.booking.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final AuthServiceClient authServiceClient; // Optional: can be used for extra validation

    public String extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring(7);
        // You can call JWT parsing service or extract directly if you have JwtService
        // For now, we'll assume we extract from token in filter
        return null; // Will be properly implemented in filter/service
    }
}