package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportHeaderDto {
    private String driverFullName;
    private String driverEmail;
    private String companyName;
    private LocalDateTime periodFrom;
    private LocalDateTime periodTo;
    private LocalDateTime reportGeneratedAt;
    private String generatedByManagerInfo;
}