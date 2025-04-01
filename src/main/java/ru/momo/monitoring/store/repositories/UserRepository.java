package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.momo.monitoring.store.entities.User;

import java.util.Optional;
import java.util.UUID;

import static ru.momo.monitoring.exceptions.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByUserData_PhoneNumber(String phoneNumber);

    default User findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with id = %d is not found", id)
                );
    }

    default User findByEmailOrThrow(String email) {
        return findByEmail(email)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with email = %s is not found", email)
                );
    }

}
