package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.report.AdminReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportRequest;
import ru.momo.monitoring.store.entities.User;

import java.security.Principal;
import java.time.LocalDateTime;

public interface ReportService {

    DriverActivityReportDto generateDriverActivityReportData(
            DriverActivityReportRequest request,
            Principal managerPrincipal
    );

    AdminReportDto prepareAdminReportData(LocalDateTime periodFrom, LocalDateTime periodTo, User adminUser);

}