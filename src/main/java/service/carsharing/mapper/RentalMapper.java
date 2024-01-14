package service.carsharing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import service.carsharing.config.MapperConfig;
import service.carsharing.dto.rental.RentalRequestDto;
import service.carsharing.dto.rental.RentalResponseDto;
import service.carsharing.model.Rental;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "car.id", source = "carId")
    Rental toModel(RentalRequestDto requestDto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "carId", source = "car.id")
    RentalResponseDto toDto(Rental rental);
}
