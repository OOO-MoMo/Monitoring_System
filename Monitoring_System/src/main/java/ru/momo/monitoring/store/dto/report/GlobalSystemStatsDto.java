package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalSystemStatsDto {
    private int totalCompanies;
    private int totalTechnics;
    private int totalActiveTechnics;
    private int totalSensors;
    private int totalActiveSensors;
    private double averageViolationsPerHour;
}
