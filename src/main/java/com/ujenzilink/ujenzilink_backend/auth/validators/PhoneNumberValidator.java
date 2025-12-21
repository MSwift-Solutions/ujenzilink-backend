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
        Set<String> errorMessage = new HashSet<>();

        // Accept phone numbers with optional + prefix followed by digits
        Pattern pattern = Pattern.compile("^\\+?[0-9]+$");
        Matcher matcher = pattern.matcher(phoneNumber);
        if (!matcher.matches()) {
            errorMessage.add("Phone number should only contain digits and optional + prefix!");
        }

        // Check for valid country code (with or without + prefix)
        if (!phoneNumber.startsWith("+2547") && !phoneNumber.startsWith("+2541")
                && !phoneNumber.startsWith("2547") && !phoneNumber.startsWith("2541")) {
            errorMessage.add("Phone number should include valid country code (+254 or 254)!");
        }

        // Length validation: 12 digits for format without +, 13 characters for format
        // with +
        int expectedLength = phoneNumber.startsWith("+") ? 13 : 12;
        if (phoneNumber.length() < expectedLength) {
            errorMessage.add("Phone number too short!");
        }
        if (phoneNumber.length() > expectedLength) {
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
