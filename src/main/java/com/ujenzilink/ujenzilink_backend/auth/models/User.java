package com.ujenzilink.ujenzilink_backend.auth.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import com.ujenzilink.ujenzilink_backend.images.models.Image;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
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
    private Instant dateOfCreation;

    private Instant confirmedAt;

    @Column(nullable = false)
    private boolean isEnabled = false;

    @Column(nullable = false)
    private boolean hasAgreedToTerms = false;

    @CreationTimestamp
    private Instant termsAgreedAt;

    private String termsVersion;

    private int resendVerificationCount = 0;
    private Instant lastResendAttempt;

    private Instant lastSuccessfulLogin;
    private Instant lastLoginAttempt;

    private int failedLoginAttempts = 0;
    @Column(nullable = false)
    private boolean isLocked = false;

    @Enumerated(EnumType.STRING)
    private Roles role;

    public User() {
    }

    // Spring Security Methods
    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
}
