package service.carsharing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.carsharing.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
