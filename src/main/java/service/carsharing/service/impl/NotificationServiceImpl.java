package service.carsharing.service.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import service.carsharing.model.TelegramUserInfo;
import service.carsharing.repository.TelegramUserInfoRepository;
import service.carsharing.service.NotificationService;
import service.carsharing.telegram.TelegramBotService;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {
    private final TelegramBotService telegramBotService;
    private final TelegramUserInfoRepository telegramUserInfoRepository;

    @Override
    public void sendNotification(Long id, String message) {
        Optional<TelegramUserInfo> byUserId = telegramUserInfoRepository.findByUserId(id);
        if (byUserId.isPresent()) {
            TelegramUserInfo telegramUserInfo = byUserId.get();
            telegramBotService.sendMessage(telegramUserInfo.getChatId(), message);
        }
    }
}
