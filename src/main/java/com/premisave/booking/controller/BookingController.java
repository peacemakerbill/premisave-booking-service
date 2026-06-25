package com.premisave.booking.controller;

import com.premisave.booking.dto.ApiResponse;
import com.premisave.booking.dto.BookingRequest;
import com.premisave.booking.dto.BookingResponse;
import com.premisave.booking.dto.mpesa.MpesaPaymentRequest;
import com.premisave.booking.entity.Booking;
import com.premisave.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management and M-Pesa payment endpoints")
public class BookingController {

    private final BookingService bookingService;

    // ── Client endpoints ──────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Create a booking or inquiry for a listing")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader("Authorization") String authorization) {

        BookingResponse response = bookingService.createBooking(request, authorization);
        return ResponseEntity.ok(ApiResponse.success("Booking created successfully", response));
    }

    @PostMapping("/payments/mpesa")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Initiate M-Pesa STK push payment for a PENDING booking")
    public ResponseEntity<ApiResponse<BookingResponse>> initiatePayment(
            @Valid @RequestBody MpesaPaymentRequest paymentRequest,
            @RequestHeader("Authorization") String authorization) {

        BookingResponse response = bookingService.initiatePayment(paymentRequest, authorization);
        return ResponseEntity.ok(ApiResponse.success("M-Pesa payment initiated", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get all bookings for the authenticated user")
    public ResponseEntity<ApiResponse<List<Booking>>> getMyBookings(
            @RequestHeader("Authorization") String authorization) {

        List<Booking> bookings = bookingService.getMyBookings(authorization);
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched", bookings));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get a single booking by ID (must belong to authenticated user)")
    public ResponseEntity<ApiResponse<Booking>> getBookingById(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {

        Booking booking = bookingService.getBookingById(id, authorization);
        return ResponseEntity.ok(ApiResponse.success("Booking fetched", booking));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Cancel a PENDING booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable String id,
            @RequestParam(required = false) String reason,
            @RequestHeader("Authorization") String authorization) {

        BookingResponse response = bookingService.cancelBooking(id, reason, authorization);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", response));
    }

    // ── Owner endpoints ───────────────────────────────────────────────────────

    @GetMapping("/owner/mine")
    @PreAuthorize("hasRole('HOME_OWNER')")
    @Operation(summary = "Get all bookings for listings owned by the authenticated owner")
    public ResponseEntity<ApiResponse<List<Booking>>> getMyListingBookings(
            @RequestHeader("Authorization") String authorization) {

        List<Booking> bookings = bookingService.getBookingsByOwner(authorization);
        return ResponseEntity.ok(ApiResponse.success("Owner bookings fetched", bookings));
    }

    @GetMapping("/listing/{listingId}")
    @PreAuthorize("hasRole('HOME_OWNER')")
    @Operation(summary = "Get all bookings for a specific listing")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookingsByListing(
            @PathVariable String listingId,
            @RequestHeader("Authorization") String authorization) {

        List<Booking> bookings = bookingService.getBookingsByListing(listingId, authorization);
        return ResponseEntity.ok(ApiResponse.success("Listing bookings fetched", bookings));
    }
}