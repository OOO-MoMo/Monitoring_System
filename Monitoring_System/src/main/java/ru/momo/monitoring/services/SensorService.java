package ru.momo.monitoring.services;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import ru.momo.monitoring.store.dto.request.CreateSensorRequest;
import ru.momo.monitoring.store.dto.request.SensorAssignmentRequest;
import ru.momo.monitoring.store.dto.request.SensorDataHistoryDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorRequest;
import ru.momo.monitoring.store.dto.response.SensorDto;
import ru.momo.monitoring.store.dto.response.SensorsDto;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.enums.AggregationType;
import ru.momo.monitoring.store.entities.enums.DataGranularity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SensorService {

    SensorDto registerSensor(@Valid CreateSensorRequest request, String email);

    void assignToTechnic(@Valid SensorAssignmentRequest request, String email);

    void unassignFromTechnic(@Valid SensorAssignmentRequest request, String email);

    SensorsDto getAllCompanySensors(String email);

    SensorsDto getAllSensorsByCompanyIdForAdmin(UUID companyId);

    SensorsDto getSensorsForDriver();

    SensorsDto getSensorsByTechnicId(UUID technicId);

    SensorDto updateSensor(UUID sensorId, @Valid UpdateSensorRequest request);

    void deleteSensor(UUID sensorId);

    boolean existsByTypeId(UUID typeId);

    SensorsDto getSensorsBySensorTypeId(UUID sensorTypeId);

    Sensor getSensorEntityById(UUID sensorId);

    SensorsDto getAllSensorsPaged(Boolean attachedToTechnic, Pageable pageable);

    /**
     * Получает историю данных для указанного сенсора за период с возможностью агрегации.
     *
     * @param sensorId        ID сенсора.
     * @param from            Начало периода (UTC).
     * @param to              Конец периода (UTC).
     * @param granularity     Гранулярность агрегации (null или RAW для сырых данных).
     * @param aggregationType Тип агрегации (игнорируется, если granularity null или RAW).
     * @return Список исторических данных, отсортированный по времени.
     */
    List<SensorDataHistoryDto> getSensorDataHistory(
            UUID sensorId,
            LocalDateTime from,
            LocalDateTime to,
            DataGranularity granularity,
            AggregationType aggregationType
    );

}
