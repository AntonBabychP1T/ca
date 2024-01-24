package service.carsharing.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import service.carsharing.model.TelegramUserInfo;
import service.carsharing.model.User;
import service.carsharing.repository.TelegramUserInfoRepository;
import service.carsharing.repository.UserRepository;
import service.carsharing.service.impl.TelegramUserServiceImpl;
import service.carsharing.telegram.TelegramMessageEvent;
import service.carsharing.telegram.TelegramUserState;

@ExtendWith(MockitoExtension.class)
public class TelegramUserServiceTest {
    private static final String VALID_EMAIL = "testemail@mail.com";
    private static final String INVALID_EMAIL = "NOT VALID EMAIL";
    private static final Long VALID_ID = 1L;
    private static final Long VALID_CHAT_ID = 123456L;
    private static final String VALID_PASSWORD = "Password";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";

    @InjectMocks
    private TelegramUserServiceImpl telegramUserService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TelegramUserInfoRepository telegramUserInfoRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

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

    private TelegramUserState createValidTelegramUserState() {
        return new TelegramUserState(
                VALID_ID,
                true
        );
    }

    @Test
    @DisplayName("Verify registerNewUser() method checks already registered users")
    public void registerNewUser_ValidChatIdAndUsername_UserRegistered() {
        telegramUserService.addUserState(VALID_CHAT_ID, createValidTelegramUserState());
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(createValidUser()));
        when(telegramUserInfoRepository.findByChatId(VALID_CHAT_ID))
                .thenReturn(Optional.of(new TelegramUserInfo()));

        telegramUserService.registerNewUser(VALID_CHAT_ID, VALID_EMAIL);

        verify(eventPublisher).publishEvent(
                new TelegramMessageEvent(VALID_CHAT_ID, "You already registered"));
    }

    @Test
    @DisplayName("Verify registerNewUser() method checks invalid email")
    public void registerNewUser_ValidChatIdAndInvalidUsername_errorMessage() {
        telegramUserService.registerNewUser(VALID_CHAT_ID, INVALID_EMAIL);

        verify(eventPublisher).publishEvent(
                new TelegramMessageEvent(VALID_CHAT_ID,
                        "Not valid email, please send again"));
    }

    @Test
    @DisplayName("Verify registerNewUser() method registers a new user")
    public void registerNewUser_ValidChatIdAndUsername_RegistrationNewUser() {
        TelegramUserState telegramUserState = createValidTelegramUserState();
        telegramUserState.setAwaitingEmail(false);
        telegramUserService.addUserState(VALID_CHAT_ID, telegramUserState);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(createValidUser()));
        when(telegramUserInfoRepository.findByChatId(VALID_CHAT_ID)).thenReturn(Optional.empty());

        telegramUserService.registerNewUser(VALID_CHAT_ID, VALID_EMAIL);

        verify(eventPublisher).publishEvent(
                new TelegramMessageEvent(VALID_CHAT_ID,
                        "Registration success"));
    }

    @Test
    @DisplayName("Verify getUserState() method work")
    void getUserState_ValidChatId_ValidTelegramUserState() {
        TelegramUserState expected = createValidTelegramUserState();
        telegramUserService.addUserState(VALID_CHAT_ID, expected);

        TelegramUserState actual = telegramUserService.getUserState(VALID_CHAT_ID);

        Assertions.assertEquals(expected, actual);
    }
}
