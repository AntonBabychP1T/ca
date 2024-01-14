package service.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.carsharing.dto.rental.RentalRequestDto;
import service.carsharing.dto.rental.RentalResponseDto;
import service.carsharing.service.RentalService;

@Tag(name = "Rental managing", description = "Endpoint to rentals managing")
@RequiredArgsConstructor
@RestController
@RequestMapping("api/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping()
    @Operation(summary = "Create a new rental", description = "Create a new rental")
    public RentalResponseDto addRental(@RequestBody @Valid RentalRequestDto requestDto) {
        return rentalService.addNewRental(requestDto);
    }

    @GetMapping("/{userId}/{isActive}")
    @Operation(summary = "Get all active rentals",
            description = "Get list of all active rentals for specify user")
    public List<RentalResponseDto> getAllActiveRentals(@PathVariable Long userId,
                                                       @PathVariable boolean isActive) {
        return rentalService.getAllCurrentRentals(userId, isActive);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get rental by id", description = "Get specify rental by id")
    public RentalResponseDto getRental(@PathVariable Long id, Authentication authentication) {
        return rentalService.getRentalById(id, authentication.getName());
    }

    @PostMapping("/{id}/return")
    public RentalResponseDto returnRental(@PathVariable Long id, Authentication authentication) {
        return rentalService.returnRental(id, authentication.getName());
    }
}
