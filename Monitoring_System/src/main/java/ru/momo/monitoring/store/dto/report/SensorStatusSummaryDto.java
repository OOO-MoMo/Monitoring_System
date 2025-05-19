package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorStatusSummaryDto {
    private long criticalCount;
    private long warningCount;
}