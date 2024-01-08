package service.carsharing.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import service.carsharing.model.Car;

public interface CarRepository extends JpaRepository<Car, Long> {
    @Transactional
    default void softDelete(Long id) {
        findById(id).ifPresent(this::accept);
    }

    List<Car> findAllByDeletedFalse(Pageable pageable);

    Optional<Car> findByIdAndDeletedFalse(Long id);

    private void accept(Car car) {
        car.setDeleted(true);
        save(car);
    }
}
