package com.premisave.booking.service;

import com.premisave.booking.client.ListingServiceClient;
import com.premisave.booking.dto.FavoriteRequest;
import com.premisave.booking.dto.FavoriteResponse;
import com.premisave.booking.entity.Favorite;
import com.premisave.booking.repository.FavoriteRepository;
import com.premisave.booking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ListingServiceClient listingServiceClient;
    private final JwtUtil jwtUtil;

    @Transactional
    public FavoriteResponse addToFavorites(FavoriteRequest request, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);

        if (userId == null) {
            throw new RuntimeException("Unable to authenticate user. Please login again.");
        }

        // Check if already in favorites
        if (favoriteRepository.existsByUserIdAndListingId(userId, request.getListingId())) {
            return new FavoriteResponse("Already in favorites", true, request.getListingId());
        }

        // Optional: Verify listing exists
        try {
            listingServiceClient.getListingById(request.getListingId(), authorization);
        } catch (Exception e) {
            log.warn("Could not verify listing {}: {}", request.getListingId(), e.getMessage());
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setListingId(request.getListingId());

        favoriteRepository.save(favorite);

        log.info("User {} added listing {} to favorites", userId, request.getListingId());

        return new FavoriteResponse("Added to favorites successfully", true, request.getListingId());
    }

    @Transactional
    public FavoriteResponse removeFromFavorites(String listingId, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);

        if (userId == null) {
            throw new RuntimeException("Unable to authenticate user. Please login again.");
        }

        favoriteRepository.deleteByUserIdAndListingId(userId, listingId);

        log.info("User {} removed listing {} from favorites", userId, listingId);

        return new FavoriteResponse("Removed from favorites successfully", true, listingId);
    }

    public List<Favorite> getMyFavorites(String authorization) {
        String userId = jwtUtil.extractUserId(authorization);

        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        return favoriteRepository.findByUserId(userId);
    }
}