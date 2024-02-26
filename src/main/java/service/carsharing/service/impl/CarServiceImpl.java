package service.carsharing.service.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.carsharing.dto.cars.CarRequestDto;
import service.carsharing.dto.cars.CarResponseDto;
import service.carsharing.mapper.CarMapper;
import service.carsharing.model.Car;
import service.carsharing.repository.CarRepository;
import service.carsharing.service.CarService;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional
    public CarResponseDto createCar(CarRequestDto requestDto) {
        return carMapper.toDto(carRepository.save(carMapper.toModel(requestDto)));
    }

    @Override
    public List<CarResponseDto> getAllCars(Pageable pageable) {
        return carRepository.findAllByDeletedFalse(pageable).stream()
                .map(carMapper::toDto).toList();
    }

    @Override
    public CarResponseDto getCar(Long id) {
        return carMapper.toDto(getCarById(id));
    }

    @Override
    public CarResponseDto updateCar(Long id, CarRequestDto requestDto) {
        Car car = getCarById(id);
        carMapper.updateCar(requestDto, car);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public void deleteCar(Long id) {
        carRepository.softDelete(id);
    }

    private Car getCarById(Long id) {
        return carRepository.findByIdAndDeletedFalse(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id: " + id));
    }
}
