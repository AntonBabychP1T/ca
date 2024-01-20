package service.carsharing.service;

import service.carsharing.telegram.TelegramUserState;

public interface TelegramUserService {

    void registerNewUser(Long chatId, String username);

    TelegramUserState getUserState(Long chatId);

    void addUserState(Long chatId, TelegramUserState state);
}
