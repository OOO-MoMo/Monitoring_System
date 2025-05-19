package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverSummaryDto {
    private int totalAssignedTechnics;
    private int activeAssignedTechnics;
}