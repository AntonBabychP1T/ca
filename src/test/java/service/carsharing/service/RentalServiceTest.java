package service.carsharing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.carsharing.dto.rental.RentalRequestDto;
import service.carsharing.dto.rental.RentalResponseDto;
import service.carsharing.mapper.RentalMapper;
import service.carsharing.model.Car;
import service.carsharing.model.Payment;
import service.carsharing.model.Rental;
import service.carsharing.model.User;
import service.carsharing.repository.CarRepository;
import service.carsharing.repository.PaymentRepository;
import service.carsharing.repository.RentalRepository;
import service.carsharing.repository.UserRepository;
import service.carsharing.service.impl.RentalServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    private static final String VALID_EMAIL = "test@email.com";
    private static final String VALID_PASSWORD = "Password";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    private static final Long VALID_ID = 1L;
    private static final String VALID_MODEL = "Valid Model";
    private static final String VALID_BRAND = "Valid Brand";
    private static final Car.Type VALID_TYPE = Car.Type.CUV;
    private static final Integer VALID_INVENTORY = 2;
    private static final BigDecimal VALID_FEE = BigDecimal.TEN;
    private static final boolean NOT_DELETED = false;
    private static final LocalDate VALID_RENTAL_DAY = LocalDate.now();
    private static final LocalDate VALID_RETURN_DAY = LocalDate.now().plusDays(5);
    private static final LocalDate VALID_ACTUAL_RETURN_DAY = LocalDate.now().plusDays(4);

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentRepository paymentRepository;

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

    private Car createValidCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setInventory(VALID_INVENTORY);
        car.setFee(VALID_FEE);
        car.setDeleted(NOT_DELETED);
        car.setModel(VALID_MODEL);
        car.setBrand(VALID_BRAND);
        car.setType(VALID_TYPE);
        return car;
    }

    private Rental createValidRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setRentalDate(VALID_RENTAL_DAY);
        rental.setReturnDate(VALID_RETURN_DAY);
        rental.setCar(createValidCar());
        rental.setUser(createValidUser());
        rental.setDeleted(false);
        return rental;
    }

    private RentalRequestDto createValidRentalRequestDto() {
        return new RentalRequestDto(
                VALID_RENTAL_DAY,
                VALID_RETURN_DAY,
                VALID_ID,
                VALID_ID
        );
    }

    private RentalResponseDto createValidRentalResponseDto() {
        return new RentalResponseDto(
                VALID_RENTAL_DAY,
                VALID_RETURN_DAY,
                null,
                VALID_ID,
                VALID_ID
        );
    }

    private Payment createExpiredPayments() {
        Payment payment = new Payment();
        payment.setId(VALID_ID);
        payment.setRentalId(VALID_ID);
        try {
            payment.setSessionUrl(new URL("http://localhost:8080/"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        payment.setStatus(Payment.Status.EXPIRED);
        payment.setSessionId("SESSION ID");
        payment.setType(Payment.Type.PAYMENT);
        payment.setAmountToPay(BigDecimal.TEN);
        return payment;
    }

    private RentalResponseDto returnedRentalResponseDto() {
        return new RentalResponseDto(
                VALID_RENTAL_DAY,
                VALID_RETURN_DAY,
                VALID_ACTUAL_RETURN_DAY,
                VALID_ID,
                VALID_ID
        );
    }

    private Rental createOverdueRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setRentalDate(LocalDate.now().minusDays(5));
        rental.setReturnDate(VALID_RETURN_DAY);
        rental.setCar(createValidCar());
        rental.setUser(createValidUser());
        rental.setDeleted(false);
        return rental;
    }

    @Test
    @DisplayName("Verify addNewRental() method creates a valid rental")
    public void addNewRental_ValidRentalRequestDto_ValidRentalResponseDto() {
        RentalRequestDto requestDto = createValidRentalRequestDto();
        Car car = createValidCar();
        User user = createValidUser();
        Rental rental = createValidRental();
        RentalResponseDto expectedResponse = createValidRentalResponseDto();
        when(paymentRepository.getAllByUserId(user.getId())).thenReturn(List.of());
        when(carRepository.findByIdAndDeletedFalse(requestDto.carId()))
                .thenReturn(Optional.of(car));
        when(rentalMapper.toModel(requestDto)).thenReturn(rental);
        when(rentalRepository.save(ArgumentMatchers.any(Rental.class))).thenReturn(rental);
        when(rentalMapper.toDto(ArgumentMatchers.any(Rental.class))).thenReturn(expectedResponse);

        RentalResponseDto actualResponse = rentalService.addNewRental(requestDto);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Verify addNewRental() throws RuntimeException for expired payments")
    public void addNewRental_ExpiredPayments_ThrowsRuntimeException() {
        RentalRequestDto requestDto = createValidRentalRequestDto();
        List<Payment> expiredPayments = List.of(createExpiredPayments());
        when(paymentRepository.getAllByUserId(requestDto.userId()))
                .thenReturn(expiredPayments);

        Assertions.assertThrows(RuntimeException.class,
                () -> rentalService.addNewRental(requestDto));
    }

    @Test
    @DisplayName("Verify getAllCurrentRentals() returns only active rentals")
    public void getAllCurrentRentals_ActiveRentals() {
        Long userId = VALID_ID;
        Rental returnedRental = createValidRental();
        Rental deafualtRental = createValidRental();
        returnedRental.setActualReturnDate(VALID_ACTUAL_RETURN_DAY);
        RentalResponseDto responseDto = createValidRentalResponseDto();
        List<Rental> rentals = List.of(returnedRental, deafualtRental);
        when(rentalRepository.getAllByUserIdAndDeletedFalse(userId)).thenReturn(rentals);
        when(rentalMapper.toDto(deafualtRental)).thenReturn(responseDto);

        List<RentalResponseDto> actual = rentalService.getAllCurrentRentals(userId, true);

        assertEquals(List.of(responseDto), actual);
    }

    @Test
    @DisplayName("Verify getAllCurrentRentals() returns only inactive rentals")
    public void getAllCurrentRentals_Rentals_InactiveRentals() {
        Long userId = VALID_ID;
        Rental returnedRental = createValidRental();
        Rental deafualtRental = createValidRental();
        returnedRental.setActualReturnDate(VALID_ACTUAL_RETURN_DAY);
        RentalResponseDto responseDto = returnedRentalResponseDto();
        List<Rental> rentals = List.of(returnedRental, deafualtRental);
        when(rentalRepository.getAllByUserIdAndDeletedFalse(userId)).thenReturn(rentals);
        when(rentalMapper.toDto(returnedRental)).thenReturn(responseDto);

        List<RentalResponseDto> actual = rentalService.getAllCurrentRentals(userId, false);

        assertEquals(List.of(responseDto), actual);
    }

    @Test
    @DisplayName("Verify getRentalById() method work")
    public void getRentalById_ValidIdAndEmail_ValidRentalResponseDto() {
        Rental rental = createValidRental();
        RentalResponseDto expected = createValidRentalResponseDto();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(createValidUser()));
        when(rentalRepository.findByIdAndUserIdAndDeletedFalse(VALID_ID, VALID_ID))
                .thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalResponseDto actual = rentalService.getRentalById(VALID_ID, VALID_EMAIL);

        assertEquals(expected, actual);
    }

    @Test
    public void returnRental_ValidRentalId_RentalReturned() {
        User user = createValidUser();
        Rental rental = createValidRental();
        Car car = createValidCar();
        RentalResponseDto expected = returnedRentalResponseDto();
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(rentalRepository.findByIdAndUserIdAndDeletedFalse(VALID_ID, VALID_ID))
                .thenReturn(Optional.of(rental));
        when(carRepository.findByIdAndDeletedFalse(VALID_ID)).thenReturn(Optional.of(car));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalResponseDto actual = rentalService.returnRental(VALID_ID, VALID_EMAIL);

        assertEquals(expected, actual);
        assertEquals(VALID_INVENTORY + 1, car.getInventory());
        verify(notificationService)
                .sendNotification(VALID_ID, "You successfully return your rental"
                        + " with id: " + VALID_ID);
    }

    @Test
    @DisplayName("Verify checkOverdueRentals sends notifications for overdue rentals")
    public void checkOverdueRentals_WithOverdueRentals() {
        List<Rental> overdueRentals = List.of(createOverdueRental());
        when(rentalRepository.findAllByReturnDateBeforeAndActualReturnDateIsNull(
                ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(overdueRentals);

        rentalService.checkOverdueRentals();

        for (Rental rental : overdueRentals) {
            verify(notificationService)
                    .sendNotification(ArgumentMatchers.eq(rental.getUser().getId()),
                    ArgumentMatchers.anyString());
        }
    }

    @Test
    @DisplayName("Verify checkOverdueRentals sends global notification when no rentals are overdue")
    public void checkOverdueRentals_NoOverdueRentals() {
        when(rentalRepository.findAllByReturnDateBeforeAndActualReturnDateIsNull(
                ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        rentalService.checkOverdueRentals();

        verify(notificationService).sendGlobalNotification("No rentals overdue today!");
    }
}

