package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@Schema(description = "Запрос на создание типа сенсора")
public record CreateSensorTypeRequest(
        @Schema(
                description = "Уникальное название типа",
                example = "Датчик температуры",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String name,

        @Schema(
                description = "Единица измерения",
                example = "°C",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank String unit,

        @Schema(description = "Описание типа", example = "Измерение температуры в диапазоне -40°C до 85°C")
        String description,

        @Schema(
                description = "Метаданные в формате JSON",
                example = "{\"precision\": 0.1, \"interface\": [\"I2C\"]}"
        )
        Map<String, Object> metadata) {
}