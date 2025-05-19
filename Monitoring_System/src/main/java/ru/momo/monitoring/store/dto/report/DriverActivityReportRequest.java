package ru.momo.monitoring.store.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на генерацию отчета по активности водителя")
public class DriverActivityReportRequest {

    @Schema(description = "Уникальный идентификатор (UUID) водителя, для которого формируется отчет.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @NotNull(message = "Driver ID не может быть пустым")
    private UUID driverId;

    @Schema(description = "Дата и время начала отчетного периода. Формат ISO 8601 (YYYY-MM-DDTHH:MM:SS). Рекомендуется передавать в UTC.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2024-05-01T00:00:00")
    @NotNull(message = "Дата начала периода не может быть пустой")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateFrom;

    @Schema(description = "Дата и время конца отчетного периода. Формат ISO 8601 (YYYY-MM-DDTHH:MM:SS). Рекомендуется передавать в UTC.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2024-05-31T23:59:59")
    @NotNull(message = "Дата конца периода не может быть пустой")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateTo;

    @Schema(description = "Желаемый формат отчета. Если не указан, может использоваться формат по умолчанию (например, PDF).",
            defaultValue = "PDF",
            example = "PDF")
    private ReportFormat format = ReportFormat.PDF;

    public enum ReportFormat {
        PDF,
        XLSX,
        CSV
    }
}