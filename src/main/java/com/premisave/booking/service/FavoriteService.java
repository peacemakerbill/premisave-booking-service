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
        String userId = jwtUtil.extractUserId(authorization); // Implement extraction properly

        if (favoriteRepository.existsByUserIdAndListingId(userId, request.getListingId())) {
            return new FavoriteResponse("Already in favorites", true, request.getListingId());
        }

        // Optional: Verify listing exists
        try {
            listingServiceClient.getListingById(request.getListingId(), authorization);
        } catch (Exception e) {
            log.warn("Listing not found or inaccessible: {}", request.getListingId());
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

        favoriteRepository.deleteByUserIdAndListingId(userId, listingId);

        log.info("User {} removed listing {} from favorites", userId, listingId);
        return new FavoriteResponse("Removed from favorites", true, listingId);
    }

    public List<Favorite> getMyFavorites(String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        return favoriteRepository.findByUserId(userId);
    }
}