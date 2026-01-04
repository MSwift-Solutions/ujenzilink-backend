package com.ujenzilink.ujenzilink_backend.auth.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class UsernameValidator implements ConstraintValidator<Username, String> {
    // Allow alphanumeric characters, underscores, and hyphens
    // Must start with a letter or number
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9][a-zA-Z0-9_-]*$");

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 30;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
        if (username == null || username.isBlank()) {
            return false;
        }

        String trimmedUsername = username.trim();

        // Check length constraints
        if (trimmedUsername.length() < MIN_LENGTH || trimmedUsername.length() > MAX_LENGTH) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Username must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters")
                    .addConstraintViolation();
            return false;
        }

        // Check pattern (alphanumeric, underscores, hyphens)
        if (!USERNAME_PATTERN.matcher(trimmedUsername).matches()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Username can only contain letters, numbers, underscores, and hyphens. It must start with a letter or number")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}

