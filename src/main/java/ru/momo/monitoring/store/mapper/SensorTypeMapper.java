package ru.momo.monitoring.store.mapper;

import org.mapstruct.Mapper;
import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.entities.SensorType;

@Mapper(componentModel = "spring")
public interface SensorTypeMapper {

    SensorType toEntity(CreateSensorTypeRequest request);

    SensorTypeDto toDto(SensorType sensorType);

}