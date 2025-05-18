package ru.momo.monitoring.store.projection;

import ru.momo.monitoring.services.impl.SensorServiceImpl;
import ru.momo.monitoring.store.entities.enums.SensorStatus;

import java.time.LocalDateTime;

public class AggregatedSensorDataViewImpl implements SensorServiceImpl.AggregatedSensorDataView {
    private final LocalDateTime intervalStart;
    private final Double aggregatedValue;
    private final SensorStatus aggregatedStatus;

    public AggregatedSensorDataViewImpl(LocalDateTime intervalStart, Double aggregatedValue, SensorStatus aggregatedStatus) {
        this.intervalStart = intervalStart;
        this.aggregatedValue = aggregatedValue;
        this.aggregatedStatus = aggregatedStatus;
    }

    @Override
    public LocalDateTime getIntervalStart() {
        return intervalStart;
    }

    @Override
    public Double getAggregatedValue() {
        return aggregatedValue;
    }

    @Override
    public SensorStatus getAggregatedStatus() {
        return aggregatedStatus;
    }
}