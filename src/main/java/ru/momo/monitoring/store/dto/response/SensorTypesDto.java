package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;

import java.util.List;

@Schema(description = "Список типов сенсоров")
public record SensorTypesDto(
        @Schema(description = "Список DTO типов сенсоров") List<SensorTypeDto> sensorTypesDTOList) {
}
