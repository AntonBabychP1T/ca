package service.carsharing.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import service.carsharing.model.TelegramUserInfo;

public interface TelegramUserInfoRepository extends JpaRepository<TelegramUserInfo, Long> {
    Optional<TelegramUserInfo> findByChatId(Long chatId);

    Optional<TelegramUserInfo> findByUserId(Long userId);
}
