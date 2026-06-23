package com.premisave.booking.repository;

import com.premisave.booking.entity.Booking;
import com.premisave.booking.enums.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByUserId(String userId);

    List<Booking> findByListingId(String listingId);

    List<Booking> findByOwnerId(String ownerId);

    Optional<Booking> findByIdAndUserId(String id, String userId);

    Optional<Booking> findByMpesaCheckoutRequestId(String checkoutRequestId);

    /** For checking availability conflicts on short-term rentals */
    List<Booking> findByListingIdAndStatusInAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
            String listingId,
            List<BookingStatus> statuses,
            LocalDateTime checkOut,
            LocalDateTime checkIn
    );

    /** For the auto-cancel scheduler: find stale PENDING/PAYMENT_INITIATED bookings */
    List<Booking> findByStatusInAndCreatedAtBefore(
            List<BookingStatus> statuses,
            LocalDateTime cutoff
    );
}