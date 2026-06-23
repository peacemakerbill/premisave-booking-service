package com.premisave.booking.controller;

import com.premisave.booking.dto.ApiResponse;
import com.premisave.booking.dto.FavoriteRequest;
import com.premisave.booking.dto.FavoriteResponse;
import com.premisave.booking.entity.Favorite;
import com.premisave.booking.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Manage saved listing favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Add a listing to favorites")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addToFavorites(
            @Valid @RequestBody FavoriteRequest request,
            @RequestHeader("Authorization") String authorization) {

        FavoriteResponse response = favoriteService.addToFavorites(request, authorization);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    @DeleteMapping("/{listingId}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Remove a listing from favorites")
    public ResponseEntity<ApiResponse<FavoriteResponse>> removeFromFavorites(
            @PathVariable String listingId,
            @RequestHeader("Authorization") String authorization) {

        FavoriteResponse response = favoriteService.removeFromFavorites(listingId, authorization);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get all favorites for the authenticated user")
    public ResponseEntity<ApiResponse<List<Favorite>>> getMyFavorites(
            @RequestHeader("Authorization") String authorization) {

        List<Favorite> favorites = favoriteService.getMyFavorites(authorization);
        return ResponseEntity.ok(ApiResponse.success("Favorites fetched", favorites));
    }
}