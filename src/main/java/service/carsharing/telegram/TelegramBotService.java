package service.carsharing.telegram;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import service.carsharing.service.TelegramUserService;
import service.carsharing.telegram.dispatcher.CommandDispatcher;

@RequiredArgsConstructor
@Service
public class TelegramBotService extends TelegramLongPollingBot {
    @Value("${TELEGRAM_BOT_TOKEN}")
    private String botToken;
    @Value("${TELEGRAM_BOT_USERNAME}")
    private String botUsername;
    private final TelegramUserService telegramUserService;
    @Autowired
    private CommandDispatcher commandDispatcher;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText();
            if (isAuthorized(chatId,message)) {
                String messageText = update.getMessage().getText();
                String[] parts = messageText.split("\\s+");
                String command = parts[0];
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);
                commandDispatcher.dispatch(chatId, command, args);
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

    @EventListener
    public void onTelegramMessageEvent(TelegramMessageEvent event) {
        sendMessage(event.getChatId(), event.getMessage());
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Can't send the message to chatId: " + chatId, e);
        }
    }

    public boolean isAuthorized(Long chatId, String messageText) {
        TelegramUserState state = telegramUserService.getUserState(chatId);
        if (state == null) {
            telegramUserService.addUserState(chatId, new TelegramUserState(chatId, true));
            sendMessage(chatId, "Enter your email to verify");
            return false;
        }
        if (state.isAwaitingEmail()) {
            telegramUserService.registerNewUser(chatId, messageText);
            return false;
        }
        return true;
    }
}
