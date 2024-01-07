package service.carsharing.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import service.carsharing.dto.user.UserRegistrationRequestDto;
import service.carsharing.dto.user.UserResponseDto;
import service.carsharing.exception.RegistrationException;
import service.carsharing.mapper.UserMapper;
import service.carsharing.model.User;
import service.carsharing.repository.UserRepository;
import service.carsharing.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
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
}
