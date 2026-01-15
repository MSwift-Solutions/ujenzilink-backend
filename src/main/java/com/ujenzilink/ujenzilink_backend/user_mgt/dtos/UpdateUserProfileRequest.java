package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

import com.ujenzilink.ujenzilink_backend.auth.enums.Gender;
import com.ujenzilink.ujenzilink_backend.auth.enums.ProfileVisibility;
import com.ujenzilink.ujenzilink_backend.auth.enums.VerificationStatus;
import com.ujenzilink.ujenzilink_backend.auth.validators.Name;
import com.ujenzilink.ujenzilink_backend.auth.validators.PhoneNumber;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateUserProfileRequest(
        @Name(message = "First name must be 2-50 characters and contain only letters, spaces, hyphens, and apostrophes") String firstName,

        @Name(message = "Middle name must be 2-50 characters and contain only letters, spaces, hyphens, and apostrophes") String middleName,

        @Name(message = "Last name must be 2-50 characters and contain only letters, spaces, hyphens, and apostrophes") String lastName,

        @PhoneNumber(message = "Invalid phone number format") String phoneNumber,

        @Size(max = 2000, message = "Bio cannot exceed 2000 characters") String bio,

        @Size(max = 100, message = "Title cannot exceed 100 characters") String title,

        Integer yearsOfExperience,

        LocalDate dateOfBirth,

        @Size(max = 255, message = "Location cannot exceed 255 characters") String location,

        String geoLocation,

        Gender gender,

        // Social links - list of platform/URL pairs
        List<SocialLink> socialLinks,

        // Individual user fields
        @Size(max = 100, message = "License cannot exceed 100 characters") String license,

        @Size(max = 1000, message = "Skills cannot exceed 1000 characters") String skills,

        VerificationStatus verificationStatus,

        ProfileVisibility profileVisibility) {
}
