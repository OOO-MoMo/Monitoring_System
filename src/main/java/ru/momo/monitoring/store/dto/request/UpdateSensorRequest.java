package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Данные для обновления сенсора. Все поля опциональны.")
public record UpdateSensorRequest(
        @Schema(description = "Новый серийный номер сенсора (должен быть уникальным, если изменяется)", example = "SN-UPDATED-123")
        @Size(max = 255)
        String serialNumber,

        @Schema(description = "Новый производитель сенсора", example = "Advanced Sensors Inc.")
        @Size(max = 255)
        String manufacturer,

        @Schema(description = "Новое минимальное значение, которое может измерять сенсор", example = "0.0")
        String minValue,

        @Schema(description = "Новое максимальное значение, которое может измерять сенсор", example = "150.0")
        String maxValue,

        @Schema(description = "Новая дата производства сенсора", example = "2023-01-10")
        LocalDate productionDate,

        @Schema(description = "Новая дата следующей калибровки", example = "2024-12-31")
        LocalDate calibrationDueDate,

        @Schema(description = "Новый статус активности сенсора (true - активен, false - неактивен)", example = "false")
        Boolean isActive
) {
}