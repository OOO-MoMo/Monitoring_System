package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.momo.monitoring.store.entities.Sensor;

import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByType(String type);

    @Query("SELECT s FROM sensors s JOIN s.technics t WHERE t.technicId = :technicId")
    List<Sensor> findByTechnicId(@Param("technicId") Long technicId);

}
