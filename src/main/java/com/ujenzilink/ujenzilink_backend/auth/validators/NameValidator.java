package com.ujenzilink.ujenzilink_backend.auth.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NameValidator implements ConstraintValidator<Name, String> {
    // Allow letters (including Unicode), spaces, hyphens, and apostrophes
    // Must start with a letter
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[\\p{L}][\\p{L}\\s'-]*$",
            Pattern.UNICODE_CHARACTER_CLASS);

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
        if (name == null || name.isBlank()) {
            return true;
        }

        String trimmedName = name.trim();

        // Check length constraints
        if (trimmedName.length() < MIN_LENGTH || trimmedName.length() > MAX_LENGTH) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Name must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters")
                    .addConstraintViolation();
            return false;
        }

        // Check pattern (letters, spaces, hyphens, apostrophes, Unicode support)
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Name contains invalid characters. Only letters, spaces, hyphens, and apostrophes are allowed")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
