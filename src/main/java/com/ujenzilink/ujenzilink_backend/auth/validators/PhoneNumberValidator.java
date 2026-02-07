package com.ujenzilink.ujenzilink_backend.auth.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return true; // Let @NotNull handle null validation if needed
        }

        Set<String> errorMessage = new HashSet<>();

        // Accept phone numbers with optional + prefix followed by digits
        Pattern pattern = Pattern.compile("^\\+?[0-9]+$");
        Matcher matcher = pattern.matcher(phoneNumber);
        if (!matcher.matches()) {
            errorMessage.add("Phone number should only contain digits and optional + prefix!");
        }

        // Basic length check for international numbers (typically 7-15 excluding
        // non-digits, but we only have digits/+)
        // E.164 allows up to 15 digits. Min length varies, but 7 is a safe lower bound.
        if (phoneNumber.length() < 7) {
            errorMessage.add("Phone number too short!");
        }
        if (phoneNumber.length() > 15) {
            errorMessage.add("Phone number too long!");
        }

        if (!errorMessage.isEmpty()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            errorMessage.forEach(
                    message -> constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                            .addConstraintViolation());
        }

        return errorMessage.isEmpty();
    }
}
