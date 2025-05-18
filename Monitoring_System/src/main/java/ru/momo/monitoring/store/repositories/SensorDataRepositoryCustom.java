package ru.momo.monitoring.store.repositories;

import ru.momo.monitoring.services.impl.SensorServiceImpl;
import ru.momo.monitoring.store.entities.enums.AggregationType;
import ru.momo.monitoring.store.entities.enums.DataGranularity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

interface SensorDataRepositoryCustom {
    List<SensorServiceImpl.AggregatedSensorDataView> findAggregatedData(
            UUID sensorId,
            LocalDateTime from,
            LocalDateTime to,
            DataGranularity granularity,
            AggregationType aggregationType
    );
}