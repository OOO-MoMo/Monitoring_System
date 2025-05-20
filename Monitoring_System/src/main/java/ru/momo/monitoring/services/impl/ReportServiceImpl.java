package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.services.CompanyService;
import ru.momo.monitoring.services.ReportService;
import ru.momo.monitoring.services.SensorDataAggregationService;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.report.AdminReportDto;
import ru.momo.monitoring.store.dto.report.CompanySummaryReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportRequest;
import ru.momo.monitoring.store.dto.report.DriverSummaryDto;
import ru.momo.monitoring.store.dto.report.GlobalSystemStatsDto;
import ru.momo.monitoring.store.dto.report.ReportHeaderForAdminDto;
import ru.momo.monitoring.store.dto.report.ReportHeaderForManagerDto;
import ru.momo.monitoring.store.dto.report.SensorReportDto;
import ru.momo.monitoring.store.dto.report.SensorStatusSummaryDto;
import ru.momo.monitoring.store.dto.report.SensorValueStatsDto;
import ru.momo.monitoring.store.dto.report.TechnicReportDto;
import ru.momo.monitoring.store.dto.report.TechnicStatsDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.entities.enums.SensorStatus;
import ru.momo.monitoring.store.repositories.SensorDataRepository;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final UserService userService;
    private final SensorService sensorService;
    private final CompanyService companyService;
    private final TechnicService technicService;
    private final SensorDataAggregationService sensorDataAggregationService;
    private final SensorDataRepository sensorDataRepository;

    @Override
    @Transactional(readOnly = true)
    public DriverActivityReportDto generateDriverActivityReportData(
            DriverActivityReportRequest request,
            Principal managerPrincipal) {

        User manager = userService.getByEmail(managerPrincipal.getName());
        User driver = userService.getByIdEntity(request.getDriverId());

        if (manager.getCompany() == null || driver.getCompany() == null ||
                !manager.getCompany().getId().equals(driver.getCompany().getId())) {
            throw new AccessDeniedException("Manager can only generate reports for drivers in their own company.");
        }
        if (driver.getRole() != RoleName.ROLE_DRIVER) {
            throw new IllegalArgumentException("Report can only be generated for users with ROLE_DRIVER.");
        }

        log.info("Generating activity report for driver {} (ID: {}) by manager {} (ID: {}) for period {} - {}",
                driver.getEmail(), driver.getId(), manager.getEmail(), manager.getId(), request.getDateFrom(), request.getDateTo());

        ReportHeaderForManagerDto header = buildReportHeader(driver, manager, request.getDateFrom(), request.getDateTo());
        DriverSummaryDto driverSummary = buildDriverSummary(driver);
        List<TechnicReportDto> technicsDetails = buildTechnicsDetailsForDriver(driver, request.getDateFrom(), request.getDateTo());

        return DriverActivityReportDto.builder()
                .header(header)
                .driverSummary(driverSummary)
                .technicsDetails(technicsDetails)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReportDto prepareAdminReportData(LocalDateTime periodFrom, LocalDateTime periodTo, User adminUser) {
        log.info("Preparing admin report data for period {} - {} by admin {}", periodFrom, periodTo, adminUser.getEmail());
        ReportHeaderForAdminDto header = createAdminReportHeader(periodFrom, periodTo, adminUser); // Используем ReportHeaderDto
        GlobalSystemStatsDto globalStats = gatherGlobalSystemStats(periodFrom, periodTo);
        List<CompanySummaryReportDto> companiesSummaryList = gatherCompaniesSummary(periodFrom, periodTo);

        return AdminReportDto.builder()
                .header(header)
                .globalStats(globalStats)
                .companiesSummary(companiesSummaryList)
                .build();
    }

    // --- Вспомогательные методы для отчета администратора ---

    private ReportHeaderForAdminDto createAdminReportHeader(LocalDateTime periodFrom, LocalDateTime periodTo, User adminUser) {
        String adminInfo = "Система";
        if (adminUser != null) {
            UserData adminData = adminUser.getUserData();
            adminInfo = (adminData != null && adminData.getFirstname() != null && adminData.getLastname() != null)
                    ? String.format("%s %s (%s)", adminData.getLastname(), adminData.getFirstname(), adminUser.getEmail()).trim()
                    : adminUser.getEmail();
        }

        return ReportHeaderForAdminDto.builder() // Убедись, что AdminReportDto.header этого типа
                .reportName("Сводный отчет по системе мониторинга")
                .periodFrom(periodFrom)
                .periodTo(periodTo)
                .reportGeneratedAt(LocalDateTime.now())
                .reportGeneratedBy(adminInfo) // Поле в ReportHeaderDto может называться иначе
                .build();
    }

    private GlobalSystemStatsDto gatherGlobalSystemStats(LocalDateTime periodFrom, LocalDateTime periodTo) {
        int totalCompanies = companyService.countTotalCompanies();
        int totalTechnics = technicService.countTotalTechnics();
        int totalActiveTechnics = technicService.countTotalActiveTechnics();
        int totalSensors = sensorService.countTotalSensors();
        int totalActiveSensors = sensorService.countTotalActiveSensors();

        long totalViolations = sensorDataAggregationService.countSystemViolations(periodFrom, periodTo);
        double totalOperatingHours = sensorDataAggregationService.calculateTotalSystemOperatingHours(periodFrom, periodTo);

        double avgViolations = (totalOperatingHours > 0.001) ? (double) totalViolations / totalOperatingHours : 0.0;

        return GlobalSystemStatsDto.builder()
                .totalCompanies(totalCompanies)
                .totalTechnics(totalTechnics)
                .totalActiveTechnics(totalActiveTechnics)
                .totalSensors(totalSensors)
                .totalActiveSensors(totalActiveSensors)
                .averageViolationsPerHour(avgViolations)
                .build();
    }

    private List<CompanySummaryReportDto> gatherCompaniesSummary(LocalDateTime periodFrom, LocalDateTime periodTo) {
        List<CompanySummaryReportDto> summaries = new ArrayList<>();
        List<Company> allCompanies = companyService.findAllCompaniesForReport();

        for (Company company : allCompanies) {
            int technicsInCompany = technicService.countTechnicsByCompany(company.getId());
            int activeTechnicsInCompany = technicService.countActiveTechnicsByCompany(company.getId());
            int sensorsInCompany = sensorService.countSensorsByCompany(company.getId());
            int activeSensorsInCompany = sensorService.countActiveSensorsByCompany(company.getId());

            long companyTotalViolations = sensorDataAggregationService.countCompanyViolations(company.getId(), periodFrom, periodTo);
            double companyOperatingHours = sensorDataAggregationService.calculateCompanyOperatingHours(company.getId(), periodFrom, periodTo);
            double companyAvgViolations = (companyOperatingHours > 0.001) ? (double) companyTotalViolations / companyOperatingHours : 0.0;

            List<TechnicStatsDto> technicStatsList = gatherTechnicStatsForCompany(company.getId(), periodFrom, periodTo);

            summaries.add(CompanySummaryReportDto.builder()
                    .companyName(company.getName())
                    .companyInn(company.getInn())
                    .totalTechnicsInCompany(technicsInCompany)
                    .activeTechnicsInCompany(activeTechnicsInCompany)
                    .totalSensorsInCompany(sensorsInCompany)
                    .activeSensorsInCompany(activeSensorsInCompany)
                    .companyViolationsPerHour(companyAvgViolations)
                    .technicsStats(technicStatsList)
                    .build());
        }
        return summaries;
    }

    private List<TechnicStatsDto> gatherTechnicStatsForCompany(UUID companyId, LocalDateTime periodFrom, LocalDateTime periodTo) {
        List<TechnicStatsDto> statsList = new ArrayList<>();
        List<Technic> technicsOfCompany = technicService.findAllTechnicsByCompany(companyId);

        for (Technic technic : technicsOfCompany) {
            int numberOfSensorsOnTechnic = sensorService.countActiveSensorsByTechnic(technic.getId());

            long criticals = sensorDataAggregationService.countTechnicAlertsByStatus(technic.getId(), SensorStatus.CRITICAL, periodFrom, periodTo);
            long warnings = sensorDataAggregationService.countTechnicAlertsByStatus(technic.getId(), SensorStatus.WARNING, periodFrom, periodTo);

            statsList.add(TechnicStatsDto.builder()
                    .technicBrandModel(strVal(technic.getBrand()) + " " + strVal(technic.getModel()))
                    .technicSerialNumber(strVal(technic.getSerialNumber()))
                    .isActive(technic.getIsActive())
                    .numberOfSensors(numberOfSensorsOnTechnic)
                    .criticalAlerts((int) criticals)
                    .warningAlerts((int) warnings)
                    .build());
        }
        return statsList;
    }


    // --- Вспомогательные методы для отчета водителя (generateDriverActivityReportData) ---

    private List<TechnicReportDto> buildTechnicsDetailsForDriver(User driver, LocalDateTime from, LocalDateTime to) {
        List<TechnicReportDto> technicsDetails = new ArrayList<>();
        List<Technic> assignedTechnics = driver.getTechnics();

        if (assignedTechnics != null) {
            for (Technic technic : assignedTechnics) {
                List<SensorReportDto> sensorsSummaryList = new ArrayList<>();
                if (technic.getSensors() != null) {
                    for (Sensor sensor : technic.getSensors()) {
                        if (sensor.getIsActive()) {
                            SensorValueStatsDto valueStats = getValueStatistics(sensor.getId(), from, to);
                            SensorStatusSummaryDto statusSummary = getStatusSummary(sensor.getId(), from, to);

                            sensorsSummaryList.add(SensorReportDto.builder()
                                    .sensorId(sensor.getId())
                                    .sensorType(sensor.getType() != null ? sensor.getType().getName() : "N/A")
                                    .unitOfMeasurement(sensor.getType() != null ? sensor.getType().getUnit() : "N/A")
                                    .sensorSerialNumber(sensor.getSerialNumber())
                                    .valueStats(valueStats)
                                    .statusSummary(statusSummary)
                                    .calibrationDueDate(sensor.getCalibrationDueDate())
                                    .build());
                        }
                    }
                }
                technicsDetails.add(TechnicReportDto.builder()
                        .technicId(technic.getId())
                        .brand(technic.getBrand())
                        .model(technic.getModel())
                        .year(technic.getYear())
                        .serialNumber(technic.getSerialNumber())
                        .vin(technic.getVin())
                        .isActive(technic.getIsActive())
                        .lastServiceDate(technic.getLastServiceDate())
                        .nextServiceDate(technic.getNextServiceDate())
                        .description(technic.getDescription())
                        .sensorsSummary(sensorsSummaryList)
                        .build());
            }
        }
        return technicsDetails;
    }


    private ReportHeaderForManagerDto buildReportHeader(User driver, User manager, LocalDateTime from, LocalDateTime to) {
        UserData driverData = driver.getUserData();
        UserData managerData = manager.getUserData();
        Company company = driver.getCompany();

        String driverFullName = "Н/Д";
        if (driverData != null && driverData.getLastname() != null && driverData.getFirstname() != null) {
            driverFullName = String.format("%s %s %s",
                            strVal(driverData.getLastname()),
                            strVal(driverData.getFirstname()),
                            strVal(driverData.getPatronymic()))
                    .replaceAll("\\sН/Д", "").trim();
        }


        String managerInfo = manager.getEmail();
        if (managerData != null && managerData.getLastname() != null && managerData.getFirstname() != null) {
            managerInfo = String.format("%s %s (%s)",
                    strVal(managerData.getLastname()),
                    strVal(managerData.getFirstname()),
                    manager.getEmail()).trim();
        }


        return ReportHeaderForManagerDto.builder()
                .driverFullName(driverFullName)
                .driverEmail(driver.getEmail())
                .companyName(company != null ? company.getName() : "Н/Д")
                .periodFrom(from)
                .periodTo(to)
                .reportGeneratedAt(LocalDateTime.now())
                .generatedByManagerInfo(managerInfo)
                .build();
    }

    private DriverSummaryDto buildDriverSummary(User driver) {
        int totalAssigned = driver.getTechnics() != null ? driver.getTechnics().size() : 0;
        int activeAssigned = driver.getTechnics() != null ?
                (int) driver.getTechnics().stream().filter(Technic::getIsActive).count() : 0;

        return DriverSummaryDto.builder()
                .totalAssignedTechnics(totalAssigned)
                .activeAssignedTechnics(activeAssigned)
                .build();
    }

    private SensorValueStatsDto getValueStatistics(UUID sensorId, LocalDateTime from, LocalDateTime to) {
        return sensorService.getSensorValueStatisticsForPeriod(sensorId, from, to);
    }

    private SensorStatusSummaryDto getStatusSummary(UUID sensorId, LocalDateTime from, LocalDateTime to) {
        List<Object[]> statusCountsResult = sensorDataRepository.countSensorStatusesInPeriod(sensorId, from, to);
        long criticalCount = 0;
        long warningCount = 0;

        for (Object[] row : statusCountsResult) {
            SensorStatus statusName = (row[0] instanceof SensorStatus) ? (SensorStatus) row[0] : null;

            Number count = (Number) row[1];

            if (statusName != null && count != null) {
                if (statusName == SensorStatus.CRITICAL) {
                    criticalCount = count.longValue();
                } else if (statusName == SensorStatus.WARNING) {
                    warningCount = count.longValue();
                }
            }
        }
        return SensorStatusSummaryDto.builder()
                .criticalCount(criticalCount)
                .warningCount(warningCount)
                .build();
    }


    private String formatDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return "0 ч 00 м";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%d ч %02d м", hours, minutes);
    }

    private String strVal(Object obj) {
        if (obj == null) return "Н/Д";
        String s = obj.toString();
        return s.isBlank() ? "Н/Д" : s;
    }
}