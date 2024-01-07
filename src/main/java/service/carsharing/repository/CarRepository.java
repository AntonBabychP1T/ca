package service.carsharing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.carsharing.model.Car;

public interface CarRepository extends JpaRepository<Car, Long> {
}
