package service.carsharing.dto.rental;

import java.time.LocalDate;

public record RentalResponseDto(
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        Long carId,
        Long userId
) {
}
