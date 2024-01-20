package service.carsharing.service;

import java.util.List;
import service.carsharing.dto.payment.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto createPayment(String email, Long rentalId);

    List<PaymentResponseDto> getPayments(String email, Long userId);

    PaymentResponseDto checkSuccessfulPayments(String id);

    PaymentResponseDto canceledPayment(String id);

    PaymentResponseDto renewPaymentSession(Long paymentId, String email);
}
