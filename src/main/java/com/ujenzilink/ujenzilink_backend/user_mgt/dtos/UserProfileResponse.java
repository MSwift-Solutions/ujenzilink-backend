package com.ujenzilink.ujenzilink_backend.user_mgt.dtos;

import com.ujenzilink.ujenzilink_backend.auth.enums.Gender;
import com.ujenzilink.ujenzilink_backend.auth.enums.ProfileVisibility;
import com.ujenzilink.ujenzilink_backend.auth.enums.SignupMethod;
import com.ujenzilink.ujenzilink_backend.auth.enums.VerificationStatus;

import java.time.LocalDate;
import java.util.List;

public record UserProfileResponse(
                String firstName,
                String middleName,
                String lastName,
                String phoneNumber,
                String email,
                String username,
                String bio,
                String title,
                Integer yearsOfExperience,
                LocalDate dateOfBirth,
                String location,
                String geoLocation,
                Gender gender,
                List<SocialLink> socialLinks,
                String license,
                String skills,
                VerificationStatus verificationStatus,
                ProfileVisibility profileVisibility,
                SignupMethod signupMethod) {
}
