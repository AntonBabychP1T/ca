package service.carsharing.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import service.carsharing.dto.payment.PaymentResponseDto;
import service.carsharing.mapper.PaymentMapper;
import service.carsharing.model.Payment;
import service.carsharing.model.Rental;
import service.carsharing.repository.PaymentRepository;
import service.carsharing.repository.RentalRepository;
import service.carsharing.service.NotificationService;
import service.carsharing.service.PaymentService;
import service.carsharing.service.UserService;
import service.carsharing.stripe.StripeSessionService;

@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements PaymentService {
    private static final BigDecimal CONVERT_TO_CENT = BigDecimal.valueOf(100L);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final StripeSessionService stripeSessionService;

    @Override
    public PaymentResponseDto createPayment(String email, Long rentalId) {
        Long userId = userService.getUserInfo(email).getId();
        Rental rental = validateAndGetRental(rentalId, userId);
        Payment payment = preparePayment(rental);
        try {
            Session session = stripeSessionService.createStripeSession(
                    getTotalAmountCounter(rental), "Rental Payment"
            );
            payment.setSessionId(session.getId());
            payment.setSessionUrl(new URL(session.getUrl()));
            notificationService.sendNotification(userId, "Payment URL: "
                    + payment.getSessionUrl());
        } catch (StripeException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentResponseDto> getPayments(String email, Long userId) {
        return paymentRepository.getAllByUserId(userId).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public PaymentResponseDto checkSuccessfulPayments(String id) {
        Payment payment = getPaymentBySessionId(id);
        payment.setStatus(Payment.Status.PAID);
        String message = String.format("Payment with id: %d for the amount: %s$ successful!",
                payment.getId(), payment.getAmountToPay().toString());
        notificationService.sendNotification(getUserIdByPayment(payment),
                message);
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponseDto canceledPayment(String id) {
        Payment payment = getPaymentBySessionId(id);
        payment.setStatus(Payment.Status.CANCEL);
        notificationService.sendNotification(getUserIdByPayment(payment),
                "Payment failure! The payment can be made later,"
                        + " but not after 24 hours!");
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponseDto renewPaymentSession(Long paymentId, String email) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with id: " + paymentId)
        );
        Rental rental = rentalRepository.findById(payment.getRentalId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: "
                        + payment.getRentalId())
        );
        if (!rental.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You do not have permission to renew this payment session");
        }
        if (payment.getStatus().equals(Payment.Status.PAID)) {
            throw new IllegalStateException("Payment session cannot be renewed");
        }
        try {
            Session newSession = stripeSessionService.createStripeSession(
                    getTotalAmountCounter(rental), "Rental repayment"
            );
            payment.setStatus(Payment.Status.PENDING);
            payment.setSessionId(newSession.getId());
            payment.setSessionUrl(new URL(newSession.getUrl()));
            paymentRepository.save(payment);
            return paymentMapper.toDto(payment);
        } catch (StripeException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkExpiredStripeSessions() {
        List<Payment> payments = paymentRepository.findAllByStatus(Payment.Status.PENDING);
        for (Payment payment : payments) {
            try {
                Session session = stripeSessionService.retrieveSession(payment.getSessionId());
                if (session.getStatus().equals("expired")) {
                    payment.setStatus(Payment.Status.EXPIRED);
                    paymentRepository.save(payment);
                }
            } catch (StripeException e) {
                throw new RuntimeException("Can't retrieve session for payment:" + payment, e);
            }
        }
    }

    private Long getUserIdByPayment(Payment payment) {
        Long rentalId = payment.getRentalId();
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );
        return rental.getUser().getId();
    }

    private Rental validateAndGetRental(Long rentalId, Long userId) {
        return rentalRepository.findByIdAndUserIdAndDeletedFalse(rentalId, userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
                );
    }

    private Payment preparePayment(Rental rental) {
        Payment payment = new Payment();
        payment.setAmountToPay(getTotalAmountCounter(rental));
        payment.setStatus(Payment.Status.PENDING);
        payment.setRentalId(rental.getId());
        if (rental.getReturnDate().isAfter(LocalDate.now())) {
            payment.setType(Payment.Type.PAYMENT);
            return payment;
        }
        if (rental.getActualReturnDate() == null) {
            payment.setType(Payment.Type.FINE);
            return payment;
        }
        payment.setType(rental.getActualReturnDate().isAfter(rental.getReturnDate())
                ? Payment.Type.FINE : Payment.Type.PAYMENT);
        return payment;
    }

    private BigDecimal getTotalAmountCounter(Rental rental) {
        return rental.getCar().getFee().multiply(CONVERT_TO_CENT).multiply(
                BigDecimal.valueOf(ChronoUnit.DAYS.between(
                        rental.getRentalDate(), rental.getReturnDate()
                )));
    }

    private Payment getPaymentBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with session id:"
                        + sessionId));
    }
}
