package service.carsharing.dto.payment;

import java.math.BigDecimal;

public record PaymentResponseDto(
        Long id,
        Long rentalId,
        String sessionUrl,
        String status,
        String sessionId,
        BigDecimal amountToPay
) {
}
