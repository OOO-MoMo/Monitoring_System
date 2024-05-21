package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.momo.monitoring.store.entities.Sensor;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByType(String type);

}
