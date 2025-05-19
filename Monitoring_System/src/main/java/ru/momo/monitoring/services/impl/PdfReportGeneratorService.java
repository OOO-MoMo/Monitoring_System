package ru.momo.monitoring.services.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.store.dto.report.DriverActivityReportDto;
import ru.momo.monitoring.store.dto.report.DriverSummaryDto;
import ru.momo.monitoring.store.dto.report.ReportHeaderDto;
import ru.momo.monitoring.store.dto.report.SensorReportDto;
import ru.momo.monitoring.store.dto.report.SensorStatusSummaryDto;
import ru.momo.monitoring.store.dto.report.SensorValueStatsDto;
import ru.momo.monitoring.store.dto.report.TechnicReportDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class PdfReportGeneratorService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String REGULAR_FONT_PATH = "fonts/DejaVuSans.ttf";
    private static final String BOLD_FONT_PATH = "fonts/DejaVuSans-Bold.ttf";
    private static final String NOT_AVAILABLE = "N/A"; // Константа для "нет данных"

    public PdfReportGeneratorService() {
        log.info("PdfReportGeneratorService initialized.");
    }

    public byte[] generateDriverActivityPdf(DriverActivityReportDto reportData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfFont localRegularFont;
        PdfFont localBoldFont;

        try {
            try {
                localRegularFont = PdfFontFactory.createFont(REGULAR_FONT_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                localBoldFont = PdfFontFactory.createFont(BOLD_FONT_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                log.debug("Custom PDF fonts loaded for this report generation.");
            } catch (Exception e) {
                log.warn("Failed to load custom fonts ('{}', '{}'): {}. Falling back to standard Helvetica. Cyrillic might not be supported.",
                        REGULAR_FONT_PATH, BOLD_FONT_PATH, e.getMessage());
                localRegularFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
                localBoldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
            }

            try (PdfWriter writer = new PdfWriter(baos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc, PageSize.A4)) {

                document.setMargins(36, 36, 36, 36);

                // === 1. Заголовок отчета ===
                ReportHeaderDto header = reportData.getHeader();
                if (header != null) {
                    addParagraph(document, "Отчет по активности водителя", localBoldFont, 18, TextAlignment.CENTER);
                    addParagraph(document, " ", localRegularFont, 6);

                    Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
                    addCellToTable(headerTable, "ФИО Водителя:", localBoldFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, strVal(header.getDriverFullName()), localRegularFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, "Email Водителя:", localBoldFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, strVal(header.getDriverEmail()), localRegularFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, "Компания:", localBoldFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, strVal(header.getCompanyName()), localRegularFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, "Отчетный период:", localBoldFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, formatDateTime(header.getPeriodFrom()) + " - " + formatDateTime(header.getPeriodTo()), localRegularFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, "Дата формирования:", localBoldFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, formatDateTime(header.getReportGeneratedAt()), localRegularFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, "Сформировал:", localBoldFont, TextAlignment.LEFT, false);
                    addCellToTable(headerTable, strVal(header.getGeneratedByManagerInfo()), localRegularFont, TextAlignment.LEFT, false);
                    document.add(headerTable);
                    addParagraph(document, " ", localRegularFont, 12);
                }

                // === 2. Сводка по водителю ===
                DriverSummaryDto driverSummary = reportData.getDriverSummary();
                if (driverSummary != null) {
                    addParagraph(document, "Сводная информация по водителю:", localBoldFont, 14);
                    Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth(); // Изменил пропорции
                    addCellToTable(summaryTable, "Общее количество назначенной техники:", localRegularFont, TextAlignment.LEFT, false);
                    addCellToTable(summaryTable, String.valueOf(driverSummary.getTotalAssignedTechnics()), localRegularFont, TextAlignment.RIGHT, false); // Выравнивание по правому краю
                    addCellToTable(summaryTable, "Количество активной назначенной техники:", localRegularFont, TextAlignment.LEFT, false);
                    addCellToTable(summaryTable, String.valueOf(driverSummary.getActiveAssignedTechnics()), localRegularFont, TextAlignment.RIGHT, false);
                    document.add(summaryTable);
                    addParagraph(document, " ", localRegularFont, 12);
                }

                // === 3. Детализация по технике ===
                if (reportData.getTechnicsDetails() != null && !reportData.getTechnicsDetails().isEmpty()) {
                    addParagraph(document, "Детализация по технике:", localBoldFont, 16);
                    for (TechnicReportDto technic : reportData.getTechnicsDetails()) {
                        addParagraph(document, " ", localRegularFont, 6);
                        String technicTitle = String.format("Техника: %s %s (SN: %s, Год: %s)",
                                strVal(technic.getBrand()), strVal(technic.getModel()),
                                strVal(technic.getSerialNumber()), technic.getYear() != null ? technic.getYear().toString() : NOT_AVAILABLE);
                        addParagraph(document, technicTitle, localBoldFont, 14);

                        Table technicInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth().setMarginTop(3);
                        addCellToTable(technicInfoTable, "ID Техники:", localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, uuidVal(technic.getTechnicId()), localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, "VIN:", localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, strVal(technic.getVin()), localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, "Статус активности:", localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, technic.getIsActive() != null && technic.getIsActive() ? "Активна" : "Неактивна", localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, "Последнее ТО:", localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, formatDateTime(technic.getLastServiceDate()), localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, "Следующее ТО:", localRegularFont, TextAlignment.LEFT, false);
                        addCellToTable(technicInfoTable, formatDateTime(technic.getNextServiceDate()), localRegularFont, TextAlignment.LEFT, false);
                        document.add(technicInfoTable);

                        if (technic.getSensorsSummary() != null && !technic.getSensorsSummary().isEmpty()) {
                            addParagraph(document, "Сенсоры на данной технике:", localBoldFont, 12);
                            Table sensorsTable = new Table(UnitValue.createPercentArray(new float[]{2.5f, 1.5f, 1, 1, 1, 1, 1.5f, 1.5f})).useAllAvailableWidth().setMarginTop(5);
                            addHeaderCellToTable(sensorsTable, "Тип (SN)", localBoldFont);
                            addHeaderCellToTable(sensorsTable, "Ед. изм.", localBoldFont);
                            addHeaderCellToTable(sensorsTable, "Мин.", localBoldFont);
                            addHeaderCellToTable(sensorsTable, "Макс.", localBoldFont);
                            addHeaderCellToTable(sensorsTable, "Сред.", localBoldFont);
                            addHeaderCellToTable(sensorsTable, "Посл.", localBoldFont);
                            addHeaderCellToTable(sensorsTable, "Предупр.", localBoldFont);
                            addHeaderCellToTable(sensorsTable, "Критич.", localBoldFont);

                            for (SensorReportDto sensor : technic.getSensorsSummary()) {
                                addCellToTable(sensorsTable, strVal(sensor.getSensorType()) + "\n(SN: " + strVal(sensor.getSensorSerialNumber()) + ")", localRegularFont, TextAlignment.LEFT, true);
                                addCellToTable(sensorsTable, strVal(sensor.getUnitOfMeasurement()), localRegularFont, TextAlignment.CENTER, true);

                                SensorValueStatsDto vs = sensor.getValueStats();
                                addCellToTable(sensorsTable, vs != null && vs.getMinValue() != null ? String.format(Locale.US, "%.2f", vs.getMinValue()) : NOT_AVAILABLE, localRegularFont, TextAlignment.CENTER, true);
                                addCellToTable(sensorsTable, vs != null && vs.getMaxValue() != null ? String.format(Locale.US, "%.2f", vs.getMaxValue()) : NOT_AVAILABLE, localRegularFont, TextAlignment.CENTER, true);
                                addCellToTable(sensorsTable, vs != null && vs.getAvgValue() != null ? String.format(Locale.US, "%.2f", vs.getAvgValue()) : NOT_AVAILABLE, localRegularFont, TextAlignment.CENTER, true);
                                addCellToTable(sensorsTable, vs != null && vs.getLastValue() != null ? String.format(Locale.US, "%.2f", vs.getLastValue()) : NOT_AVAILABLE, localRegularFont, TextAlignment.CENTER, true);

                                SensorStatusSummaryDto ss = sensor.getStatusSummary();
                                addCellToTable(sensorsTable, ss != null ? String.valueOf(ss.getWarningCount()) : "0", localRegularFont, TextAlignment.CENTER, true);
                                addCellToTable(sensorsTable, ss != null ? String.valueOf(ss.getCriticalCount()) : "0", localRegularFont, TextAlignment.CENTER, true);
                            }
                            document.add(sensorsTable);
                        } else {
                            addParagraph(document, "Сенсоры для данной техники не назначены или неактивны.", localRegularFont, 10);
                        }
                        addParagraph(document, " ", localRegularFont, 10);
                    }
                } else {
                    addParagraph(document, "Нет данных по технике водителя для отображения.", localRegularFont, 12);
                }

                document.close();
                log.info("PDF report generated successfully for driver: {}", reportData.getHeader() != null ? strVal(reportData.getHeader().getDriverFullName()) : "UNKNOWN_DRIVER");
                return baos.toByteArray();
            }
        } catch (IOException e) {
            log.error("IOException during PDF font loading or writer operations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report due to IO error", e);
        } catch (Exception e) {
            log.error("Unexpected exception during PDF report generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    // Вспомогательные методы

    private void addParagraph(Document document, String text, PdfFont font, float fontSize, TextAlignment alignment) {
        Paragraph p = new Paragraph(strVal(text))
                .setFont(font != null ? font : getDefaultFont())
                .setFontSize(fontSize)
                .setTextAlignment(alignment);
        document.add(p);
    }

    private void addParagraph(Document document, String text, PdfFont font, float fontSize) {
        addParagraph(document, text, font, fontSize, TextAlignment.LEFT);
    }

    private void addCellToTable(Table table, String text, PdfFont font, TextAlignment alignment, boolean border) {
        Cell cell = new Cell().add(new Paragraph(strVal(text)).setFont(font != null ? font : getDefaultFont()).setFontSize(8))
                .setTextAlignment(alignment)
                .setPadding(3);
        if (!border) {
            cell.setBorder(Border.NO_BORDER);
        }
        table.addCell(cell);
    }

    private void addHeaderCellToTable(Table table, String text, PdfFont font) {
        Cell cell = new Cell().add(new Paragraph(strVal(text)).setFont(font != null ? font : getDefaultBoldFont()).setFontSize(9).setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(DeviceGray.GRAY)
                .setPadding(3);
        table.addCell(cell);
    }

    private PdfFont getDefaultFont() {
        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA);
        } catch (IOException e) {
            return null;
        }
    }

    private PdfFont getDefaultBoldFont() {
        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            return null;
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : NOT_AVAILABLE;
    }

    private String formatDate(LocalDate date) { // На случай, если понадобится
        return date != null ? date.format(DATE_FORMATTER) : NOT_AVAILABLE;
    }

    // Хелпер для безопасного получения строки из объекта, возвращает NOT_AVAILABLE если null
    private String strVal(Object obj) {
        return obj != null ? obj.toString() : NOT_AVAILABLE;
    }

    private String uuidVal(UUID obj) {
        return obj != null ? obj.toString() : NOT_AVAILABLE;
    }
}