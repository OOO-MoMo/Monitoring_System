package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.store.entities.Technic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.momo.monitoring.exceptions.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

public interface TechnicRepository extends JpaRepository<Technic, UUID>, JpaSpecificationExecutor<Technic> {

    Optional<Technic> findBySerialNumber(String serialNumber);

    Optional<Technic> findByVin(String vin);

    List<Technic> findByCompanyId(UUID companyId);

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
