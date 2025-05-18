package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.report.DriverActivityReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportRequest;

import java.security.Principal;

public interface ReportService {
    DriverActivityReportDto generateDriverActivityReportData(
            DriverActivityReportRequest request,
            Principal managerPrincipal
    );
}