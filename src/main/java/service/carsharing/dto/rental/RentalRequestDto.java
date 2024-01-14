package service.carsharing.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RentalRequestDto(
        @NotNull
        LocalDate rentalDate,
        @NotNull
        LocalDate returnDate,
        @NotNull
        Long carId,
        @NotNull
        Long userId
) {
}
