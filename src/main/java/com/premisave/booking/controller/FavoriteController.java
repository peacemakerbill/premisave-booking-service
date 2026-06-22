package com.premisave.booking.controller;

import com.premisave.booking.dto.FavoriteRequest;
import com.premisave.booking.dto.FavoriteResponse;
import com.premisave.booking.entity.Favorite;
import com.premisave.booking.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<FavoriteResponse> addToFavorites(
            @Valid @RequestBody FavoriteRequest request,
            @RequestHeader("Authorization") String authorization) {

        FavoriteResponse response = favoriteService.addToFavorites(request, authorization);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{listingId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<FavoriteResponse> removeFromFavorites(
            @PathVariable String listingId,
            @RequestHeader("Authorization") String authorization) {

        FavoriteResponse response = favoriteService.removeFromFavorites(listingId, authorization);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Favorite>> getMyFavorites(
            @RequestHeader("Authorization") String authorization) {

        List<Favorite> favorites = favoriteService.getMyFavorites(authorization);
        return ResponseEntity.ok(favorites);
    }
}