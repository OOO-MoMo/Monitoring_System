package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.momo.monitoring.store.entities.SensorType;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для типа сенсора")
public class SensorTypeDto {

        @Schema(description = "Уникальный идентификатор типа сенсора")
        private UUID id;

        @Schema(description = "Название типа сенсора", example = "Датчик температуры")
        private String name;

        @Schema(description = "Единица измерения", example = "°C")
        private String unit;

        @Schema(description = "Описание типа сенсора", example = "Измерение температуры")
        private String description;

        @Schema(description = "Дополнительные метаданные в формате JSON")
        private Map<String, Object> metadata;

        public static SensorTypeDto fromEntity(SensorType sensorType) {
                if (sensorType == null) {
                        return null;
                }
                return SensorTypeDto.builder()
                        .id(sensorType.getId())
                        .name(sensorType.getName())
                        .unit(sensorType.getUnit())
                        .description(sensorType.getDescription())
                        .metadata(sensorType.getMetadata())
                        .build();
        }
}