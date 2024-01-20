package service.carsharing.telegram.dispatcher.handlers;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import service.carsharing.dto.rental.RentalResponseDto;
import service.carsharing.model.TelegramUserInfo;
import service.carsharing.model.User;
import service.carsharing.repository.TelegramUserInfoRepository;
import service.carsharing.service.RentalService;
import service.carsharing.telegram.TelegramBotService;
import service.carsharing.telegram.dispatcher.CommandHandler;

@RequiredArgsConstructor
@Component
public class CheckRentalsCommandHandler implements CommandHandler {
    private static final String COMMAND = "/checkRentals";
    private final TelegramBotService telegramBotService;
    private final RentalService rentalService;
    private final TelegramUserInfoRepository telegramUserInfoRepository;

    @Override
    public void handleCommand(Long chatId, String command, String[] args) {
        Optional<TelegramUserInfo> byChatId =
                telegramUserInfoRepository.findByChatId(chatId);
        if (byChatId.isEmpty()) {
            telegramBotService.sendMessage(chatId, "Can't find yor account in DB :(");
            return;
        }
        TelegramUserInfo telegramUserInfo = byChatId.get();
        User user = telegramUserInfo.getUser();
        List<RentalResponseDto> allCurrentRentals =
                rentalService.getAllCurrentRentals(user.getId(), true);
        if (allCurrentRentals.isEmpty()) {
            telegramBotService.sendMessage(chatId, "You don't have active rentals");
            return;
        }
        telegramBotService.sendMessage(chatId, allCurrentRentals.toString());
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}
