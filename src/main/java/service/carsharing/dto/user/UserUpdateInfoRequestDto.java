package service.carsharing.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateInfoRequestDto(
        @NotNull(message = "Password cannot be null")
        @Size(min = 8,max = 24, message = "Password must be between 8 and 24 characters")
        String password,
        @NotNull(message = "First name cannot be null")
        String firstName,
        @NotNull(message = "Last name cannot be null")
        String lastName
) {
}
