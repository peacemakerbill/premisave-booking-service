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

        if (favoriteRepository.existsByUserIdAndListingId(userId, request.getListingId())) {
            return FavoriteResponse.builder()
                    .message("Already in favorites")
                    .success(true)
                    .listingId(request.getListingId())
                    .build();
        }

        // Optional: verify listing exists and grab title
        String listingTitle = null;
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> listing =
                    (java.util.Map<String, Object>) listingServiceClient.getListingById(request.getListingId(), authorization);
            listingTitle = (String) listing.get("title");
        } catch (Exception e) {
            log.warn("Could not verify listing {}: {}", request.getListingId(), e.getMessage());
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setListingId(request.getListingId());

        favoriteRepository.save(favorite);

        log.info("User {} added listing {} to favorites", userId, request.getListingId());

        return FavoriteResponse.builder()
                .message("Added to favorites successfully")
                .success(true)
                .listingId(request.getListingId())
                .listingTitle(listingTitle)
                .build();
    }

    @Transactional
    public FavoriteResponse removeFromFavorites(String listingId, String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) {
            throw new RuntimeException("Unable to authenticate user. Please login again.");
        }

        favoriteRepository.deleteByUserIdAndListingId(userId, listingId);

        log.info("User {} removed listing {} from favorites", userId, listingId);

        return FavoriteResponse.builder()
                .message("Removed from favorites successfully")
                .success(true)
                .listingId(listingId)
                .build();
    }

    public List<Favorite> getMyFavorites(String authorization) {
        String userId = jwtUtil.extractUserId(authorization);
        if (userId == null) throw new RuntimeException("User not authenticated");
        return favoriteRepository.findByUserId(userId);
    }
}