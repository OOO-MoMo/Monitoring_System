package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.momo.monitoring.store.entities.SensorData;

import java.util.UUID;

public interface SensorDataRepository extends JpaRepository<SensorData, UUID> {
}
