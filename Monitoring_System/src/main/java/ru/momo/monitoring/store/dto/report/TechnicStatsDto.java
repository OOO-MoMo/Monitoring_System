package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnicStatsDto {
    private String technicBrandModel;
    private String technicSerialNumber;
    private boolean isActive;
    private int numberOfSensors;
    private int criticalAlerts;
    private int warningAlerts;
}