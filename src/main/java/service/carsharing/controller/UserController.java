package service.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.carsharing.dto.user.UserResponseDto;
import service.carsharing.dto.user.UserUpdateInfoRequestDto;
import service.carsharing.dto.user.UserUpdateRoleRequestDto;
import service.carsharing.dto.user.UserWithRoleResponseDto;
import service.carsharing.service.UserService;

@Tag(name = "Users managing", description = "Endpoints to user managing")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Update user role",
            description = "Manager can update role for specify user")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/{id}/role")
    public UserWithRoleResponseDto updateRole(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRoleRequestDto requestDto
    ) {
        return userService.updateRole(id, requestDto);
    }

    @Operation(summary = "Update info about user",
            description = "Upgrading firstName, lastName or password")
    @PatchMapping("/me")
    public UserResponseDto updateUserInfo(
            Authentication authentication,
            @RequestBody @Valid UserUpdateInfoRequestDto requestDto
    ) {
        return userService.updateUserInfo(authentication.getName(),requestDto);
    }

    @Operation(summary = "Get info about user",
            description = "Get info about user by authentication info")
    @GetMapping("/me")
    public UserWithRoleResponseDto getUserInfo(Authentication authentication) {
        return userService.getUserInfo(authentication.getName());
    }
}
