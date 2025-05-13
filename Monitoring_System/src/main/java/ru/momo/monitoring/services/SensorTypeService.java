package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorTypeRequest;
import ru.momo.monitoring.store.dto.response.SensorTypesDto;
import ru.momo.monitoring.store.entities.SensorType;

import java.util.UUID;

public interface SensorTypeService {

    SensorTypeDto createSensorType(CreateSensorTypeRequest request);

    SensorTypesDto getAllSensorTypes();

    SensorTypeDto getSensorTypeById(UUID id);

    SensorType getSensorTypeEntityById(UUID id);

    SensorTypeDto updateSensorType(UUID id, UpdateSensorTypeRequest request);

    void deleteSensorType(UUID id);

}
