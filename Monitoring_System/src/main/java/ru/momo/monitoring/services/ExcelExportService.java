package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.SensorDataHistoryDto;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface ExcelExportService {

    ByteArrayInputStream generateSensorHistoryExcel(
            UUID sensorId,
            String sensorInfo,
            String periodFromString,
            String periodToString,
            String reportGeneratedAt,
            List<SensorDataHistoryDto> data
    );

}