package com.premisave.booking.service;

import com.premisave.booking.client.ListingServiceClient;
import com.premisave.booking.dto.BookingRequest;
import com.premisave.booking.dto.BookingResponse;
import com.premisave.booking.entity.Booking;
import com.premisave.booking.enums.BookingStatus;
import com.premisave.booking.repository.BookingRepository;
import com.premisave.booking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ListingServiceClient listingServiceClient;
    private final JwtUtil jwtUtil;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);

        // TODO: Call ListingService to get price, ownerId, availability, etc.
        // For now, placeholder logic
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setListingId(request.getListingId());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setTotalAmount(BigDecimal.valueOf(5000)); // TODO: Calculate from listing
        booking.setCurrency("KES");
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        log.info("New booking created for user {} on listing {}", userId, request.getListingId());

        return new BookingResponse(
                saved.getId(),
                saved.getListingId(),
                saved.getStatus(),
                saved.getCheckInDate(),
                saved.getCheckOutDate(),
                saved.getNumberOfGuests(),
                saved.getTotalAmount(),
                saved.getCurrency(),
                "Booking created successfully. Proceed to payment.",
                true
        );
    }

    public List<Booking> getMyBookings(String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBookingById(String id, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        return bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or access denied"));
    }
}