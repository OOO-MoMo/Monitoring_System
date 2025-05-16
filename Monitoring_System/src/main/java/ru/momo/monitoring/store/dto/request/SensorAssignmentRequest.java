package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Запрос на прикрепление/открепление сенсора")
public record SensorAssignmentRequest(
        @Schema(
                description = "Technic ID for assignment",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        @NotNull
        UUID technicId,

        @Schema(
                description = "Sensor ID to assign",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotNull
        UUID sensorId
) {
}
