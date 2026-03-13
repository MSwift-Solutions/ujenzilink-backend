package com.ujenzilink.ujenzilink_backend.auth.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ujenzilink.ujenzilink_backend.auth.enums.Gender;
import com.ujenzilink.ujenzilink_backend.auth.enums.ProfileVisibility;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import com.ujenzilink.ujenzilink_backend.auth.enums.SignupMethod;
import com.ujenzilink.ujenzilink_backend.auth.enums.VerificationStatus;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String username;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_picture_id")
    @JsonManagedReference
    private Image profilePicture;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant dateOfCreation;

    private Instant confirmedAt;

    @Column(nullable = false)
    private boolean isEnabled = false;

    @Column(nullable = false)
    private boolean hasAgreedToTerms = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant termsAgreedAt;

    private String termsVersion;

    private int resendVerificationCount = 0;
    private Instant lastResendAttempt;

    private Instant lastSuccessfulLogin;
    private Instant lastLoginAttempt;

    private int failedLoginAttempts = 0;
    @Column(nullable = false)
    private boolean isLocked = false;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(length = 500)
    private String bio;

    private LocalDate dateOfBirth;

    private String location;

    private String geoLocation;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String license;

    @Column(length = 1000)
    private String skills;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignupMethod signupMethod = SignupMethod.DEFAULT;

    public User() {
    }

    // Spring Security Methods
    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public @NonNull String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    // Helper method to get full name
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();

        if (firstName != null && !firstName.isEmpty()) {
            fullName.append(firstName);
        }

        if (middleName != null && !middleName.isEmpty()) {
            if (!fullName.isEmpty()) {
                fullName.append(" ");
            }
            fullName.append(middleName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            if (!fullName.isEmpty()) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }

        return fullName.toString().trim();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserHandle() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Image getProfilePicture() {
        return this.profilePicture;
    }

    public void setProfilePicture(Image profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Instant getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Instant dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean getHasAgreedToTerms() {
        return hasAgreedToTerms;
    }

    public void setHasAgreedToTerms(boolean hasAgreedToTerms) {
        this.hasAgreedToTerms = hasAgreedToTerms;
    }

    public Instant getTermsAgreedAt() {
        return termsAgreedAt;
    }

    public void setTermsAgreedAt(Instant termsAgreedAt) {
        this.termsAgreedAt = termsAgreedAt;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public void setTermsVersion(String termsVersion) {
        this.termsVersion = termsVersion;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public int getResendVerificationCount() {
        return resendVerificationCount;
    }

    public void setResendVerificationCount(int resendVerificationCount) {
        this.resendVerificationCount = resendVerificationCount;
    }

    public Instant getLastResendAttempt() {
        return lastResendAttempt;
    }

    public void setLastResendAttempt(Instant lastResendAttempt) {
        this.lastResendAttempt = lastResendAttempt;
    }

    public Instant getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(Instant lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }

    public Instant getLastLoginAttempt() {
        return lastLoginAttempt;
    }

    public void setLastLoginAttempt(Instant lastLoginAttempt) {
        this.lastLoginAttempt = lastLoginAttempt;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public ProfileVisibility getProfileVisibility() {
        return profileVisibility;
    }

    public void setProfileVisibility(ProfileVisibility profileVisibility) {
        this.profileVisibility = profileVisibility;
    }

    public SignupMethod getSignupMethod() {
        return signupMethod;
    }

    public void setSignupMethod(SignupMethod signupMethod) {
        this.signupMethod = signupMethod;
    }
}
