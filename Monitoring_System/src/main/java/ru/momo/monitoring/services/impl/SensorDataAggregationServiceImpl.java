package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.services.SensorDataAggregationService;
import ru.momo.monitoring.store.entities.enums.SensorStatus;
import ru.momo.monitoring.store.repositories.SensorDataRepository;
import ru.momo.monitoring.store.repositories.TechnicRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SensorDataAggregationServiceImpl implements SensorDataAggregationService {

    private final SensorDataRepository sensorDataRepository;
    private final TechnicRepository technicRepository;

    @Override
    @Transactional(readOnly = true)
    public long countSystemViolations(LocalDateTime from, LocalDateTime to) {
        return sensorDataRepository.countByStatusInAndTimestampBetween(
                List.of(SensorStatus.WARNING, SensorStatus.CRITICAL), from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateTotalSystemOperatingHours(LocalDateTime from, LocalDateTime to) {
        Long distinctHoursWithActivity = sensorDataRepository.countDistinctHoursWithActiveTechnicData(from, to);
        return distinctHoursWithActivity != null ? distinctHoursWithActivity.doubleValue() : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompanyViolations(UUID companyId, LocalDateTime from, LocalDateTime to) {
        return sensorDataRepository.countBySensor_Company_IdAndStatusInAndTimestampBetween(
                companyId, List.of(SensorStatus.WARNING, SensorStatus.CRITICAL), from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCompanyOperatingHours(UUID companyId, LocalDateTime from, LocalDateTime to) {
        Long distinctHours = sensorDataRepository.countDistinctHoursWithActiveTechnicDataForCompany(companyId, from, to);
        return distinctHours != null ? distinctHours.doubleValue() : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countTechnicAlertsByStatus(UUID technicId, SensorStatus status, LocalDateTime from, LocalDateTime to) {
        if (technicId == null || status == null || from == null || to == null) {
            return 0;
        }
        if (from.isAfter(to)) {
            return 0;
        }
        return sensorDataRepository.countByTechnic_IdAndStatusAndTimestampBetween(
                technicId, status, from, to);
    }
}