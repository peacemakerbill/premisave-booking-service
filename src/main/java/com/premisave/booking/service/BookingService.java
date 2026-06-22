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

        // Fetch listing details from Listing Service
        Object listingObj;
        try {
            listingObj = listingServiceClient.getListingById(request.getListingId(), authorization);
        } catch (Exception e) {
            log.error("Failed to fetch listing {}", request.getListingId(), e);
            throw new RuntimeException("Listing not found or unavailable.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> listing = (Map<String, Object>) listingObj;

        String ownerId = (String) listing.get("ownerId");
        String category = (String) listing.get("category");
        String title = (String) listing.get("title");
        boolean isPromoted = Boolean.TRUE.equals(listing.get("promoted"));

        if (ownerId == null) {
            throw new RuntimeException("Invalid listing: missing owner information.");
        }

        if (!isPromoted) {
            throw new RuntimeException("This listing is not currently promoted.");
        }

        // Route based on listing type
        if ("SHORT_TERM_RENTAL".equals(category)) {
            return handleShortTermBooking(request, userId, ownerId, listing, title);
        } else {
            return handleInquiry(request, userId, ownerId, category, title);
        }
    }

    private BookingResponse handleShortTermBooking(BookingRequest request, String userId, 
                                                   String ownerId, Map<String, Object> listing, String title) {
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(
                request.getCheckInDate(), request.getCheckOutDate());
        
        if (days <= 0) {
            throw new RuntimeException("Check-out date must be after check-in date.");
        }

        BigDecimal price = new BigDecimal(listing.get("price").toString());
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(days));

        if (!isBookingAvailable(request.getListingId(), request.getCheckInDate(), request.getCheckOutDate())) {
            throw new RuntimeException("Selected dates are not available for this listing.");
        }

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

        log.info("Short-term booking created - ID: {}, User: {}, Listing: {}", 
                saved.getId(), userId, request.getListingId());

        return new BookingResponse(
                saved.getId(),
                saved.getListingId(),
                saved.getStatus(),
                saved.getCheckInDate(),
                saved.getCheckOutDate(),
                saved.getNumberOfGuests(),
                saved.getTotalAmount(),
                saved.getCurrency(),
                "Short-term booking created for " + title + ". Please proceed to payment."
        );
    }

    private BookingResponse handleInquiry(BookingRequest request, String userId, 
                                          String ownerId, String category, String title) {
        
        Booking inquiry = new Booking();
        inquiry.setUserId(userId);
        inquiry.setListingId(request.getListingId());
        inquiry.setOwnerId(ownerId);
        inquiry.setCheckInDate(request.getCheckInDate());
        inquiry.setCheckOutDate(request.getCheckOutDate());
        inquiry.setNumberOfGuests(request.getNumberOfGuests());
        inquiry.setTotalAmount(BigDecimal.ZERO);
        inquiry.setCurrency("KES");
        inquiry.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(inquiry);

        log.info("Inquiry created for {} listing - User: {}, Listing: {}", 
                category, userId, request.getListingId());

        return new BookingResponse(
                saved.getId(),
                saved.getListingId(),
                saved.getStatus(),
                saved.getCheckInDate(),
                saved.getCheckOutDate(),
                saved.getNumberOfGuests(),
                BigDecimal.ZERO,
                "KES",
                "Interest registered for " + title + ". The homeowner has been notified."
        );
    }

    private boolean isBookingAvailable(String listingId, LocalDateTime checkIn, LocalDateTime checkOut) {
        List<Booking> conflicts = bookingRepository
                .findByListingIdAndStatusInAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        listingId,
                        List.of(BookingStatus.CONFIRMED.name(), BookingStatus.PENDING.name()),
                        checkOut,
                        checkIn
                );
        return conflicts.isEmpty();
    }

    public List<Booking> getMyBookings(String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) throw new RuntimeException("User not authenticated");
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBookingById(String id, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) throw new RuntimeException("User not authenticated");

        return bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or access denied"));
    }
}