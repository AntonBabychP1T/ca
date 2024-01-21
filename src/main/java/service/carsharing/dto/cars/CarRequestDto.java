package service.carsharing.dto.cars;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CarRequestDto(
        @NotNull
        String model,
        @NotNull
        String brand,
        @NotNull
        String type,
        @NotNull
        @Min(0)
        Integer inventory,
        @NotNull
        BigDecimal fee
) {
}
