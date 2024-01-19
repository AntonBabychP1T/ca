package service.carsharing.telegram;

import lombok.Data;

@Data
public class TelegramUserState {
    private Long chatId;
    private String email;
    private boolean awaitingEmail;

    public TelegramUserState(Long chatId, boolean awaitingEmail) {
        this.chatId = chatId;
        this.awaitingEmail = awaitingEmail;
    }
}
