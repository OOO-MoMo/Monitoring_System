package ru.momo.monitoring.store.mapper;

import org.mapstruct.Mapper;
import ru.momo.monitoring.store.dto.request.CreateSensorRequest;
import ru.momo.monitoring.store.dto.response.SensorDto;
import ru.momo.monitoring.store.entities.Sensor;

@Mapper(componentModel = "spring")
public interface SensorMapper {

    Sensor toEntity(CreateSensorRequest request);

    SensorDto toDto(Sensor sensorType);

}