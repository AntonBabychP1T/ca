package service.carsharing.telegram;

import lombok.Data;

@Data
public class TelegramMessageEvent {
    private final Long chatId;
    private final String message;

    public TelegramMessageEvent(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
    }
}
