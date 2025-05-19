package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TechnicReportDto {
    private UUID technicId;
    private String brand;
    private String model;
    private Integer year;
    private String serialNumber;
    private String vin;
    private Boolean isActive;
    private LocalDateTime lastServiceDate;
    private LocalDateTime nextServiceDate;
    private List<SensorReportDto> sensorsSummary;
}