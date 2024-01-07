package service.carsharing.dto;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}
