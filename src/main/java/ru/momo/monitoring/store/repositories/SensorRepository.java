package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.momo.monitoring.store.entities.Sensor;

import java.util.UUID;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {

}
