package service.carsharing.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import service.carsharing.config.MapperConfig;
import service.carsharing.dto.user.UserRegistrationRequestDto;
import service.carsharing.dto.user.UserResponseDto;
import service.carsharing.dto.user.UserUpdateInfoRequestDto;
import service.carsharing.dto.user.UserWithRoleResponseDto;
import service.carsharing.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseDto toDto(User user);

    @Mapping(target = "roles", ignore = true)
    UserWithRoleResponseDto toDtoWithRole(User user);

    void updateUserFromDto(UserUpdateInfoRequestDto requestDto, @MappingTarget User user);

    @AfterMapping
    default void setRoles(User user, @MappingTarget UserWithRoleResponseDto dto) {
        if (user.getRoles() != null) {
            String[] roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .toArray(String[]::new);
            dto.setRoles(roles);
        }
    }
}
