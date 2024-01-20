package service.carsharing.service.impl;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.carsharing.dto.rental.RentalRequestDto;
import service.carsharing.dto.rental.RentalResponseDto;
import service.carsharing.mapper.RentalMapper;
import service.carsharing.model.Car;
import service.carsharing.model.Rental;
import service.carsharing.model.User;
import service.carsharing.repository.CarRepository;
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

    @Override
    @Transactional
    public RentalResponseDto addNewRental(RentalRequestDto requestDto) {
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
