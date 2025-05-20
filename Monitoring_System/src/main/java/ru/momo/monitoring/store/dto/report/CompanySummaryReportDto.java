package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class CompanySummaryReportDto {
    private String companyName;
    private String companyInn;
    private int totalTechnicsInCompany;
    private int activeTechnicsInCompany;
    private int totalSensorsInCompany;
    private int activeSensorsInCompany;
    private double companyViolationsPerHour;
    @Singular("technicStat")
    private List<TechnicStatsDto> technicsStats;
}