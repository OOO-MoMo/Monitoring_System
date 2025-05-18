package ru.momo.monitoring.store.repositories;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.store.entities.Sensor;

import java.util.List;
import java.util.UUID;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {

    boolean existsByType_Id(UUID typeId);

    boolean existsBySerialNumber(String serialNumber);

    List<Sensor> findAllByType_Id(UUID typeId);

    List<Sensor> findAllByCompanyId(UUID companyId);

    Page<Sensor> findByTechnicIsNotNull(Pageable pageable);

    Page<Sensor> findByTechnicIsNull(Pageable pageable);

    default void throwIfExistsWithSameSerialNumber(String serialNumber) {
        if (existsBySerialNumber(serialNumber)) {
            throw new EntityDuplicationException("Sensor with serial number " + serialNumber + " already exists");
        }
    }

    default Sensor findByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Sensor with id " + id + " not found"));
    }

}
