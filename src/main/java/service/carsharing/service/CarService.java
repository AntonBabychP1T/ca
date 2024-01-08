package service.carsharing.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import service.carsharing.dto.cars.CarRequestDto;
import service.carsharing.dto.cars.CarResponseDto;

public interface CarService {
    CarResponseDto createCar(CarRequestDto requestDto);

    List<CarResponseDto> getAllCars(Pageable pageable);

    CarResponseDto getCar(Long id);

    CarResponseDto updateCar(Long id, CarRequestDto requestDto);

    void deleteCar(Long id);
}
