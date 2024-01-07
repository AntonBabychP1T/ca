package service.carsharing.security;

import service.carsharing.dto.UserLoginRequestDto;
import service.carsharing.dto.UserLoginResponseDto;

public interface AuthenticationService {
    UserLoginResponseDto authentication(UserLoginRequestDto requestDto);
}
