package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.response.SensorTypesDto;

import java.util.UUID;

public interface SensorTypeService {

    SensorTypeDto createSensorType(CreateSensorTypeRequest request);

    SensorTypesDto getAllSensorTypes();

    SensorTypeDto getSensorTypeById(UUID id);

}
