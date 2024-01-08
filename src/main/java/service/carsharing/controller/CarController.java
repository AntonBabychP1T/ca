package service.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.carsharing.dto.cars.CarRequestDto;
import service.carsharing.dto.cars.CarResponseDto;
import service.carsharing.service.CarService;

@Tag(name = "Cars manager", description = "Endpoints for cars managing")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cars")
public class CarController {
    private final CarService carService;

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new car", description = "Create a new car")
    public CarResponseDto createCar(@RequestBody @Valid CarRequestDto requestDto) {
        return carService.createCar(requestDto);
    }

    @Operation(summary = "Get all cars", description = "Get a list of all not deleted cars")
    @Parameter(name = "page", description = "open specified page, default value = 0",
            required = true, example = "0")
    @Parameter(name = "size", description = "describes count element per page",
            required = true, example = "10")
    @Parameter(name = "sort", description = "add sort for schema field", example = "sort=fee")
    @GetMapping
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carService.getAllCars(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by id", description = "Get single car by id key")
    public CarResponseDto getCar(@PathVariable Long id) {
        return carService.getCar(id);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/{id}")
    public CarResponseDto updateCar(@PathVariable Long id,
                                    @RequestBody @Valid CarRequestDto requestDto) {
        return carService.updateCar(id, requestDto);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car by id", description = "Soft delete specify car by id key")
    private void deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
    }
}
