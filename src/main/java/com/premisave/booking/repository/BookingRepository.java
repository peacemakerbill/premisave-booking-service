package com.premisave.booking.repository;

import com.premisave.booking.entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByUserId(String userId);
    List<Booking> findByListingId(String listingId);
    List<Booking> findByOwnerId(String ownerId);

    Optional<Booking> findByIdAndUserId(String id, String userId);

    // For checking availability conflicts
    List<Booking> findByListingIdAndStatusInAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqual(
            String listingId, List<String> statuses, LocalDateTime checkOut, LocalDateTime checkIn);
}