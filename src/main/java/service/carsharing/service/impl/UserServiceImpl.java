package service.carsharing.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import service.carsharing.dto.user.UserRegistrationRequestDto;
import service.carsharing.dto.user.UserResponseDto;
import service.carsharing.dto.user.UserUpdateInfoRequestDto;
import service.carsharing.dto.user.UserUpdateRoleRequestDto;
import service.carsharing.dto.user.UserWithRoleResponseDto;
import service.carsharing.exception.RegistrationException;
import service.carsharing.mapper.UserMapper;
import service.carsharing.model.Role;
import service.carsharing.model.User;
import service.carsharing.repository.RoleRepository;
import service.carsharing.repository.UserRepository;
import service.carsharing.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto registration(UserRegistrationRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new RegistrationException("This email already registered");
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserWithRoleResponseDto updateRole(Long id, UserUpdateRoleRequestDto requestDto) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find user with id: " + id));
        Role.RoleName roleName = Role.RoleName.valueOf(requestDto.role());
        Role role = roleRepository.findByName(roleName).orElseThrow(
                        () -> new EntityNotFoundException("Can't find role with name=" + roleName));
        user.getRoles().add(role);
        return userMapper.toDtoWithRole(userRepository.save(user));
    }

    @Override
    public UserWithRoleResponseDto getUserInfo(String email) {
        return userMapper.toDtoWithRole(getUserByEmail(email));
    }

    @Override
    public UserResponseDto updateUserInfo(String email, UserUpdateInfoRequestDto requestDto) {
        User user = getUserByEmail(email);
        userMapper.updateUserFromDto(requestDto, user);
        return userMapper.toDto(userRepository.save(user));

    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with email: " + email));
    }
}
