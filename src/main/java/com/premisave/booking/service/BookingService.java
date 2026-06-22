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
import java.util.Map;

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

        if (userId == null) {
            throw new RuntimeException("Unable to authenticate user. Please login again.");
        }

        // ====================== FETCH LISTING DETAILS ======================
        Object listingObj;
        try {
            listingObj = listingServiceClient.getListingById(request.getListingId(), authorization);
        } catch (Exception e) {
            log.error("Failed to fetch listing {}", request.getListingId(), e);
            throw new RuntimeException("Listing not found or unavailable.");
        }

        if (listingObj == null) {
            throw new RuntimeException("Listing not found.");
        }

        // Extract data from response (ListingService returns different types based on category)
        @SuppressWarnings("unchecked")
        Map<String, Object> listing = (Map<String, Object>) listingObj;

        String ownerId = (String) listing.get("ownerId");
        BigDecimal price = new BigDecimal(listing.get("price").toString());
        String listingTitle = (String) listing.get("title");
        boolean isPromoted = Boolean.TRUE.equals(listing.get("promoted"));

        if (ownerId == null) {
            throw new RuntimeException("Invalid listing: missing owner information.");
        }

        if (!isPromoted) {
            throw new RuntimeException("This listing is not currently promoted and cannot be booked.");
        }

        // ====================== AVAILABILITY CHECK ======================
        boolean isAvailable = isBookingAvailable(request.getListingId(), 
                                                request.getCheckInDate(), 
                                                request.getCheckOutDate());
        if (!isAvailable) {
            throw new RuntimeException("Selected dates are not available for this listing.");
        }

        // ====================== CALCULATE TOTAL AMOUNT ======================
        long days = java.time.temporal.ChronoUnit.DAYS.between(
                request.getCheckInDate(), request.getCheckOutDate());
        
        if (days <= 0) {
            throw new RuntimeException("Check-out date must be after check-in date.");
        }

        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(days));

        // ====================== CREATE BOOKING ======================
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setListingId(request.getListingId());
        booking.setOwnerId(ownerId);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setTotalAmount(totalAmount);
        booking.setCurrency("KES");
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        log.info("Booking created successfully - BookingId: {}, User: {}, Listing: {}, Amount: {}", 
                saved.getId(), userId, request.getListingId(), totalAmount);

        return new BookingResponse(
                saved.getId(),
                saved.getListingId(),
                saved.getStatus(),
                saved.getCheckInDate(),
                saved.getCheckOutDate(),
                saved.getNumberOfGuests(),
                saved.getTotalAmount(),
                saved.getCurrency(),
                "Booking created successfully for " + listingTitle + ". Please proceed to payment."
        );
    }

    /**
     * Checks if the requested dates overlap with any existing confirmed bookings
     */
    private boolean isBookingAvailable(String listingId, LocalDateTime checkIn, LocalDateTime checkOut) {
        List<Booking> conflictingBookings = bookingRepository
                .findByListingIdAndStatusInAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        listingId,
                        List.of(BookingStatus.CONFIRMED.name(), BookingStatus.PENDING.name()),
                        checkOut,
                        checkIn
                );

        return conflictingBookings.isEmpty();
    }

    public List<Booking> getMyBookings(String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBookingById(String id, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        return bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or access denied"));
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingId, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or access denied"));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return new BookingResponse(
                booking.getId(),
                booking.getListingId(),
                booking.getStatus(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfGuests(),
                booking.getTotalAmount(),
                booking.getCurrency(),
                "Booking cancelled successfully."
        );
    }
}