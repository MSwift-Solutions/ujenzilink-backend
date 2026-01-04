package com.ujenzilink.ujenzilink_backend.auth.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)
public @interface Username {
    String message() default "Invalid username. Username must be 3-30 characters and contain only letters, numbers, underscores, and hyphens";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

