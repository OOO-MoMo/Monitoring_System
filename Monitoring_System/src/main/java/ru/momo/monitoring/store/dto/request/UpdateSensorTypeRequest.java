package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Запрос на обновление типа сенсора")
public record UpdateSensorTypeRequest(

        @Schema(
                description = "Новое уникальное название типа",
                example = "Датчик температуры v2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @Schema(
                description = "Новая единица измерения",
                example = "°C",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String unit,

        @Schema(description = "Новое описание типа", example = "Обновленное описание датчика температуры")
        String description,

        @Schema(
                description = "Новые метаданные в формате JSON",
                example = "{\"precision\": 0.05, \"interface\": [\"I2C\", \"SPI\"]}"
        )
        Map<String, Object> metadata

) {
}