package service.carsharing.service.impl;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import service.carsharing.service.NotificationService;
import service.carsharing.service.RentalService;

@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public RentalResponseDto addNewRental(RentalRequestDto requestDto) {
        if (!canBorrow(requestDto.userId())) {
            throw new RuntimeException("You have expired payments, denied access");
        }
        Car car = getCarById(requestDto.carId());
        if (car.getInventory() < 1) {
            notificationService.sendNotification(requestDto.userId(),
                    "There is no free available car with id: "
                            + requestDto.carId());
            throw new RuntimeException("There is no free available car with id: "
                    + requestDto.carId());
        }
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);
        RentalResponseDto responseDto = rentalMapper.toDto(rentalRepository
                .save(rentalMapper.toModel(requestDto)));
        notificationService.sendNotification(requestDto.userId(),
                "Your rental created! Meta info: " + responseDto.toString());
        return responseDto;
    }

    @Override
    public List<RentalResponseDto> getAllCurrentRentals(Long userId, boolean isActive) {
        List<Rental> allRentalsByUser = rentalRepository.getAllByUserIdAndDeletedFalse(userId);
        if (isActive) {
            return allRentalsByUser.stream()
                    .filter(rental -> rental.getActualReturnDate() == null)
                    .map(rentalMapper::toDto)
                    .toList();
        }
        return allRentalsByUser.stream()
                .filter(rental -> rental.getActualReturnDate() != null)
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public RentalResponseDto getRentalById(Long id, String email) {
        Rental rental = getRentalByIdAndUserId(id, getUserByEmail(email).getId());
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalResponseDto returnRental(Long rentalId, String email) {
        Long userById = getUserByEmail(email).getId();
        Rental rental = getRentalByIdAndUserId(rentalId, userById);
        Car car = getCarById(rental.getCar().getId());
        car.setInventory(car.getInventory() + 1);
        rental.setActualReturnDate(LocalDate.now());
        RentalResponseDto responseDto = rentalMapper.toDto(rentalRepository.save(rental));
        notificationService.sendNotification(userById, "You successfully return your rental"
                + " with id: " + rentalId);
        return responseDto;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkOverdueRentals() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals =
                rentalRepository.findAllByReturnDateBeforeAndActualReturnDateIsNull(today);
        if (overdueRentals.isEmpty()) {
            notificationService.sendGlobalNotification("No rentals overdue today!");
            return;
        }
        for (Rental rental : overdueRentals) {
            String message = createOverdueRentalMessage(rental);
            notificationService.sendNotification(rental.getUser().getId(), message);
        }
    }

    private boolean canBorrow(Long userId) {
        List<Payment> userPayments = paymentRepository.getAllByUserId(userId);
        Optional<Payment.Status> expiredState = userPayments.stream().map(Payment::getStatus)
                .filter(status -> status == Payment.Status.EXPIRED)
                .findAny();
        return expiredState.isEmpty();
    }

    private String createOverdueRentalMessage(Rental rental) {
        return "Overdue rental alert! Rental ID: " + rental.getId()
                + ", User ID: " + rental.getUser().getId()
                + ", Car ID: " + rental.getCar().getId()
                + ", Return Date: " + rental.getReturnDate();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with email: " + email)
        );
    }

    private Rental getRentalByIdAndUserId(Long rentalId, Long userId) {
        return rentalRepository
                .findByIdAndUserIdAndDeletedFalse(rentalId, userId).orElseThrow(
                        () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
                );
    }

    private Car getCarById(Long id) {
        return carRepository.findByIdAndDeletedFalse(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id: " + id)
        );
    }
}
