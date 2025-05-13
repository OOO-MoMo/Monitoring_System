package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Запрос на создание сенсора")
public record CreateSensorRequest(

        @Schema(description = "Unique serial number", example = "SN-123456")
        @NotBlank
        String serialNumber,

        @Schema(description = "Manufacturer name", example = "Bosch")
        String manufacturer,

        @Schema(description = "Minimum value", example = "0")
        String minValue,

        @Schema(description = "Maximum value", example = "100")
        String maxValue,

        @Schema(description = "Production date", example = "2023-01-15")
        LocalDate productionDate,

        @Schema(
                description = "Sensor type ID",
                example = "d9a5c4e1-0b7a-4e1a-8f3c-123456789abc"
        )
        @NotNull
        UUID sensorTypeId
) {
}
