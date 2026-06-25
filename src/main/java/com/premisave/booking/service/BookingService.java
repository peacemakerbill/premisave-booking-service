package com.premisave.booking.service;

import com.premisave.booking.client.ListingServiceClient;
import com.premisave.booking.dto.BookingRequest;
import com.premisave.booking.dto.BookingResponse;
import com.premisave.booking.dto.listing_service.ListingResponse;
import com.premisave.booking.dto.mpesa.MpesaPaymentRequest;
import com.premisave.booking.entity.Booking;
import com.premisave.booking.enums.BookingStatus;
import com.premisave.booking.enums.BookingType;
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
    private final MpesaService mpesaService;
    private final JwtUtil jwtUtil;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) {
            throw new RuntimeException("Unable to authenticate user. Please login again.");
        }

        ListingResponse listing = fetchListing(request.getListingId(), authorization);

        String ownerId    = listing.getOwnerId();
        String category   = listing.getCategory();
        String title      = listing.getTitle();
        boolean isPromoted = listing.isPromoted();

        if (ownerId == null) {
            throw new RuntimeException("Invalid listing: missing owner information.");
        }

        if (!isPromoted) {
            throw new RuntimeException("This listing is not currently promoted and cannot be booked.");
        }

        if ("SHORT_TERM_RENTAL".equals(category)) {
            return handleShortTermBooking(request, userId, ownerId, listing, title);
        } else {
            return handleInquiry(request, userId, ownerId, category, title);
        }
    }

    // ── Payment ───────────────────────────────────────────────────────────────

    @Transactional
    public BookingResponse initiatePayment(MpesaPaymentRequest paymentRequest, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) {
            throw new RuntimeException("Unable to authenticate user. Please login again.");
        }

        Booking booking = bookingRepository.findByIdAndUserId(paymentRequest.getBookingId(), userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or access denied"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be paid. Current status: " + booking.getStatus());
        }

        String checkoutRequestId = mpesaService.initiateStk(
                paymentRequest.getPhoneNumber(),
                booking.getTotalAmount(),
                booking.getId()
        );

        booking.setMpesaCheckoutRequestId(checkoutRequestId);
        bookingRepository.save(booking);

        log.info("M-Pesa STK push initiated - Booking: {}, CheckoutRequestId: {}", booking.getId(), checkoutRequestId);

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .listingId(booking.getListingId())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .currency(booking.getCurrency())
                .mpesaCheckoutRequestId(checkoutRequestId)
                .message("M-Pesa payment prompt sent. Please enter your PIN.")
                .success(true)
                .build();
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Transactional
    public BookingResponse cancelBooking(String id, String reason, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) {
            throw new RuntimeException("Unable to authenticate user. Please login again.");
        }

        Booking booking = bookingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found or access denied"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be cancelled. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        Booking saved = bookingRepository.save(booking);

        log.info("Booking {} cancelled by user {}", id, userId);

        return BookingResponse.builder()
                .bookingId(saved.getId())
                .listingId(saved.getListingId())
                .status(saved.getStatus())
                .totalAmount(saved.getTotalAmount())
                .currency(saved.getCurrency())
                .message("Booking cancelled successfully.")
                .success(true)
                .build();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

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

    public List<Booking> getBookingsByOwner(String authorization) {
        String ownerId = jwtUtil.extractUserId(authorization);
        if (ownerId == null) throw new RuntimeException("User not authenticated");
        return bookingRepository.findByOwnerId(ownerId);
    }

    public List<Booking> getBookingsByListing(String listingId, String authorization) {
        String ownerId = jwtUtil.extractUserId(authorization);
        if (ownerId == null) throw new RuntimeException("User not authenticated");
        List<Booking> bookings = bookingRepository.findByListingId(listingId);
        bookings.forEach(b -> {
            if (!b.getOwnerId().equals(ownerId)) {
                throw new RuntimeException("Access denied: listing does not belong to you");
            }
        });
        return bookings;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ListingResponse fetchListing(String listingId, String authorization) {
        try {
            return listingServiceClient.getListingById(listingId, authorization);
        } catch (Exception e) {
            log.error("Failed to fetch listing {}: {}", listingId, e.getMessage(), e);
            throw new RuntimeException("Listing not found or unavailable.");
        }
    }

    private BookingResponse handleShortTermBooking(BookingRequest request, String userId,
                                                   String ownerId, ListingResponse listing,
                                                   String title) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(
                request.getCheckInDate(), request.getCheckOutDate());

        if (days <= 0) {
            throw new RuntimeException("Check-out date must be after check-in date.");
        }

        if (!isBookingAvailable(request.getListingId(), request.getCheckInDate(), request.getCheckOutDate())) {
            throw new RuntimeException("Selected dates are not available for this listing.");
        }

        BigDecimal price       = listing.getPrice();
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(days));

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

        return BookingResponse.builder()
                .bookingId(saved.getId())
                .listingId(saved.getListingId())
                .listingTitle(title)
                .bookingType(BookingType.SHORT_TERM_RENTAL)
                .status(saved.getStatus())
                .checkInDate(saved.getCheckInDate())
                .checkOutDate(saved.getCheckOutDate())
                .numberOfGuests(saved.getNumberOfGuests())
                .totalAmount(saved.getTotalAmount())
                .currency(saved.getCurrency())
                .message("Booking created for " + title + ". Please proceed to payment.")
                .success(true)
                .build();
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

        return BookingResponse.builder()
                .bookingId(saved.getId())
                .listingId(saved.getListingId())
                .listingTitle(title)
                .bookingType(BookingType.INQUIRY)
                .status(saved.getStatus())
                .checkInDate(saved.getCheckInDate())
                .checkOutDate(saved.getCheckOutDate())
                .numberOfGuests(saved.getNumberOfGuests())
                .totalAmount(BigDecimal.ZERO)
                .currency("KES")
                .message("Interest registered for " + title + ". The homeowner has been notified.")
                .success(true)
                .build();
    }

    private boolean isBookingAvailable(String listingId, LocalDateTime checkIn, LocalDateTime checkOut) {
        List<Booking> conflicts = bookingRepository
                .findByListingIdAndStatusInAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
                        listingId,
                        List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING, BookingStatus.PAYMENT_INITIATED),
                        checkOut,
                        checkIn
                );
        return conflicts.isEmpty();
    }
}