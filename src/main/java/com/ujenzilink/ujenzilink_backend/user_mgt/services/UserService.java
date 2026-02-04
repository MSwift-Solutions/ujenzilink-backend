package com.ujenzilink.ujenzilink_backend.user_mgt.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectRepository;
import com.ujenzilink.ujenzilink_backend.projects.repositories.ProjectStageRepository;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.*;
import com.ujenzilink.ujenzilink_backend.user_mgt.models.Bio;
import com.ujenzilink.ujenzilink_backend.user_mgt.repositories.BioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BioRepository bioRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectStageRepository projectStageRepository;


    public UserService(UserRepository userRepository, BioRepository bioRepository, SecurityUtil securityUtil) {
        this.userRepository = userRepository;
        this.bioRepository = bioRepository;
        this.securityUtil = securityUtil;
    }

    public ApiCustomResponse<String> deleteUser() {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated or not found.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();
        user.setIsDeleted(true);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        return new ApiCustomResponse<>(
                "User deleted successfully",
                "User account has been deactivated",
                HttpStatus.OK.value());
    }

    private String formatUserCount(long count) {
        if (count >= 1_000_000) {
            double millions = count / 1_000_000.0;
            if (millions == (long) millions) {
                return String.format("%d million %s", (long) millions, millions == 1 ? "user" : "users");
            } else {
                return String.format("%.1f million users", millions);
            }
        } else if (count >= 1_000) {
            double thousands = count / 1_000.0;
            if (thousands == (long) thousands) {
                return String.format("%d thousand %s", (long) thousands, thousands == 1 ? "user" : "users");
            } else {
                return String.format("%.1f thousand users", thousands);
            }
        } else {
            return String.format("%d %s", count, count == 1 ? "user" : "users");
        }
    }

    public ApiCustomResponse<UserCountResponseDto> getUserCount() {
        long totalUsers = userRepository.count();
        String formattedCount = formatUserCount(totalUsers);

        List<User> allUsers = userRepository.findAll();
        Collections.shuffle(allUsers);

        List<UserInfoDto> randomUsers = new ArrayList<>();
        int userCount = Math.min(4, allUsers.size());

        for (int i = 0; i < userCount; i++) {
            User user = allUsers.get(i);
            String name = user.getFullName();
            String profileUrl = (user.getProfilePicture() != null)
                    ? user.getProfilePicture().getUrl()
                    : "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random";
            randomUsers.add(new UserInfoDto(name, profileUrl));
        }

        UserCountResponseDto responseDto = new UserCountResponseDto(formattedCount, randomUsers);

        return new ApiCustomResponse<>(
                responseDto,
                "User count retrieved successfully",
                HttpStatus.OK.value());
    }

    public ApiCustomResponse<List<TestimonialItemDto>> getTestimonials() {
        // Placeholder quotes
        String[] quotes = {
                "The platform has transformed how we present our portfolio to potential clients. It's intuitive and professional.",
                "Outstanding service! UjenziLink helped us win more contracts by showcasing our work effectively.",
                "A game-changer for our construction business. The detailed project listings attract quality leads consistently.",
                "Working with UjenziLink has been amazing. Our project visibility has increased significantly.",
                "The best platform for construction professionals. Highly recommend to anyone in the industry.",
                "UjenziLink made it easy to connect with clients and showcase our expertise."
        };

        List<User> allUsers = userRepository.findAll();
        Collections.shuffle(allUsers);

        List<TestimonialItemDto> testimonials = new ArrayList<>();
        int testimonialCount = Math.min(6, allUsers.size());

        for (int i = 0; i < testimonialCount; i++) {
            User user = allUsers.get(i);
            String name = user.getFullName();
            String title = user.getUserHandle() != null ? user.getUserHandle() : "User";
            String profileUrl = (user.getProfilePicture() != null)
                    ? user.getProfilePicture().getUrl()
                    : "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random";
            String quote = quotes[i % quotes.length];

            testimonials.add(new TestimonialItemDto(quote, name, title, profileUrl));
        }

        return new ApiCustomResponse<>(
                testimonials,
                "Testimonials retrieved successfully",
                HttpStatus.OK.value());
    }

    public ApiCustomResponse<UserSummaryResponse> getMySummary() {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated or not found.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        // Build profile image URL
        String name = user.getFullName();
        String profileUrl = (user.getProfilePicture() != null)
                ? user.getProfilePicture().getUrl()
                : "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random";

        // Get bio and build social links
        List<SocialLink> socialLinks = new ArrayList<>();
        Optional<Bio> bioOpt = bioRepository.findByUser(user);

        if (bioOpt.isPresent()) {
            Bio bio = bioOpt.get();

            if (bio.getTiktok() != null && !bio.getTiktok().isEmpty()) {
                socialLinks.add(new SocialLink("tiktok", bio.getTiktok()));
            }
            if (bio.getWhatsapp() != null && !bio.getWhatsapp().isEmpty()) {
                socialLinks.add(new SocialLink("whatsapp", bio.getWhatsapp()));
            }
            if (bio.getTwitter() != null && !bio.getTwitter().isEmpty()) {
                socialLinks.add(new SocialLink("twitter", bio.getTwitter()));
            }
            if (bio.getFacebook() != null && !bio.getFacebook().isEmpty()) {
                socialLinks.add(new SocialLink("facebook", bio.getFacebook()));
            }
            if (bio.getLinkedin() != null && !bio.getLinkedin().isEmpty()) {
                socialLinks.add(new SocialLink("linkedin", bio.getLinkedin()));
            }
            if (bio.getWebsite() != null && !bio.getWebsite().isEmpty()) {
                socialLinks.add(new SocialLink("website", bio.getWebsite()));
            }
        }

        // Build the response
        UserSummaryResponse response = new UserSummaryResponse(
                user.getFullName(),
                user.getUserHandle(),
                user.getLocation(),
                user.getEmail(),
                user.getPhoneNumber(),
                profileUrl,
                socialLinks);

        return new ApiCustomResponse<>(
                response,
                "User summary retrieved successfully",
                HttpStatus.OK.value());
    }

    public ApiCustomResponse<UserSummaryResponse> getUserSummary(String username) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated or not found.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User targetUser = userRepository.findFirstByUsername(username);

        if (targetUser == null || targetUser.getIsDeleted()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not found.",
                    HttpStatus.NOT_FOUND.value());
        }

        // Build profile image URL
        String name = targetUser.getFullName();
        String profileUrl = (targetUser.getProfilePicture() != null)
                ? targetUser.getProfilePicture().getUrl()
                : "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random";

        // Get bio and build social links
        List<SocialLink> socialLinks = new ArrayList<>();
        Optional<Bio> bioOpt = bioRepository.findByUser(targetUser);

        if (bioOpt.isPresent()) {
            Bio bio = bioOpt.get();

            if (bio.getTiktok() != null && !bio.getTiktok().isEmpty()) {
                socialLinks.add(new SocialLink("tiktok", bio.getTiktok()));
            }
            if (bio.getWhatsapp() != null && !bio.getWhatsapp().isEmpty()) {
                socialLinks.add(new SocialLink("whatsapp", bio.getWhatsapp()));
            }
            if (bio.getTwitter() != null && !bio.getTwitter().isEmpty()) {
                socialLinks.add(new SocialLink("twitter", bio.getTwitter()));
            }
            if (bio.getFacebook() != null && !bio.getFacebook().isEmpty()) {
                socialLinks.add(new SocialLink("facebook", bio.getFacebook()));
            }
            if (bio.getLinkedin() != null && !bio.getLinkedin().isEmpty()) {
                socialLinks.add(new SocialLink("linkedin", bio.getLinkedin()));
            }
            if (bio.getWebsite() != null && !bio.getWebsite().isEmpty()) {
                socialLinks.add(new SocialLink("website", bio.getWebsite()));
            }
        }

        // Build the response
        UserSummaryResponse response = new UserSummaryResponse(
                targetUser.getFullName(),
                targetUser.getUserHandle(),
                targetUser.getLocation(),
                targetUser.getEmail(),
                targetUser.getPhoneNumber(),
                profileUrl,
                socialLinks);

        return new ApiCustomResponse<>(
                response,
                "User summary retrieved successfully",
                HttpStatus.OK.value());
    }

    public ApiCustomResponse<UserProfileResponse> getMyProfile() {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated or not found.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        // Get bio (or empty default) and build social links
        Bio bio = bioRepository.findByUser(user).orElse(new Bio());
        List<SocialLink> socialLinks = new ArrayList<>();

        // Always add all supported platforms for the edit form
        socialLinks.add(new SocialLink("tiktok", bio.getTiktok()));
        socialLinks.add(new SocialLink("whatsapp", bio.getWhatsapp()));
        socialLinks.add(new SocialLink("twitter", bio.getTwitter()));
        socialLinks.add(new SocialLink("facebook", bio.getFacebook()));
        socialLinks.add(new SocialLink("linkedin", bio.getLinkedin()));
        socialLinks.add(new SocialLink("website", bio.getWebsite()));

        // Build the response (excluding password and image)
        UserProfileResponse response = new UserProfileResponse(
                user.getFirstName(),
                user.getMiddleName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getUserHandle(),
                bio.getBio(),
                bio.getTitle(),
                bio.getYearsOfExperience(),
                user.getDateOfBirth(),
                user.getLocation(),
                user.getGeoLocation(),
                user.getGender(),
                socialLinks,
                user.getLicense(),
                user.getSkills(),
                user.getVerificationStatus(),
                user.getProfileVisibility(),
                user.getSignupMethod());

        return new ApiCustomResponse<>(
                response,
                "User profile retrieved successfully",
                HttpStatus.OK.value());
    }

    public ApiCustomResponse<UserProfileResponse> updateMyProfile(UpdateUserProfileRequest request) {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated or not found.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();

        // Update user fields
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName());
        }
        if (request.middleName() != null && !request.middleName().isBlank()) {
            user.setMiddleName(request.middleName());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        if (request.location() != null && !request.location().isBlank()) {
            user.setLocation(request.location());
        }
        if (request.geoLocation() != null && !request.geoLocation().isBlank()) {
            user.setGeoLocation(request.geoLocation());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.license() != null && !request.license().isBlank()) {
            user.setLicense(request.license());
        }
        if (request.skills() != null && !request.skills().isBlank()) {
            user.setSkills(request.skills());
        }
        if (request.verificationStatus() != null) {
            user.setVerificationStatus(request.verificationStatus());
        }
        if (request.profileVisibility() != null) {
            user.setProfileVisibility(request.profileVisibility());
        }

        // Save user
        userRepository.save(user);

        // Update or create bio
        Bio bio = bioRepository.findByUser(user).orElse(new Bio());
        bio.setUser(user);

        if (request.bio() != null && !request.bio().isBlank()) {
            bio.setBio(request.bio());
        }
        if (request.title() != null && !request.title().isBlank()) {
            bio.setTitle(request.title());
        }
        if (request.yearsOfExperience() != null) {
            bio.setYearsOfExperience(request.yearsOfExperience());
        }

        // Update social links
        if (request.socialLinks() != null) {
            for (SocialLink link : request.socialLinks()) {
                if (link.platform() != null) {
                    switch (link.platform().toLowerCase()) {
                        case "tiktok" -> bio.setTiktok(link.url());
                        case "whatsapp" -> bio.setWhatsapp(link.url());
                        case "twitter" -> bio.setTwitter(link.url());
                        case "facebook" -> bio.setFacebook(link.url());
                        case "linkedin" -> bio.setLinkedin(link.url());
                        case "website" -> bio.setWebsite(link.url());
                    }
                }
            }
        }

        // Save bio
        bioRepository.save(bio);

        // Return updated profile
        return getMyProfile();
    }

    public ApiCustomResponse<UserStatsResponse> getMyStats() {
        Optional<User> userOpt = securityUtil.getAuthenticatedUser();

        if (userOpt.isEmpty()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not authenticated or not found.",
                    HttpStatus.UNAUTHORIZED.value());
        }

        User user = userOpt.get();
        return getUserStats(user);
    }

    public ApiCustomResponse<UserStatsResponse> getUserStats(String username) {
        User user = userRepository.findFirstByUsername(username);

        if (user == null || user.getIsDeleted()) {
            return new ApiCustomResponse<>(
                    null,
                    "User not found.",
                    HttpStatus.NOT_FOUND.value());
        }

        return getUserStats(user);
    }

    private ApiCustomResponse<UserStatsResponse> getUserStats(User user) {
        long totalProjects = projectRepository.countByOwner_IdAndIsDeletedFalse(user.getId());
        long totalPosts = projectStageRepository.countByPostedBy_IdAndIsDeletedFalse(user.getId());

        UserStatsResponse stats = new UserStatsResponse(totalPosts, totalProjects);

        return new ApiCustomResponse<>(
                stats,
                "User stats retrieved successfully",
                HttpStatus.OK.value());
    }
}
