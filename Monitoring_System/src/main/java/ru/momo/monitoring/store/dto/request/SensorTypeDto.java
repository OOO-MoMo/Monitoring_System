package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(description = "DTO типа сенсора")
public record SensorTypeDto(
        @Schema(description = "UUID типа сенсора", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Название типа", example = "Датчик давления")
        String name,

        @Schema(description = "Единица измерения", example = "kPa")
        String unit,

        @Schema(description = "Описание типа", example = "Измерение давления в диапазоне 0-1000 kPa")
        String description,

        @Schema(description = "Метаданные в формате JSON", example = "{\"max_response_time\": \"100ms\"}")
        Map<String, Object> metadata) {
}