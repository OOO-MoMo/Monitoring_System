package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.store.entities.SensorType;

import java.util.UUID;

public interface SensorTypeRepository extends JpaRepository<SensorType, UUID> {

    boolean existsByName(String name);

    default void throwIfExistsWithSameName(String name) {
        if (existsByName(name)) {
            throw new EntityDuplicationException("Sensor type with name " + name + " already exists");
        }
    }

    default SensorType getByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Sensor type not found"));
    }

}