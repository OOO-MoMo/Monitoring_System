package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.momo.monitoring.store.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.momo.monitoring.exceptions.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByUserData_PhoneNumber(String phoneNumber);

    List<User> findUserByCompany_Id(UUID companyId);

    default User findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with id = %s is not found", id.toString())
                );
    }

    default User findByEmailOrThrow(String email) {
        return findByEmail(email)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with email = %s is not found", email)
                );
    }

}
