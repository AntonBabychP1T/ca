package service.carsharing.mapper;

import org.mapstruct.Mapper;
import service.carsharing.config.MapperConfig;
import service.carsharing.dto.user.UserRegistrationRequestDto;
import service.carsharing.dto.user.UserResponseDto;
import service.carsharing.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseDto toDto(User user);
}
