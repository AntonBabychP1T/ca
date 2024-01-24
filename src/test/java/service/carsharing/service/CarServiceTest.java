package service.carsharing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import service.carsharing.dto.cars.CarRequestDto;
import service.carsharing.dto.cars.CarResponseDto;
import service.carsharing.mapper.CarMapper;
import service.carsharing.model.Car;
import service.carsharing.repository.CarRepository;
import service.carsharing.service.impl.CarServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    private static final Long VALID_ID = 1L;
    private static final Long NOT_VALID_ID = -1L;
    private static final String VALID_MODEL = "Valid Model";
    private static final String VALID_BRAND = "Valid Brand";
    private static final Car.Type VALID_TYPE = Car.Type.CUV;
    private static final String VALID_STRING_TYPE = "SUV";
    private static final Integer VALID_INVENTORY = 2;
    private static final BigDecimal VALID_FEE = BigDecimal.TEN;
    private static final boolean NOT_DELETED = false;

    @InjectMocks
    private CarServiceImpl carService;

    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;

    private Car createValidCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setInventory(VALID_INVENTORY);
        car.setFee(VALID_FEE);
        car.setDeleted(NOT_DELETED);
        car.setModel(VALID_MODEL);
        car.setBrand(VALID_BRAND);
        car.setType(VALID_TYPE);
        return car;
    }

    private CarRequestDto createValidCarRequestDto() {
        return new CarRequestDto(
                VALID_MODEL,
                VALID_BRAND,
                VALID_STRING_TYPE,
                VALID_INVENTORY,
                VALID_FEE
        );
    }

    private CarResponseDto createValidCarResponseDto() {
        return new CarResponseDto(
                VALID_ID,
                VALID_MODEL,
                VALID_BRAND,
                VALID_STRING_TYPE,
                VALID_INVENTORY,
                VALID_FEE
        );
    }

    @Test
    @DisplayName("Verify createCar() method work")
    public void createCar_ValidCarRequestDto_ValidCarResponseDto() {
        CarRequestDto requestDto = createValidCarRequestDto();
        CarResponseDto expected = createValidCarResponseDto();
        Car car = createValidCar();
        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto actual = carService.createCar(requestDto);

        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("Verify getAllCars() method works")
    public void getAllCars_ValidPageable_CarsList() {
        Car car = createValidCar();
        CarResponseDto responseDto = createValidCarResponseDto();
        List<CarResponseDto> expected = List.of(responseDto);
        Pageable pageable = PageRequest.of(0, 10);
        when(carRepository.findAllByDeletedFalse(pageable))
                .thenReturn(List.of(car));
        when(carMapper.toDto(car)).thenReturn(responseDto);

        List<CarResponseDto> actual = carService.getAllCars(pageable);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getCar() method works")
    public void getCar_ValidId_ValidCar() {
        Car car = createValidCar();
        CarResponseDto expected = createValidCarResponseDto();
        when(carRepository.findByIdAndDeletedFalse(VALID_ID))
                .thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto actual = carService.getCar(VALID_ID);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getCar() method throw exception")
    public void getCar_NotValidID_EntityNotFoundException() {
        when(carRepository.findByIdAndDeletedFalse(NOT_VALID_ID))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> carService.getCar(NOT_VALID_ID));
    }

    @Test
    public void updateCar_ValidIdAndRequestDto_UpdaterCar() {
        Car car = createValidCar();
        CarRequestDto requestDto = createValidCarRequestDto();
        CarResponseDto expected = createValidCarResponseDto();
        when(carRepository.findByIdAndDeletedFalse(VALID_ID))
                .thenReturn(Optional.of(car));
        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto actual = carService.updateCar(VALID_ID, requestDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify updateCar() method throws EntityNotFoundException for non-existent ID")
    public void updateCar_NonExistentId_EntityNotFoundException() {
        Long nonExistentId = NOT_VALID_ID;
        CarRequestDto requestDto = createValidCarRequestDto();

        when(carRepository.findByIdAndDeletedFalse(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> carService.updateCar(nonExistentId, requestDto));
    }

    @Test
    @DisplayName("Verify deleteCar() method work")
    public void deleteCar_ValidId_CarSoftDelete() {
        carService.deleteCar(VALID_ID);
        verify(carRepository).softDelete(VALID_ID);
    }
}
