package ru.momo.monitoring.store.dto.report;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportHeaderForAdminDto {
    private String reportName;
    private LocalDateTime periodFrom;
    private LocalDateTime periodTo;
    private LocalDateTime reportGeneratedAt;
    private String reportGeneratedBy;
}
