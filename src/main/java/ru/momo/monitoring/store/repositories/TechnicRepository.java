package ru.momo.monitoring.store.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.store.entities.Technic;

import java.util.Optional;
import java.util.UUID;

import static ru.momo.monitoring.exceptions.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

@Repository
public interface TechnicRepository extends JpaRepository<Technic, UUID> {

    Page<Technic> findAllByOwnerIdIdAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
            UUID ownerId,
            String brand,
            String model,
            Pageable pageable
    );

    Optional<Technic> findBySerialNumber(String serialNumber);

    Optional<Technic> findByVin(String vin);

    default Technic findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("Technic with id = %s is not exist", id.toString())
                );
    }

    default void throwIfExistWithSameSerialNumber(String serialNumber) {
        findBySerialNumber(serialNumber).ifPresent(technic -> {
            throw new EntityDuplicationException("Technic with serial number = %s is already exists", serialNumber);
        });
    }

    default void throwIfExistWithSameVin(String vin) {
        findByVin(vin).ifPresent(technic -> {
            throw new EntityDuplicationException("Technic with vin = %s is already exists", vin);
        });
    }

}
