package service.carsharing.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import service.carsharing.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Role.RoleName roleName);
}
