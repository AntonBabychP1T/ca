package service.carsharing.security;

import service.carsharing.dto.user.UserLoginRequestDto;
import service.carsharing.dto.user.UserLoginResponseDto;

public interface AuthenticationService {
    UserLoginResponseDto authentication(UserLoginRequestDto requestDto);
}
