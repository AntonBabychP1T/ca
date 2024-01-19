package service.carsharing.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import service.carsharing.service.TelegramUserService;

@RequiredArgsConstructor
@Service
public class TelegramBotService extends TelegramLongPollingBot {
    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;
    @Value("${TELEGRAM_BOT_USERNAME}")
    private String botUsername;
    private final TelegramUserService telegramUserService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            TelegramUserState state = telegramUserService.getUserState(chatId);
            if (state == null) {
                telegramUserService.addUserState(chatId, new TelegramUserState(chatId, true));
                sendMessage(chatId, "Enter your email to verify");
            } else if (state.isAwaitingEmail()) {
                String email = update.getMessage().getText();
                telegramUserService.registerNewUser(chatId, email);
            } else {
                String text = update.getMessage().getText();
            }
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
