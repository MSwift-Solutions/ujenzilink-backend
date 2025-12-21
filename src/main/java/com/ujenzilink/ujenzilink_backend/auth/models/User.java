package com.ujenzilink.ujenzilink_backend.auth.models;

import com.ujenzilink.ujenzilink_backend.auth.enums.Roles;
import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDateTime dateOfCreation;
    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    private boolean isEnabled = false;

    @Column(nullable = false)
    private boolean hasAgreedToTerms = false;

    private LocalDateTime termsAgreedAt;

    private String termsVersion;

    private int resendVerificationCount = 0;
    private LocalDateTime lastResendAttempt;

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
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(middleName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }

        return fullName.toString().trim();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(LocalDateTime dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
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

    public LocalDateTime getTermsAgreedAt() {
        return termsAgreedAt;
    }

    public void setTermsAgreedAt(LocalDateTime termsAgreedAt) {
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

    public LocalDateTime getLastResendAttempt() {
        return lastResendAttempt;
    }

    public void setLastResendAttempt(LocalDateTime lastResendAttempt) {
        this.lastResendAttempt = lastResendAttempt;
    }
}
