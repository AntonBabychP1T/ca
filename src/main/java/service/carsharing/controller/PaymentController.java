package service.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.carsharing.dto.payment.PaymentRequestDto;
import service.carsharing.dto.payment.PaymentResponseDto;
import service.carsharing.service.PaymentService;

@Tag(name = "Payment managing", description = "Endpoint to managing payments")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService stripeService;

    @Operation(summary = "Get specify payment", description = "Get payment by id")
    @GetMapping("/{userId}")
    public List<PaymentResponseDto> getPayments(Authentication authentication,
                                                @PathVariable Long userId) {
        return stripeService.getPayments(authentication.getName(), userId);
    }

    @Operation(summary = "Create new payment",
            description = "Create new payment, result payment URL and id")
    @PostMapping("/{rentalId}")
    public PaymentResponseDto createPayment(Authentication authentication,
                                            @PathVariable Long rentalId) {
        return stripeService.createPayment(authentication.getName(), rentalId);
    }

    @Operation(summary = "stripe redirection endpoint",
            description = "Set payment is success, send message to Telegram")
    @GetMapping("/success")
    public PaymentResponseDto checkSuccessfulPayments(
            @RequestParam(required = false) PaymentRequestDto requestDto) {
        return stripeService.checkSuccessfulPayments(requestDto.id());
    }

    @Operation(summary = "stripe redirection endpoint",
            description = "Set payment is cancel, send message to Telegram")
    @GetMapping("/cancel")
    public PaymentResponseDto canceledPayment(
            @RequestParam(required = false) PaymentRequestDto requestDto) {
        return stripeService.canceledPayment(requestDto.id());
    }

    @Operation(summary = "Endpoint to renew expired session",
            description = "User can renew expired session and get new link")
    @PostMapping("/renew/{paymentId}")
    public PaymentResponseDto renewPaymentSession(@PathVariable Long paymentId,
                                                  Authentication authentication) {
        return stripeService.renewPaymentSession(paymentId, authentication.getName());
    }

}
