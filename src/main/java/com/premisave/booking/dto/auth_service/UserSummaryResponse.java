package com.premisave.booking.dto.auth_service;

import lombok.Data;

@Data
public class UserSummaryResponse {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String displayName;
    private String profilePictureUrl;
    private String email;
    private String phoneNumber;
    private String country;
}