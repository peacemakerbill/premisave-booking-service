package com.premisave.booking.repository;

import com.premisave.booking.entity.Favorite;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends MongoRepository<Favorite, String> {

    List<Favorite> findByUserId(String userId);

    Optional<Favorite> findByUserIdAndListingId(String userId, String listingId);

    void deleteByUserIdAndListingId(String userId, String listingId);

    boolean existsByUserIdAndListingId(String userId, String listingId);
}