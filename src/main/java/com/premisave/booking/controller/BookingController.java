package com.premisave.booking.controller;

import com.premisave.booking.dto.BookingRequest;
import com.premisave.booking.dto.BookingResponse;
import com.premisave.booking.entity.Booking;
import com.premisave.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader("Authorization") String authorization) {

        BookingResponse response = bookingService.createBooking(request, authorization);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Booking>> getMyBookings(
            @RequestHeader("Authorization") String authorization) {

        List<Booking> bookings = bookingService.getMyBookings(authorization);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization) {

        Booking booking = bookingService.getBookingById(id, authorization);
        return ResponseEntity.ok(booking);
    }
}