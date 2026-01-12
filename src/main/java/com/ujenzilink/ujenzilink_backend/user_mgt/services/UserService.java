package com.ujenzilink.ujenzilink_backend.user_mgt.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import com.ujenzilink.ujenzilink_backend.auth.utils.SecurityUtil;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.SocialLink;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.TestimonialItemDto;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserCountResponseDto;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserInfoDto;
import com.ujenzilink.ujenzilink_backend.user_mgt.dtos.UserSummaryResponse;
import com.ujenzilink.ujenzilink_backend.user_mgt.models.Bio;
import com.ujenzilink.ujenzilink_backend.user_mgt.repositories.BioRepository;
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

    public ApiCustomResponse<UserSummaryResponse> getUserSummary() {
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
}
