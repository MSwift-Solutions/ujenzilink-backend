package com.ujenzilink.ujenzilink_backend.auth.dtos;

import com.ujenzilink.ujenzilink_backend.auth.validators.Name;
import com.ujenzilink.ujenzilink_backend.auth.validators.Password;
import com.ujenzilink.ujenzilink_backend.auth.validators.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
                @NotBlank(message = "First Name cannot be null or empty!") @Name String firstName,

                @Name String middleName,

                @NotBlank(message = "Last Name cannot be null or empty!") @Name String lastName,

                @NotBlank(message = "Phone Number cannot be null or empty!") @PhoneNumber String phoneNumber,

                @NotBlank(message = "Email cannot be null or empty!") @Email(message = "Invalid email!") String email,

                @Password String password) {
}
