package service.carsharing.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import service.carsharing.model.Rental;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    Optional<Rental> findByIdAndUserIdAndDeletedFalse(Long rentalId, Long userId);

    List<Rental> getAllByUserIdAndDeletedFalse(Long id);

    List<Rental> findAllByReturnDateBeforeAndActualReturnDateIsNull(LocalDate date);

    Object validateAndGetRental(Long rentalId, Long id);
}
