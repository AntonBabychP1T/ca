package service.carsharing.service;

import service.carsharing.dto.user.UserRegistrationRequestDto;
import service.carsharing.dto.user.UserResponseDto;
import service.carsharing.dto.user.UserUpdateInfoRequestDto;
import service.carsharing.dto.user.UserUpdateRoleRequestDto;
import service.carsharing.dto.user.UserWithRoleResponseDto;

public interface UserService {
    UserResponseDto registration(UserRegistrationRequestDto requestDto);

    UserWithRoleResponseDto updateRole(Long id, UserUpdateRoleRequestDto requestDto);

    UserWithRoleResponseDto getUserInfo(String email);

    UserResponseDto updateUserInfo(String email, UserUpdateInfoRequestDto requestDto);
}
