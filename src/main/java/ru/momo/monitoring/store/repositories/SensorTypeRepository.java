package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.store.entities.SensorType;

import java.util.Optional;
import java.util.UUID;

public interface SensorTypeRepository extends JpaRepository<SensorType, UUID> {

    Optional<SensorType> findByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsByName(String name);

    default void throwIfExistsWithSameName(String name) {
        if (existsByName(name)) {
            throw new EntityDuplicationException("Sensor type with name " + name + " already exists");
        }
    }

    default SensorType getByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Sensor type not found"));
    }

    default void throwIfExistsWithSameNameAndDifferentId(String name, UUID id) {
        if (existsByNameAndIdNot(name, id)) {
            throw new EntityDuplicationException("Sensor type with name '" + name + "' already exists for a different ID");
        }
    }

}