package com.premisave.booking.service;

import com.premisave.booking.dto.mpesa.MpesaCallbackRequest;
import com.premisave.booking.entity.Booking;
import com.premisave.booking.enums.BookingStatus;
import com.premisave.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles Safaricom Daraja M-Pesa STK Push integration.
 *
 * Required environment variables:
 *   MPESA_CONSUMER_KEY
 *   MPESA_CONSUMER_SECRET
 *   MPESA_SHORTCODE
 *   MPESA_PASSKEY
 *   MPESA_CALLBACK_URL
 *   MPESA_ENV        (sandbox | production)
 *   MPESA_BASE_URL   (optional override — defaults to sandbox URL)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaService {

    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;

    @Value("${mpesa.daraja.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.daraja.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.daraja.shortcode}")
    private String shortcode;

    @Value("${mpesa.daraja.passkey}")
    private String passkey;

    @Value("${mpesa.daraja.callback-url}")
    private String callbackUrl;

    @Value("${mpesa.daraja.base-url:https://sandbox.safaricom.co.ke}")
    private String baseUrl;

    private static final DateTimeFormatter MPESA_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ── STK Push ──────────────────────────────────────────────────────────────

    /**
     * Initiates an STK Push to the given phone number.
     *
     * @param phone     Kenyan phone in 254XXXXXXXXX format
     * @param amount    Amount to charge (rounded up to whole KES)
     * @param bookingId Used as the AccountReference
     * @return CheckoutRequestID from Safaricom — store on the booking for callback matching
     */
    public String initiateStk(String phone, BigDecimal amount, String bookingId) {
        String accessToken = getAccessToken();
        String timestamp   = LocalDateTime.now().format(MPESA_TIMESTAMP);
        String password    = buildPassword(timestamp);

        Map<String, Object> body = new HashMap<>();
        body.put("BusinessShortCode", shortcode);
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("TransactionType", "CustomerPayBillOnline");
        body.put("Amount", amount.setScale(0, RoundingMode.CEILING).intValue());
        body.put("PartyA", phone);
        body.put("PartyB", shortcode);
        body.put("PhoneNumber", phone);
        body.put("CallBackURL", callbackUrl);
        body.put("AccountReference", bookingId);
        body.put("TransactionDesc", "Premisave booking payment");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                stkPushUrl(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<?, ?> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("CheckoutRequestID")) {
            throw new RuntimeException("Invalid STK push response from Safaricom");
        }

        String checkoutRequestId = (String) responseBody.get("CheckoutRequestID");
        log.info("STK push sent - Booking: {}, CheckoutRequestID: {}", bookingId, checkoutRequestId);
        return checkoutRequestId;
    }

    // ── Callback processing ───────────────────────────────────────────────────

    /**
     * Processes the async STK Push result sent by Safaricom.
     * Updates the booking status to CONFIRMED or PAYMENT_FAILED.
     */
    public void processCallback(MpesaCallbackRequest callbackRequest) {
        var stkCallback = callbackRequest.getBody().getStkCallback();

        String checkoutRequestId = stkCallback.getCheckoutRequestID();
        int resultCode           = stkCallback.getResultCode();

        log.info("Processing M-Pesa callback - CheckoutRequestID: {}, ResultCode: {}",
                checkoutRequestId, resultCode);

        Booking booking = bookingRepository
                .findByMpesaCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> {
                    log.warn("No booking found for CheckoutRequestID: {}", checkoutRequestId);
                    return new RuntimeException("Booking not found for checkout request: " + checkoutRequestId);
                });

        if (resultCode == 0) {
            booking.setStatus(BookingStatus.CONFIRMED);
            log.info("Payment confirmed for booking {}", booking.getId());
        } else {
            booking.setStatus(BookingStatus.PAYMENT_FAILED);
            log.warn("Payment failed for booking {} - ResultDesc: {}",
                    booking.getId(), stkCallback.getResultDesc());
        }

        bookingRepository.save(booking);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String getAccessToken() {
        String credentials = Base64.getEncoder().encodeToString(
                (consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + credentials);

        ResponseEntity<Map> response = restTemplate.exchange(
                oauthUrl(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<?, ?> body = response.getBody();
        if (body == null || !body.containsKey("access_token")) {
            throw new RuntimeException("Failed to obtain M-Pesa access token");
        }

        return (String) body.get("access_token");
    }

    private String buildPassword(String timestamp) {
        String raw = shortcode + passkey + timestamp;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String oauthUrl() {
        return baseUrl + "/oauth/v1/generate?grant_type=client_credentials";
    }

    private String stkPushUrl() {
        return baseUrl + "/mpesa/stkpush/v1/processrequest";
    }
}