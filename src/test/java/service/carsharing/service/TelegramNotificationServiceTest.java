package service.carsharing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import service.carsharing.model.TelegramUserInfo;
import service.carsharing.model.User;
import service.carsharing.repository.TelegramUserInfoRepository;
import service.carsharing.service.impl.TelegramNotificationServiceImpl;
import service.carsharing.telegram.TelegramMessageEvent;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationServiceTest {
    private static final String VALID_EMAIL = "testemail@email.com";
    private static final String VALID_PASSWORD = "Password";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    private static final Long VALID_ID = 1L;

    @InjectMocks
    private TelegramNotificationServiceImpl telegramNotificationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private TelegramUserInfoRepository telegramUserInfoRepository;

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

    @Test
    @DisplayName("sendNotification sends a message to a specific user")
    void sendNotification_ToSpecificUser() {
        User validUser = createValidUser();
        TelegramUserInfo userInfo = new TelegramUserInfo();
        userInfo.setChatId(123L);
        userInfo.setUser(validUser);
        String message = "Test message";
        Mockito.when(telegramUserInfoRepository.findByUserId(validUser.getId()))
                .thenReturn(Optional.of(userInfo));

        telegramNotificationService.sendNotification(validUser.getId(), message);

        Mockito.verify(eventPublisher)
                .publishEvent(new TelegramMessageEvent(userInfo.getChatId(), message));
    }

    @Test
    @DisplayName("sendNotification does nothing if user not found")
    void sendNotification_UserNotFound() {
        Long userId = 1L;
        String message = "Test message";
        Mockito.when(telegramUserInfoRepository.findByUserId(userId)).thenReturn(Optional.empty());

        telegramNotificationService.sendNotification(userId, message);

        Mockito.verify(eventPublisher,
                Mockito.never()).publishEvent(Mockito.any(TelegramMessageEvent.class));
    }

    @Test
    @DisplayName("sendGlobalNotification sends messages to all users")
    void sendGlobalNotification_ToAllUsers() {
        User validUser = createValidUser();
        String message = "Global message";
        List<TelegramUserInfo> userList = List.of(
                new TelegramUserInfo(1L, 123L, validUser),
                new TelegramUserInfo(2L, 456L, createValidUser())
        );
        Mockito.when(telegramUserInfoRepository.findAll()).thenReturn(userList);

        telegramNotificationService.sendGlobalNotification(message);

        userList.forEach(user ->
                Mockito.verify(eventPublisher)
                        .publishEvent(new TelegramMessageEvent(user.getChatId(), message))
        );
    }

    @Test
    @DisplayName("sendGlobalNotification does nothing if no users")
    void sendGlobalNotification_NoUsers() {
        String message = "Global message";
        Mockito.when(telegramUserInfoRepository.findAll()).thenReturn(List.of());

        telegramNotificationService.sendGlobalNotification(message);

        Mockito.verify(eventPublisher,
                Mockito.never()).publishEvent(Mockito.any(TelegramMessageEvent.class));
    }

}
