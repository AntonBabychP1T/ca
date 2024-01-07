package service.carsharing.service;

import service.carsharing.dto.UserRegistrationRequestDto;
import service.carsharing.dto.UserResponseDto;

public interface UserService {
    UserResponseDto registration(UserRegistrationRequestDto requestDto);
}
