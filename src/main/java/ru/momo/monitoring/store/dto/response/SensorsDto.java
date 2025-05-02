package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Список сенсоров")
public record SensorsDto(
        @Schema(description = "List of sensors")
        List<SensorDto> sensors
) {
}
