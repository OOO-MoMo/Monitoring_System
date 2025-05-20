package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DriverActivityReportDto {
    private ReportHeaderForManagerDto header;
    private DriverSummaryDto driverSummary;
    private List<TechnicReportDto> technicsDetails;
}