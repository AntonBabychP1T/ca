package service.carsharing.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import service.carsharing.model.TelegramUserInfo;
import service.carsharing.repository.TelegramUserInfoRepository;
import service.carsharing.repository.UserRepository;
import service.carsharing.service.TelegramUserService;
import service.carsharing.telegram.TelegramBotService;
import service.carsharing.telegram.TelegramUserState;

@RequiredArgsConstructor
@Service
public class TelegramUserServiceImpl implements TelegramUserService {
    private final TelegramUserInfoRepository telegramUserInfoRepository;
    private final UserRepository userRepository;
    private final ApplicationContext context;
    private final Map<Long, TelegramUserState> userStates = new HashMap<>();

    @Override
    public void registerNewUser(Long chatId, String username) {
        if (!isEmail(username) || userRepository.findByEmail(username).isEmpty()) {
            TelegramBotService botService = context.getBean(TelegramBotService.class);
            botService.sendMessage(chatId, "Not valid email, please send again");
            return;
        }
        userStates.get(chatId).setAwaitingEmail(false);
        TelegramUserInfo telegramUserInfo = new TelegramUserInfo();
        telegramUserInfo.setChatId(chatId);
        telegramUserInfo.setUser(userRepository.findByEmail(username).get());
        telegramUserInfoRepository.save(telegramUserInfo);
    }

    @Override
    public TelegramUserState getUserState(Long chatId) {
        return userStates.get(chatId);
    }

    @Override
    public void addUserState(Long chatId, TelegramUserState state) {
        userStates.put(chatId, state);
    }

    private boolean isEmail(String email) {
        Pattern p = Pattern.compile("\\b[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,4}\\b",
                Pattern.CASE_INSENSITIVE);
        return p.matcher(email).find();
    }
}
