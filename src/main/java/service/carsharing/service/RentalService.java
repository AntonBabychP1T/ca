package service.carsharing.service;

import java.util.List;
import service.carsharing.dto.rental.RentalRequestDto;
import service.carsharing.dto.rental.RentalResponseDto;

public interface RentalService {
    RentalResponseDto addNewRental(RentalRequestDto requestDto);

    List<RentalResponseDto> getAllCurrentRentals(Long userId, boolean isActive);

    RentalResponseDto getRentalById(Long id, String email);

    RentalResponseDto returnRental(Long rentalId, String email);
}
