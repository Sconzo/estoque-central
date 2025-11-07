package com.estoquecentral.auth.adapter.in.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * AssignProfileRequest - Request DTO for assigning a profile to a user
 */
public class AssignProfileRequest {

    @NotNull(message = "Profile ID is required")
    private UUID profileId;

    public AssignProfileRequest() {
    }

    public AssignProfileRequest(UUID profileId) {
        this.profileId = profileId;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }
}
