package ru.momo.monitoring.services;

import ru.momo.monitoring.store.entities.enums.SensorStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SensorDataAggregationService {

    long countSystemViolations(LocalDateTime from, LocalDateTime to);

    double calculateTotalSystemOperatingHours(LocalDateTime from, LocalDateTime to);

    long countCompanyViolations(UUID companyId, LocalDateTime from, LocalDateTime to);

    double calculateCompanyOperatingHours(UUID companyId, LocalDateTime from, LocalDateTime to);

    long countTechnicAlertsByStatus(UUID technicId, SensorStatus status, LocalDateTime from, LocalDateTime to);

}