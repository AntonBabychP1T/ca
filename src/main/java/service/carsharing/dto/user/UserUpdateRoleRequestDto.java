package service.carsharing.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserUpdateRoleRequestDto(
        @NotNull
        String role
) {
}
