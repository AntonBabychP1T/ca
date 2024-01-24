package service.carsharing.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.carsharing.dto.payment.PaymentResponseDto;
import service.carsharing.dto.user.UserWithRoleResponseDto;
import service.carsharing.mapper.PaymentMapper;
import service.carsharing.model.Car;
import service.carsharing.model.Payment;
import service.carsharing.model.Rental;
import service.carsharing.model.User;
import service.carsharing.repository.PaymentRepository;
import service.carsharing.repository.RentalRepository;
import service.carsharing.service.impl.StripePaymentServiceImpl;
import service.carsharing.stripe.StripeSessionService;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {
    private static final String VALID_EMAIL = "test@email.com";
    private static final String VALID_PASSWORD = "Password";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = -1L;
    private static final LocalDate VALID_RENTAL_DAY = LocalDate.now();
    private static final LocalDate VALID_RETURN_DAY = LocalDate.now().plusDays(5);
    private static final String VALID_SESSION_ID = "stripe_session_id";
    private static final String VALID_SESSION_URL = "http://payment.url";

    @InjectMocks
    private StripePaymentServiceImpl stripeService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private StripeSessionService stripeSessionService;

    private User createValidUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setPassword(VALID_PASSWORD);
        user.setRoles(new HashSet<>());
        user.setLastName(VALID_LAST_NAME);
        user.setFirstName(VALID_FIRST_NAME);
        return user;
    }

    private Rental createValidRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setRentalDate(VALID_RENTAL_DAY);
        rental.setReturnDate(VALID_RETURN_DAY);
        Car car = new Car();
        car.setId(VALID_ID);
        car.setFee(BigDecimal.TEN);
        rental.setCar(car);
        rental.setUser(createValidUser());
        rental.setDeleted(false);
        return rental;
    }

    private Payment createValidPayments() throws MalformedURLException {
        Payment payment = new Payment();
        payment.setId(VALID_ID);
        payment.setRentalId(VALID_ID);
        payment.setSessionUrl(new URL(VALID_SESSION_URL));
        payment.setStatus(Payment.Status.PENDING);
        payment.setSessionId(VALID_SESSION_ID);
        payment.setType(Payment.Type.PAYMENT);
        payment.setAmountToPay(BigDecimal.TEN);
        return payment;
    }

    private PaymentResponseDto createValidPaymentResponseDto() {
        return new PaymentResponseDto(
                VALID_ID,
                VALID_ID,
                VALID_SESSION_URL,
                "Pending",
                VALID_SESSION_ID,
                BigDecimal.TEN
        );
    }

    @Test
    @DisplayName("Verify createPayment() method work")
    void createPayment_Successful() throws MalformedURLException, StripeException {
        UserWithRoleResponseDto responseDto = new UserWithRoleResponseDto();
        responseDto.setId(VALID_ID);
        Rental rental = createValidRental();
        Payment payment = createValidPayments();
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(VALID_SESSION_ID);
        when(session.getUrl()).thenReturn(VALID_SESSION_URL);
        when(userService.getUserInfo(VALID_EMAIL)).thenReturn(responseDto);
        when(rentalRepository.findByIdAndUserIdAndDeletedFalse(VALID_ID, VALID_ID))
                .thenReturn(Optional.of(rental));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(stripeSessionService.createStripeSession(any(BigDecimal.class),any(String.class)))
                .thenReturn(session);
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(createValidPaymentResponseDto());

        PaymentResponseDto actualResponse = stripeService.createPayment(VALID_EMAIL, VALID_ID);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(VALID_SESSION_ID, actualResponse.sessionId());
        Assertions.assertEquals(VALID_SESSION_URL, actualResponse.sessionUrl());
        verify(notificationService).sendNotification(eq(VALID_ID), anyString());
    }

    @Test
    @DisplayName("Verify getPayments() method work")
    public void getPayments_ValidEmailAndId_ResponseDtos() throws MalformedURLException {
        Payment payment = createValidPayments();
        PaymentResponseDto responseDto = createValidPaymentResponseDto();
        when(paymentRepository.getAllByUserId(VALID_ID)).thenReturn(List.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        List<PaymentResponseDto> actual = stripeService.getPayments(VALID_EMAIL, VALID_ID);

        Assertions.assertEquals(List.of(responseDto),actual);
    }

    @Test
    @DisplayName("Verify checkSuccessfulPayments() method work")
    public void checkSuccessfulPayments_ValidId_ResponseDto() throws MalformedURLException {
        Payment payment = createValidPayments();
        payment.setStatus(Payment.Status.PAID);
        PaymentResponseDto expected = new PaymentResponseDto(
                VALID_ID,
                VALID_ID,
                VALID_SESSION_URL,
                Payment.Status.PAID.name(),
                VALID_SESSION_ID,
                BigDecimal.TEN
        );
        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(createValidRental()));
        when(paymentRepository.findBySessionId(VALID_SESSION_ID))
                .thenReturn(Optional.of(createValidPayments()));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expected);

        PaymentResponseDto actual = stripeService.checkSuccessfulPayments(VALID_SESSION_ID);

        Assertions.assertEquals(expected, actual);
        verify(notificationService).sendNotification(any(Long.class), any(String.class));
    }

    @Test
    @DisplayName("Verify canceledPayment() method work")
    public void canceledPayment_ValidId_ResponseDto() throws MalformedURLException {
        Payment payment = createValidPayments();
        payment.setStatus(Payment.Status.CANCEL);
        PaymentResponseDto expected = new PaymentResponseDto(
                VALID_ID,
                VALID_ID,
                VALID_SESSION_URL,
                Payment.Status.CANCEL.name(),
                VALID_SESSION_ID,
                BigDecimal.TEN
        );
        when(rentalRepository.findById(VALID_ID)).thenReturn(Optional.of(createValidRental()));
        when(paymentRepository.findBySessionId(VALID_SESSION_ID))
                .thenReturn(Optional.of(createValidPayments()));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expected);

        PaymentResponseDto actual = stripeService.canceledPayment(VALID_SESSION_ID);

        Assertions.assertEquals(expected, actual);
        verify(notificationService).sendNotification(any(Long.class), any(String.class));
    }

    @Test
    @DisplayName("Verify renewPaymentSession successfully renews a session")
    void renewPaymentSession_Successful() throws StripeException, MalformedURLException {
        Long paymentId = VALID_ID;
        String email = VALID_EMAIL;
        Payment payment = createValidPayments();
        Rental rental = createValidRental();
        rental.getUser().setEmail(email);
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("new_stripe_session_id");
        when(session.getUrl()).thenReturn("http://new.payment.url");
        PaymentResponseDto expected = new PaymentResponseDto(
                VALID_ID,
                VALID_ID,
                "http://new.payment.url",
                "Paid",
                "new_stripe_session_id",
                BigDecimal.TEN
        );
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(rentalRepository.findById(payment.getRentalId())).thenReturn(Optional.of(rental));
        when(stripeSessionService.createStripeSession(any(BigDecimal.class), anyString()))
                .thenReturn(session);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(expected);

        PaymentResponseDto actualResponse = stripeService.renewPaymentSession(paymentId, email);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expected, actualResponse);
        Assertions.assertEquals("new_stripe_session_id", actualResponse.sessionId());
        Assertions.assertEquals("http://new.payment.url", actualResponse.sessionUrl());
    }

    @Test
    @DisplayName("Verify renewPaymentSession throws EntityNotFoundException")
    void renewPaymentSession_PaymentNotFound() {
        Long paymentId = INVALID_ID;
        String email = VALID_EMAIL;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> stripeService.renewPaymentSession(paymentId, email));
    }

    @Test
    @DisplayName("Verify renewPaymentSession throws RuntimeException for invalid user email")
    void renewPaymentSession_InvalidUserEmail() throws MalformedURLException {
        Long paymentId = VALID_ID;
        String email = "invalid@email.com";

        Payment payment = createValidPayments();
        Rental rental = createValidRental();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(rentalRepository.findById(payment.getRentalId()))
                .thenReturn(Optional.of(rental));

        Assertions.assertThrows(RuntimeException.class, () -> {
            stripeService.renewPaymentSession(paymentId, email);
        });
    }

    @Test
    @DisplayName("Verify checkExpiredStripeSessions updates status for expired sessions")
    void checkExpiredStripeSessions_UpdatesExpiredSessions()
            throws StripeException, MalformedURLException {
        List<Payment> pendingPayments = List.of(createValidPayments());
        Session expiredSession = mock(Session.class);
        when(expiredSession.getStatus()).thenReturn("expired");
        when(paymentRepository.findAllByStatus(Payment.Status.PENDING)).thenReturn(pendingPayments);
        when(stripeSessionService.retrieveSession(anyString())).thenReturn(expiredSession);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stripeService.checkExpiredStripeSessions();

        for (Payment payment : pendingPayments) {
            Assertions.assertEquals(Payment.Status.EXPIRED, payment.getStatus());
        }
    }
}
