package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class SensorReportDto {
    private UUID sensorId;
    private String sensorType;
    private String unitOfMeasurement;
    private String sensorSerialNumber;
    private SensorValueStatsDto valueStats;
    private SensorStatusSummaryDto statusSummary;
    private LocalDate calibrationDueDate;
}