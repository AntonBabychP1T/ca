package service.carsharing.dto.cars;

import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String model,
        String brand,
        String type,
        Integer inventory,
        BigDecimal fee
) {
}
