package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorValueStatsDto {
    private Double minValue;
    private Double maxValue;
    private Double avgValue;
    private Double lastValue;
}