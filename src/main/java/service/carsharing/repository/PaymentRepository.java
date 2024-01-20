package service.carsharing.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import service.carsharing.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.rentalId "
            + "IN (SELECT r.id FROM Rental r WHERE r.user.id = :userId)")
    List<Payment> getAllByUserId(@Param("userId") Long userId);

    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findAllByStatus(Payment.Status status);

}
