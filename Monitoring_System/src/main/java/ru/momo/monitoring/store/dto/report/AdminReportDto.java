package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class AdminReportDto {
    private ReportHeaderDto header;
    private GlobalSystemStatsDto globalStats;
    @Singular("companySummary")
    private List<CompanySummaryReportDto> companiesSummary;
}