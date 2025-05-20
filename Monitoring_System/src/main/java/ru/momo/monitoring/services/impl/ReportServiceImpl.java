package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.services.ReportService;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.report.DriverActivityReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportRequest;
import ru.momo.monitoring.store.dto.report.DriverSummaryDto;
import ru.momo.monitoring.store.dto.report.ReportHeaderDto;
import ru.momo.monitoring.store.dto.report.SensorReportDto;
import ru.momo.monitoring.store.dto.report.SensorStatusSummaryDto;
import ru.momo.monitoring.store.dto.report.SensorValueStatsDto;
import ru.momo.monitoring.store.dto.report.TechnicReportDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.entities.enums.SensorStatus;
import ru.momo.monitoring.store.repositories.SensorDataRepository;

import java.security.Principal;
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

        // 1. Формируем заголовок
        ReportHeaderDto header = buildReportHeader(driver, manager, request.getDateFrom(), request.getDateTo());

        // 2. Формируем сводку по водителю
        DriverSummaryDto driverSummary = buildDriverSummary(driver);

        // 3. Формируем детали по технике
        List<TechnicReportDto> technicsDetails = new ArrayList<>();
        List<Technic> assignedTechnics = driver.getTechnics();

        if (assignedTechnics != null) {
            for (Technic technic : assignedTechnics) {
                List<SensorReportDto> sensorsSummaryList = new ArrayList<>();
                if (technic.getSensors() != null) {
                    for (Sensor sensor : technic.getSensors()) {
                        if (sensor.getIsActive()) {
                            SensorValueStatsDto valueStats = getValueStatistics(sensor.getId(), request.getDateFrom(), request.getDateTo());
                            SensorStatusSummaryDto statusSummary = getStatusSummary(sensor.getId(), request.getDateFrom(), request.getDateTo());

                            sensorsSummaryList.add(SensorReportDto.builder()
                                    .sensorId(sensor.getId())
                                    .sensorType(sensor.getType().getName())
                                    .unitOfMeasurement(sensor.getType().getUnit())
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

        return DriverActivityReportDto.builder()
                .header(header)
                .driverSummary(driverSummary)
                .technicsDetails(technicsDetails)
                .build();
    }

    private ReportHeaderDto buildReportHeader(User driver, User manager, LocalDateTime from, LocalDateTime to) {
        UserData driverData = driver.getUserData();
        UserData managerData = manager.getUserData();
        Company company = driver.getCompany();

        return ReportHeaderDto.builder()
                .driverFullName(driverData != null ? String.format("%s %s %s", driverData.getLastname(), driverData.getFirstname(), driverData.getPatronymic()).trim() : "N/A")
                .driverEmail(driver.getEmail())
                .companyName(company != null ? company.getName() : "N/A")
                .periodFrom(from)
                .periodTo(to)
                .reportGeneratedAt(LocalDateTime.now())
                .generatedByManagerInfo(managerData != null ? String.format("%s %s (%s)", managerData.getLastname(), managerData.getFirstname(), manager.getEmail()).trim() : manager.getEmail())
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
            String statusName = (String) row[0];
            Number count = (Number) row[1];

            if (statusName != null && count != null) {
                if (SensorStatus.CRITICAL.name().equalsIgnoreCase(statusName)) {
                    criticalCount = count.longValue();
                } else if (SensorStatus.WARNING.name().equalsIgnoreCase(statusName)) {
                    warningCount = count.longValue();
                }
            }
        }
        return SensorStatusSummaryDto.builder()
                .criticalCount(criticalCount)
                .warningCount(warningCount)
                .build();
    }
}