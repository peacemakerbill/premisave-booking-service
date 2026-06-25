package com.premisave.booking.controller;

import com.premisave.booking.dto.ApiResponse;
import com.premisave.booking.security.JwtService;
import com.premisave.booking.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@Tag(name = "System", description = "Health checks and debugging endpoints")
public class SystemController {

    private final JwtService jwtService;
    private final JwtUtil jwtUtil;

    @GetMapping("/health")
    @Operation(summary = "Health check for Booking Service")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "premisave-booking-service",
                "timestamp", LocalDateTime.now().toString(),
                "version", "0.0.1-SNAPSHOT"
        ));
    }

    @GetMapping("/test-token")
    @Operation(summary = "Decode and display current JWT token details (for debugging)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testToken(
            @RequestHeader("Authorization") String authorization) {

        Map<String, Object> data = new HashMap<>();
        String token = jwtUtil.stripBearer(authorization);

        data.put("hasValidToken", token != null);
        data.put("rawTokenLength", token != null ? token.length() : 0);

        if (token == null) {
            data.put("message", "No Bearer token found in Authorization header");
        } else {
            try {
                data.put("userId", jwtUtil.extractUserId(authorization));
                data.put("role", jwtUtil.extractRole(authorization));
                data.put("isValid", jwtUtil.isTokenValid(authorization));
                data.put("expiresAt", jwtService.getTokenExpiration(token));
                data.put("expiresAtFormatted", jwtService.getTokenExpiration(token) != null 
                        ? jwtService.getTokenExpiration(token).toString() : null);
            } catch (Exception e) {
                data.put("parsingError", e.getMessage());
                data.put("errorClass", e.getClass().getSimpleName());
            }
        }

        return ResponseEntity.ok(
            ApiResponse.success("Token analysis complete", data)
        );
    }
}