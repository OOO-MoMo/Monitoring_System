package ru.momo.monitoring.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.monitoring.annotations.CheckUserActive;
import ru.momo.monitoring.exceptions.ExceptionBody;
import ru.momo.monitoring.services.ReportService;
import ru.momo.monitoring.services.SecurityService;
import ru.momo.monitoring.services.impl.PdfReportGeneratorService;
import ru.momo.monitoring.store.dto.report.AdminReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportRequest;
import ru.momo.monitoring.store.entities.User;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Отчеты", description = "API для генерации отчетов")
public class ReportController {

    private final ReportService reportService;
    private final PdfReportGeneratorService pdfReportGeneratorService;
    private final SecurityService securityService;

    @PostMapping("/driver-activity")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Сгенерировать отчет по активности водителя (PDF)",
            description = "Формирует отчет по указанному водителю за заданный период в формате PDF и предлагает его для скачивания."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", // OK
                    description = "Отчет успешно сгенерирован и возвращен в теле ответа.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary", description = "Бинарное содержимое PDF файла.")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса (ошибка валидации DTO, неверный диапазон дат и т.д.).",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (например, менеджер пытается получить отчет по водителю не из своей компании).",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "404", description = "Водитель с указанным ID не найден.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера при генерации отчета.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
    })
    public ResponseEntity<byte[]> generateDriverActivityReport(
            @Parameter(hidden = true) Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Параметры для генерации отчета: ID водителя, период (from, to), формат (опционально).",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DriverActivityReportRequest.class))
            )
            @Valid @RequestBody DriverActivityReportRequest request
    ) {
        DriverActivityReportDto reportData = reportService.generateDriverActivityReportData(request, principal);

        if (reportData == null || reportData.getHeader() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to gather report data.".getBytes());
        }

        byte[] pdfBytes = pdfReportGeneratorService.generateDriverActivityPdf(reportData);

        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate PDF content.".getBytes());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String driverNamePart = "UnknownDriver";
        if (reportData.getHeader().getDriverFullName() != null && !reportData.getHeader().getDriverFullName().isBlank() && !reportData.getHeader().getDriverFullName().equalsIgnoreCase("N/A")) {
            driverNamePart = reportData.getHeader().getDriverFullName().replace(" ", "_").replaceAll("[^a-zA-Z0-9_.-]", "");
        }

        String dateFromStr = request.getDateFrom().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dateToStr = request.getDateTo().format(DateTimeFormatter.ISO_LOCAL_DATE);

        String filename = String.format("Driver_Activity_Report_%s_%s_to_%s.pdf",
                driverNamePart,
                dateFromStr,
                dateToStr);

        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");

        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/admin/system-summary")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Только для администраторов
    @CheckUserActive // Проверяем, что сам администратор активен
    @Operation(
            summary = "Сформировать сводный PDF отчет по системе для администратора",
            description = "Генерирует PDF-файл со сводной статистикой по всем компаниям, технике и сенсорам в системе за указанный период. Доступно только администраторам.",
            security = @SecurityRequirement(name = "bearerAuth") // Если используешь JWT Bearer аутентификацию в Swagger
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF отчет успешно сформирован и возвращен.",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса (например, неверный формат дат, 'from' после 'to').",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (пользователь не администратор или не активен).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера при генерации отчета.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExceptionBody.class)))
    })
    public ResponseEntity<byte[]> generateAdminSystemReport(
            @Parameter(description = "Начало периода для отчета (ISO 8601 UTC, например, 2024-05-01T00:00:00).",
                    required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "Конец периода для отчета (ISO 8601 UTC, например, 2024-05-31T23:59:59).",
                    required = true, example = "2024-03-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        User adminUser = securityService.getCurrentUser();

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be after 'to' date.");
        }

        AdminReportDto reportData = reportService.prepareAdminReportData(from, to, adminUser);

        byte[] pdfBytes = null;
        pdfBytes = pdfReportGeneratorService.generateAdminSystemReportPdf(reportData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = String.format("AdminSystemReport_%s_to_%s.pdf",
                from.format(DateTimeFormatter.ISO_LOCAL_DATE),
                to.format(DateTimeFormatter.ISO_LOCAL_DATE));
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}