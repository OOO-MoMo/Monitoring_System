package ru.momo.monitoring.store.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorTypeRequest;
import ru.momo.monitoring.store.entities.SensorType;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SensorTypeMapper {

    SensorType toEntity(CreateSensorTypeRequest request);

    SensorTypeDto toDto(SensorType sensorType);

    void updateEntityFromRequest(UpdateSensorTypeRequest request, @MappingTarget SensorType sensorType);

}