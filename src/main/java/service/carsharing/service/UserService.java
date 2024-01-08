package service.carsharing.service;

import service.carsharing.dto.user.UserRegistrationRequestDto;
import service.carsharing.dto.user.UserResponseDto;

public interface UserService {
    UserResponseDto registration(UserRegistrationRequestDto requestDto);
}
