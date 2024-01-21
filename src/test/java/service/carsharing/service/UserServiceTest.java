package service.carsharing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import service.carsharing.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final String VALID_EMAIL = "test@email.com";
    private static final String VALID_PASSWORD = "Password";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    private static final String VALID_NEW_LAST_NAME = "New Last Name";
    private static final String NOT_VALID_EMAIL = "not_valid_email@mail.com";
    private static final Long VALID_ID = 1L;
    private static final Role.RoleName VALID_ROLE = Role.RoleName.ROLE_CUSTOMER;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User createValidUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setPassword(VALID_PASSWORD);
        user.setRoles(new HashSet<>());
        user.setLastName(VALID_LAST_NAME);
        user.setFirstName(VALID_FIRST_NAME);
        return user;
    }

    private UserRegistrationRequestDto createValiduserRegistrationRequestDto() {
        return new UserRegistrationRequestDto(
                VALID_EMAIL,
                VALID_PASSWORD,
                VALID_PASSWORD,
                VALID_FIRST_NAME,
                VALID_LAST_NAME
        );
    }

    private UserResponseDto createValidUserResponseDto() {
        return new UserResponseDto(
                VALID_ID,
                VALID_EMAIL,
                VALID_FIRST_NAME,
                VALID_LAST_NAME
        );
    }

    private UserUpdateRoleRequestDto createValidUserUpdateRoleRequestDto() {
        return new UserUpdateRoleRequestDto(
                "ROLE_CUSTOMER"
        );
    }

    private Role createValidRole() {
        Role role = new Role();
        role.setId(VALID_ID);
        role.setName(VALID_ROLE);
        return role;
    }

    private UserWithRoleResponseDto createValidUserWithRoleResponseDto() {
        UserWithRoleResponseDto responseDto = new UserWithRoleResponseDto();
        responseDto.setRoles(new String[]{"ROLE_CUSTOMER"});
        responseDto.setId(VALID_ID);
        responseDto.setEmail(VALID_EMAIL);
        responseDto.setLastName(VALID_LAST_NAME);
        responseDto.setFirstName(VALID_FIRST_NAME);
        return responseDto;
    }

    private UserUpdateInfoRequestDto createValidUserUpdateInfoRequestDto() {
        return new UserUpdateInfoRequestDto(
                VALID_PASSWORD,
                VALID_FIRST_NAME,
                VALID_NEW_LAST_NAME
        );
    }

    @Test
    @DisplayName("Verify register() method works")
    public void register_ValidUSerRegistrationDto_ReturnResponseDto() {
        UserRegistrationRequestDto requestDto = createValiduserRegistrationRequestDto();
        User user = createValidUser();
        UserResponseDto expected = createValidUserResponseDto();
        when(userRepository.findByEmail(requestDto.email())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(passwordEncoder.encode(requestDto.password())).thenReturn("HashedPassword");
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.registration(requestDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify updateRole() method works")
    public void updateRole_ValidUserUpdateRoleRequestDto_UserWithNewRole() {
        User user = createValidUser();
        Role role = createValidRole();
        user.setRoles(Set.of(role));
        UserUpdateRoleRequestDto requestDto = createValidUserUpdateRoleRequestDto();
        UserWithRoleResponseDto expected = createValidUserWithRoleResponseDto();
        when(userRepository.findByIdAndDeletedFalse(VALID_ID))
                .thenReturn(Optional.of(createValidUser()));
        when(roleRepository.findByName(VALID_ROLE)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDtoWithRole(user)).thenReturn(expected);

        UserWithRoleResponseDto actual = userService.updateRole(VALID_ID, requestDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify updateUserInfo() method works")
    public void updateUserInfo_ValidEmailAndUserUpdateInfoRequestDto_NewUser() {
        User user = createValidUser();
        UserResponseDto expected = new UserResponseDto(
                VALID_ID,
                VALID_EMAIL,
                VALID_FIRST_NAME,
                VALID_NEW_LAST_NAME
        );

        UserUpdateInfoRequestDto requestDto = createValidUserUpdateInfoRequestDto();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.updateUserInfo(VALID_EMAIL, requestDto);

        verify(userMapper).updateUserFromDto(requestDto, user);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getUserInfo() method works")
    public void getUserInfo_ValidEmail_UserWithRoleResponseDto() {
        User user = createValidUser();
        UserWithRoleResponseDto expected = createValidUserWithRoleResponseDto();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDtoWithRole(user)).thenReturn(expected);

        UserWithRoleResponseDto actual = userService.getUserInfo(VALID_EMAIL);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify register() method throw exception")
    public void register_AlreadyRegisteredUser_RegistrationException() {
        UserRegistrationRequestDto requestDto = createValiduserRegistrationRequestDto();
        when(userRepository.findByEmail(requestDto.email()))
                .thenReturn(Optional.of(createValidUser()));

        assertThrows(RegistrationException.class, () -> userService.registration(requestDto));
    }

    @Test
    @DisplayName("Verify updateRole() method throw exception")
    public void updateRole_NotValidRole_EntityNotFoundException() {
        User user = createValidUser();
        String validButNonExistentRoleName = "ROLE_CUSTOMER";

        when(userRepository.findByIdAndDeletedFalse(VALID_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(Role.RoleName.valueOf(validButNonExistentRoleName)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateRole(VALID_ID,
                new UserUpdateRoleRequestDto(validButNonExistentRoleName)));
    }

    @Test
    @DisplayName("Verify getUserInfo() method throw exception")
    void getUserInfo_NotValidId_EntityNotFoundException() {
        when(userRepository.findByEmail(NOT_VALID_EMAIL)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserInfo(NOT_VALID_EMAIL));
    }
}
