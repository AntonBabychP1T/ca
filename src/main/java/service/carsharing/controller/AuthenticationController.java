package service.carsharing.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.carsharing.dto.UserLoginRequestDto;
import service.carsharing.dto.UserLoginResponseDto;
import service.carsharing.dto.UserRegistrationRequestDto;
import service.carsharing.dto.UserResponseDto;
import service.carsharing.security.AuthenticationService;
import service.carsharing.service.UserService;

@Tag(name = "Authentication manager", description = "Endpoints for registration and authorization")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto requestDto) {
        return userService.registration(requestDto);
    }

    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authenticationService.authentication(requestDto);
    }
}
