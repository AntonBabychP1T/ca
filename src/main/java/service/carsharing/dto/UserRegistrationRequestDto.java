package service.carsharing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import service.carsharing.validation.FieldMatch;

@FieldMatch(
        firstField = "password",
        secondField = "repeatPassword",
        message = "The password fields must match"
)
public record UserRegistrationRequestDto(
        @NotNull(message = "Email cannot be null")
        @Email(message = "Invalid email format")
        String email,
        @NotNull(message = "Password cannot be null")
        @Size(min = 8,max = 24, message = "Password must be between 8 and 24 characters")
        String password,
        @NotNull(message = "You must repeat password")
        @Size(min = 8,max = 24, message = "Password must be between 8 and 24 characters")
        String repeatPassword,
        @NotNull(message = "First name cannot be null")
        String firstName,
        @NotNull(message = "Last name cannot be null")
        String lastName
) {
}
