package service.carsharing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @NotNull
        @Email
        String email,
        @NotNull
        @Size(min = 8, max = 24, message = "Password must be between 8 and 24 charaters")
        String password
) {
}
