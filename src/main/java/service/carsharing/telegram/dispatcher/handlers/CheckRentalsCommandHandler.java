package service.carsharing.telegram.dispatcher.handlers;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import service.carsharing.dto.rental.RentalResponseDto;
import service.carsharing.model.TelegramUserInfo;
import service.carsharing.model.User;
import service.carsharing.repository.TelegramUserInfoRepository;
import service.carsharing.service.RentalService;
import service.carsharing.telegram.TelegramMessageEvent;
import service.carsharing.telegram.dispatcher.CommandHandler;

@RequiredArgsConstructor
@Component
public class CheckRentalsCommandHandler implements CommandHandler {
    private static final String COMMAND = "/checkRentals";
    private final RentalService rentalService;
    private final TelegramUserInfoRepository telegramUserInfoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handleCommand(Long chatId, String command, String[] args) {
        Optional<TelegramUserInfo> byChatId =
                telegramUserInfoRepository.findByChatId(chatId);
        if (byChatId.isEmpty()) {
            eventPublisher.publishEvent(
                    new TelegramMessageEvent(chatId, "Can't find yor account in DB :("));
            return;
        }
        TelegramUserInfo telegramUserInfo = byChatId.get();
        User user = telegramUserInfo.getUser();
        List<RentalResponseDto> allCurrentRentals =
                rentalService.getAllCurrentRentals(user.getId(), true);
        if (allCurrentRentals.isEmpty()) {
            eventPublisher.publishEvent(
                    new TelegramMessageEvent(chatId, "You don't have active rentals"));
            return;
        }
        eventPublisher.publishEvent(
                new TelegramMessageEvent(chatId, allCurrentRentals.toString()));
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}
