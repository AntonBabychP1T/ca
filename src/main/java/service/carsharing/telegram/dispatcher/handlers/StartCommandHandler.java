package service.carsharing.telegram.dispatcher.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import service.carsharing.telegram.TelegramBotService;
import service.carsharing.telegram.dispatcher.CommandHandler;

@RequiredArgsConstructor
@Component
public class StartCommandHandler implements CommandHandler {
    private static final String COMMAND = "/start";
    private final TelegramBotService telegramBotService;

    @Override
    public void handleCommand(Long chatId, String command, String[] args) {
        String welcomeMessage = "Hello, i`m bot! I will help you with Car Sharing App "
                + "Please send me your email!";
        telegramBotService.sendMessage(chatId,welcomeMessage);
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}
