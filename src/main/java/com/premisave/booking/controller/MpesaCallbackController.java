package com.premisave.booking.controller;

import com.premisave.booking.dto.mpesa.MpesaCallbackRequest;
import com.premisave.booking.service.MpesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Receives asynchronous payment results from Safaricom Daraja.
 *
 *  This endpoint is PUBLIC (no auth) — Safaricom does not send Bearer tokens.
 *     It is listed in SecurityConfig.permitAll() and WebConfig exclusions.
 *
 *  URL must be publicly reachable. Configure MPESA_CALLBACK_URL env var.
 *     For local dev use ngrok: ngrok http 8083
 */
@Slf4j
@RestController
@RequestMapping("/bookings/payments/mpesa")
@RequiredArgsConstructor
@Tag(name = "M-Pesa Callbacks", description = "Safaricom Daraja callback endpoints — do not call directly")
public class MpesaCallbackController {

    private final MpesaService mpesaService;

    /**
     * STK Push callback — called by Safaricom when user completes or dismisses the payment prompt.
     * Must respond with HTTP 200 and a specific JSON body, otherwise Safaricom will retry.
     */
    @PostMapping("/callback")
    @Operation(summary = "M-Pesa STK Push callback — called by Safaricom (not by clients)")
    public ResponseEntity<Map<String, Object>> handleStkCallback(
            @RequestBody MpesaCallbackRequest callbackRequest) {

        log.info("M-Pesa STK callback received");

        try {
            mpesaService.processCallback(callbackRequest);
        } catch (Exception e) {
            // Always return 200 to Safaricom — even on internal error — to prevent retries
            log.error("Error processing M-Pesa callback: {}", e.getMessage(), e);
        }

        // Safaricom expects this exact structure
        return ResponseEntity.ok(Map.of(
                "ResultCode", 0,
                "ResultDesc", "Accepted"
        ));
    }
}