package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.entities.Sensor;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Ответ на запросы связанные с сенсорами")
public record SensorDto(

        @Schema(
                description = "Unique identifier of the sensor",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
                description = "Serial number of the sensor",
                example = "SN-123456"
        )
        String serialNumber,

        @Schema(
                description = "Manufacturer of the sensor",
                example = "Siemens"
        )
        String manufacturer,

        @Schema(
                description = "Minimum measurable value",
                example = "0"
        )
        String minValue,

        @Schema(
                description = "Maximum measurable value",
                example = "100"
        )
        String maxValue,

        @Schema(
                description = "Production date",
                example = "2023-01-15"
        )
        LocalDate productionDate,


        @Schema(
                description = "Installation date",
                example = "2023-02-20"
        )
        LocalDate installationDate,

        @Schema(
                description = "Next calibration due date",
                example = "2024-02-20"
        )
        LocalDate calibrationDueDate,

        @Schema(description = "Sensor type details")
        SensorTypeDto sensorType,

        @Schema(
                description = "ID of assigned technic",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                nullable = true
        )
        UUID assignedTechnicId
) {

    public static SensorDto toDto(
            Sensor sensor
    ) {
        return new SensorDto(
                sensor.getId(),
                sensor.getSerialNumber(),
                sensor.getManufacturer(),
                sensor.getMinValue(),
                sensor.getMaxValue(),
                sensor.getProductionDate(),
                sensor.getInstallationDate(),
                sensor.getCalibrationDueDate(),
                new SensorTypeDto(
                        sensor.getType().getId(),
                        sensor.getType().getName(),
                        sensor.getType().getUnit(),
                        sensor.getType().getDescription(),
                        sensor.getType().getMetadata()
                ),
                sensor.getTechnic() == null ? null : sensor.getTechnic().getId()
        );
    }

}
