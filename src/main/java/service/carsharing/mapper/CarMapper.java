package service.carsharing.mapper;

import org.mapstruct.Mapper;
import service.carsharing.config.MapperConfig;
import service.carsharing.dto.cars.CarRequestDto;
import service.carsharing.dto.cars.CarResponseDto;
import service.carsharing.model.Car;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    Car toModel(CarRequestDto requestDto);

    CarResponseDto toDto(Car car);
}
