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
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.store.dto.report.AdminReportDto;
import ru.momo.monitoring.store.dto.report.CompanySummaryReportDto;
import ru.momo.monitoring.store.dto.report.DriverActivityReportDto;
import ru.momo.monitoring.store.dto.report.DriverSummaryDto;
import ru.momo.monitoring.store.dto.report.GlobalSystemStatsDto;
import ru.momo.monitoring.store.dto.report.ReportHeaderDto;
import ru.momo.monitoring.store.dto.report.SensorReportDto;
import ru.momo.monitoring.store.dto.report.SensorStatusSummaryDto;
import ru.momo.monitoring.store.dto.report.SensorValueStatsDto;
import ru.momo.monitoring.store.dto.report.TechnicReportDto;
import ru.momo.monitoring.store.dto.report.TechnicStatsDto;

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

    private static final String REGULAR_FONT_RESOURCE_PATH = "fonts/DejaVuSans.ttf";
    private static final String BOLD_FONT_RESOURCE_PATH = "fonts/DejaVuSans-Bold.ttf";
    private static final String NOT_AVAILABLE_TEXT = "Н/Д";

    // Поля для хранения загруженных шрифтов
    private final PdfFont regularFont;
    private final PdfFont boldFont;

    public PdfReportGeneratorService() {
        log.info("PdfReportGeneratorService initializing...");
        PdfFont tempRegularFont;
        PdfFont tempBoldFont;
        try {
            tempRegularFont = PdfFontFactory.createFont(REGULAR_FONT_RESOURCE_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            tempBoldFont = PdfFontFactory.createFont(BOLD_FONT_RESOURCE_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            log.info("Custom PDF fonts ('{}', '{}') loaded successfully.", REGULAR_FONT_RESOURCE_PATH, BOLD_FONT_RESOURCE_PATH);
        } catch (Exception e) {
            log.warn("Failed to load custom fonts. Falling back to standard Helvetica. Cyrillic might not be supported well. Error: {}", e.getMessage());
            try {
                tempRegularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                tempBoldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            } catch (IOException ex) {
                log.error("Failed to load even standard Helvetica fonts. PDF generation might fail or look bad.", ex);
                // В случае критической ошибки при загрузке даже стандартных шрифтов, можно либо бросить исключение,
                // либо оставить шрифты null, но тогда методы, использующие их, должны быть готовы к этому.
                // Для простоты сейчас оставим их null, но в реальном приложении это нужно обработать надежнее.
                tempRegularFont = null;
                tempBoldFont = null;
            }
        }
        this.regularFont = tempRegularFont;
        this.boldFont = tempBoldFont;
        log.info("PdfReportGeneratorService initialized.");
    }

    public byte[] generateDriverActivityPdf(DriverActivityReportDto reportData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc, PageSize.A4)) {

            document.setMargins(36, 36, 36, 36);

            // === 0. Введение ===
            addParagraph(document, "Отчет по Активности Водителя", this.boldFont, 20, TextAlignment.CENTER);
            addParagraph(document, " ", this.regularFont, 10);
            addParagraph(document,
                    "Данный документ представляет собой сводный отчет об активности водителя, " +
                            "включая информацию о назначенной технике и статистику по показаниям сенсоров " +
                            "за указанный отчетный период.",
                    this.regularFont, 10, TextAlignment.JUSTIFIED);
            addParagraph(document, " ", this.regularFont, 12);

            // === 1. Заголовок отчета (Общая информация) ===
            ReportHeaderDto header = reportData.getHeader();
            if (header != null) {
                addParagraph(document, "1. Общая информация", this.boldFont, 16, TextAlignment.LEFT);
                addParagraph(document, " ", this.regularFont, 4);

                Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1.5f, 3f})).useAllAvailableWidth();
                headerTable.addCell(createHeaderInfoCell("ФИО Водителя:", this.boldFont));
                headerTable.addCell(createInfoCell(strVal(header.getDriverFullName()), this.regularFont));
                headerTable.addCell(createHeaderInfoCell("Email Водителя:", this.boldFont));
                headerTable.addCell(createInfoCell(strVal(header.getDriverEmail()), this.regularFont));
                headerTable.addCell(createHeaderInfoCell("Компания:", this.boldFont));
                headerTable.addCell(createInfoCell(strVal(header.getCompanyName()), this.regularFont));
                headerTable.addCell(createHeaderInfoCell("Отчетный период:", this.boldFont));
                headerTable.addCell(createInfoCell(formatDateTime(header.getPeriodFrom()) + " — " + formatDateTime(header.getPeriodTo()), this.regularFont));
                headerTable.addCell(createHeaderInfoCell("Дата формирования:", this.boldFont));
                headerTable.addCell(createInfoCell(formatDateTime(header.getReportGeneratedAt()), this.regularFont));
                headerTable.addCell(createHeaderInfoCell("Отчет сформировал:", this.boldFont));
                headerTable.addCell(createInfoCell(strVal(header.getGeneratedByManagerInfo()), this.regularFont));
                document.add(headerTable);
                addParagraph(document, " ", this.regularFont, 12);
            }

            // === 2. Сводка по водителю ===
            DriverSummaryDto driverSummary = reportData.getDriverSummary();
            if (driverSummary != null) {
                addParagraph(document, "2. Сводная информация по водителю", this.boldFont, 16, TextAlignment.LEFT);
                addParagraph(document, " ", this.regularFont, 4);
                Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
                summaryTable.addCell(createInfoCell("Общее количество единиц техники, назначенной водителю:", this.regularFont));
                summaryTable.addCell(createInfoCell(String.valueOf(driverSummary.getTotalAssignedTechnics()), this.regularFont, TextAlignment.RIGHT));
                summaryTable.addCell(createInfoCell("Из них активно в данный момент:", this.regularFont));
                summaryTable.addCell(createInfoCell(String.valueOf(driverSummary.getActiveAssignedTechnics()), this.regularFont, TextAlignment.RIGHT));
                document.add(summaryTable);
                addParagraph(document, " ", this.regularFont, 12);
            }

            // === 3. Детализация по технике ===
            if (reportData.getTechnicsDetails() != null && !reportData.getTechnicsDetails().isEmpty()) {
                addParagraph(document, "3. Детализация по назначенной технике и сенсорам", this.boldFont, 16, TextAlignment.LEFT);

                for (TechnicReportDto technic : reportData.getTechnicsDetails()) {
                    addParagraph(document, " ", this.regularFont, 8);

                    String technicTitle = String.format("%s %s (Серийный №: %s, Год выпуска: %s)",
                            strVal(technic.getBrand()), strVal(technic.getModel()),
                            strVal(technic.getSerialNumber()), technic.getYear() != null ? technic.getYear().toString() : NOT_AVAILABLE_TEXT);
                    addParagraph(document, technicTitle, this.boldFont, 14, TextAlignment.LEFT);

                    Table technicInfoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2.5f})).useAllAvailableWidth().setMarginTop(3);
                    technicInfoTable.addCell(createHeaderInfoCell("ID Техники:", this.regularFont)); // Используем regularFont для "подписей"
                    technicInfoTable.addCell(createInfoCell(uuidVal(technic.getTechnicId()), this.regularFont));
                    technicInfoTable.addCell(createHeaderInfoCell("VIN:", this.regularFont));
                    technicInfoTable.addCell(createInfoCell(strVal(technic.getVin()), this.regularFont));
                    technicInfoTable.addCell(createHeaderInfoCell("Статус:", this.regularFont));
                    technicInfoTable.addCell(createInfoCell(technic.getIsActive() != null && technic.getIsActive() ? "Активна" : "Неактивна", this.regularFont));
                    technicInfoTable.addCell(createHeaderInfoCell("Последнее ТО:", this.regularFont));
                    technicInfoTable.addCell(createInfoCell(formatDateTime(technic.getLastServiceDate()), this.regularFont));
                    technicInfoTable.addCell(createHeaderInfoCell("Следующее ТО:", this.regularFont));
                    technicInfoTable.addCell(createInfoCell(formatDateTime(technic.getNextServiceDate()), this.regularFont));
                    if (technic.getDescription() != null && !technic.getDescription().isBlank()) {
                        technicInfoTable.addCell(createHeaderInfoCell("Описание:", this.regularFont));
                        technicInfoTable.addCell(createInfoCell(strVal(technic.getDescription()), this.regularFont));
                    }
                    document.add(technicInfoTable);

                    if (technic.getSensorsSummary() != null && !technic.getSensorsSummary().isEmpty()) {
                        addParagraph(document, "Установленные сенсоры и их показатели за период:", this.boldFont, 12, TextAlignment.LEFT).setMarginTop(8);

                        Table sensorsTable = new Table(UnitValue.createPercentArray(new float[]{2.5f, 1.5f, 1.2f, 1.2f, 1.2f, 1.2f, 1f, 1f})).useAllAvailableWidth().setMarginTop(5);

                        addHeaderCellToTable(sensorsTable, "Сенсор (Тип / S/N)", this.boldFont);
                        addHeaderCellToTable(sensorsTable, "Ед. изм.", this.boldFont);
                        addHeaderCellToTable(sensorsTable, "Мин. знач.", this.boldFont);
                        addHeaderCellToTable(sensorsTable, "Макс. знач.", this.boldFont);
                        addHeaderCellToTable(sensorsTable, "Сред. знач.", this.boldFont);
                        addHeaderCellToTable(sensorsTable, "Посл. знач.", this.boldFont);
                        addHeaderCellToTable(sensorsTable, "Предупр.", this.boldFont);
                        addHeaderCellToTable(sensorsTable, "Критич.", this.boldFont);

                        for (SensorReportDto sensor : technic.getSensorsSummary()) {
                            addCellToTable(sensorsTable, strVal(sensor.getSensorType()) + "\n(SN: " + strVal(sensor.getSensorSerialNumber()) + ")", this.regularFont);
                            addCellToTable(sensorsTable, strVal(sensor.getUnitOfMeasurement()), this.regularFont, TextAlignment.CENTER);

                            SensorValueStatsDto vs = sensor.getValueStats();
                            addCellToTable(sensorsTable, vs != null && vs.getMinValue() != null ? String.format(Locale.US, "%.2f", vs.getMinValue()) : NOT_AVAILABLE_TEXT, this.regularFont, TextAlignment.CENTER);
                            addCellToTable(sensorsTable, vs != null && vs.getMaxValue() != null ? String.format(Locale.US, "%.2f", vs.getMaxValue()) : NOT_AVAILABLE_TEXT, this.regularFont, TextAlignment.CENTER);
                            addCellToTable(sensorsTable, vs != null && vs.getAvgValue() != null ? String.format(Locale.US, "%.2f", vs.getAvgValue()) : NOT_AVAILABLE_TEXT, this.regularFont, TextAlignment.CENTER);
                            addCellToTable(sensorsTable, vs != null && vs.getLastValue() != null ? String.format(Locale.US, "%.2f", vs.getLastValue()) : NOT_AVAILABLE_TEXT, this.regularFont, TextAlignment.CENTER);

                            SensorStatusSummaryDto ss = sensor.getStatusSummary();
                            addCellToTable(sensorsTable, ss != null ? String.valueOf(ss.getWarningCount()) : "0", this.regularFont, TextAlignment.CENTER);
                            addCellToTable(sensorsTable, ss != null ? String.valueOf(ss.getCriticalCount()) : "0", this.regularFont, TextAlignment.CENTER);
                        }
                        document.add(sensorsTable);
                    } else {
                        addParagraph(document, "На данной единице техники не найдено активных сенсоров с данными за период.", this.regularFont, 10).setItalic();
                    }
                    addParagraph(document, " ", this.regularFont, 10);
                }
            } else {
                addParagraph(document, "Водителю не назначена техника, или отсутствуют данные по технике за указанный период.", this.regularFont, 12, TextAlignment.CENTER).setItalic();
            }

            // === 4. Заключение/Примечания ===
            addParagraph(document, " ", this.regularFont, 20);
            addParagraph(document, "Примечания:", this.boldFont, 12, TextAlignment.LEFT);
            addParagraph(document,
                    "1. Все временные метки в отчете указаны в часовом поясе UTC.\n" +
                            "2. 'Н/Д' означает 'Нет данных' или 'Неприменимо'.\n" +
                            "3. Статистика по сенсорам (Мин./Макс./Сред./Посл. значения, Предупр./Критич. события) рассчитывается за указанный отчетный период.\n" +
                            "4. Данный отчет является автоматически сгенерированным и предназначен для информационных целей.",
                    this.regularFont, 9, TextAlignment.JUSTIFIED);

            document.close();
            log.info("PDF report generated successfully for driver: {}", reportData.getHeader() != null ? strVal(reportData.getHeader().getDriverFullName()) : "UNKNOWN_DRIVER");
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("IOException during PDF font loading or writer operations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report due to IO error", e);
        } catch (Exception e) {
            log.error("Unexpected exception during PDF report generation: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while generating the PDF report", e);
        }
    }

    public byte[] generateAdminSystemReportPdf(AdminReportDto reportData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc, PageSize.A4)) {

            document.setMargins(36, 36, 36, 36);

            // === 0. Заголовок отчета ===
            addParagraph(document, "Сводный Отчет по Системе Мониторинга", this.boldFont, 20, TextAlignment.CENTER);
            addParagraph(document, " ", this.regularFont, 10);
            addParagraph(document,
                    "Настоящий документ содержит общую статистику по работе системы мониторинга, " +
                            "а также детализированную информацию по каждой зарегистрированной компании " +
                            "за указанный отчетный период.",
                    this.regularFont, 10, TextAlignment.JUSTIFIED);
            addParagraph(document, " ", this.regularFont, 12);

            // === 1. Общая информация об отчете (из ReportHeaderDto) ===
            ReportHeaderDto header = reportData.getHeader();
            if (header != null) {
                addParagraph(document, "1. Информация об отчете", this.boldFont, 16, TextAlignment.LEFT);
                addParagraph(document, " ", this.regularFont, 4);

                Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1.5f, 3f})).useAllAvailableWidth();
                headerTable.addCell(createHeaderInfoCell("Отчетный период:", this.boldFont));
                headerTable.addCell(createInfoCell(formatDateTime(header.getPeriodFrom()) + " — " + formatDateTime(header.getPeriodTo()), this.regularFont));
                headerTable.addCell(createHeaderInfoCell("Дата формирования:", this.boldFont));
                headerTable.addCell(createInfoCell(formatDateTime(header.getReportGeneratedAt()), this.regularFont));
                headerTable.addCell(createHeaderInfoCell("Отчет сформировал:", this.boldFont));
                headerTable.addCell(createInfoCell(strVal(header.getGeneratedByManagerInfo()), this.regularFont));
                document.add(headerTable);
                addParagraph(document, " ", this.regularFont, 12);
            }

            // === 2. Глобальная статистика по системе ===
            GlobalSystemStatsDto globalStats = reportData.getGlobalStats();
            if (globalStats != null) {
                addParagraph(document, "2. Общая статистика по системе", this.boldFont, 16, TextAlignment.LEFT);
                addParagraph(document, " ", this.regularFont, 4);
                Table statsTable = new Table(UnitValue.createPercentArray(new float[]{3, 2})).useAllAvailableWidth();
                statsTable.addCell(createInfoCell("Всего компаний в системе:", this.regularFont));
                statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalCompanies()), this.regularFont, TextAlignment.RIGHT));
                statsTable.addCell(createInfoCell("Всего единиц техники:", this.regularFont));
                statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalTechnics()), this.regularFont, TextAlignment.RIGHT));
                statsTable.addCell(createInfoCell("Из них активно:", this.regularFont));
                statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalActiveTechnics()), this.regularFont, TextAlignment.RIGHT));
                statsTable.addCell(createInfoCell("Всего сенсоров:", this.regularFont));
                statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalSensors()), this.regularFont, TextAlignment.RIGHT));
                statsTable.addCell(createInfoCell("Из них активно:", this.regularFont));
                statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalActiveSensors()), this.regularFont, TextAlignment.RIGHT));
                statsTable.addCell(createInfoCell("Среднее кол-во нарушений в час (по системе):", this.regularFont));
                statsTable.addCell(createInfoCell(String.format(Locale.US, "%.2f", globalStats.getAverageViolationsPerHour()), this.regularFont, TextAlignment.RIGHT));
                statsTable.addCell(createInfoCell("Общее время работы техники:", this.regularFont));
                statsTable.addCell(createInfoCell(strVal(globalStats.getTotalSystemUptimeFormatted()), this.regularFont, TextAlignment.RIGHT));
                statsTable.addCell(createInfoCell("Общее время простоя техники:", this.regularFont));
                statsTable.addCell(createInfoCell(strVal(globalStats.getTotalSystemDowntimeFormatted()), this.regularFont, TextAlignment.RIGHT));
                document.add(statsTable);
                addParagraph(document, " ", this.regularFont, 12);
            }

            // === 3. Детализация по компаниям ===
            if (reportData.getCompaniesSummary() != null && !reportData.getCompaniesSummary().isEmpty()) {
                addParagraph(document, "3. Статистика по компаниям", this.boldFont, 16, TextAlignment.LEFT);

                for (CompanySummaryReportDto companySummary : reportData.getCompaniesSummary()) {
                    addParagraph(document, " ", this.regularFont, 8);
                    String companyTitle = String.format("Компания: %s (ИНН: %s)",
                            strVal(companySummary.getCompanyName()), strVal(companySummary.getCompanyInn()));
                    addParagraph(document, companyTitle, this.boldFont, 14, TextAlignment.LEFT);
                    addParagraph(document, " ", this.regularFont, 2);

                    Table companyInfoTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f})).useAllAvailableWidth().setMarginTop(3);
                    companyInfoTable.addCell(createInfoCell("Всего техники в компании:", this.regularFont));
                    companyInfoTable.addCell(createInfoCell(String.valueOf(companySummary.getTotalTechnicsInCompany()), this.regularFont, TextAlignment.RIGHT));
                    companyInfoTable.addCell(createInfoCell("Активной техники:", this.regularFont));
                    companyInfoTable.addCell(createInfoCell(String.valueOf(companySummary.getActiveTechnicsInCompany()), this.regularFont, TextAlignment.RIGHT));
                    companyInfoTable.addCell(createInfoCell("Всего сенсоров в компании:", this.regularFont));
                    companyInfoTable.addCell(createInfoCell(String.valueOf(companySummary.getTotalSensorsInCompany()), this.regularFont, TextAlignment.RIGHT));
                    companyInfoTable.addCell(createInfoCell("Активных сенсоров:", this.regularFont));
                    companyInfoTable.addCell(createInfoCell(String.valueOf(companySummary.getActiveSensorsInCompany()), this.regularFont, TextAlignment.RIGHT));
                    companyInfoTable.addCell(createInfoCell("Среднее кол-во нарушений в час (по компании):", this.regularFont));
                    companyInfoTable.addCell(createInfoCell(String.format(Locale.US, "%.2f", companySummary.getCompanyViolationsPerHour()), this.regularFont, TextAlignment.RIGHT));
                    document.add(companyInfoTable);

                    if (companySummary.getTechnicsStats() != null && !companySummary.getTechnicsStats().isEmpty()) {
                        addParagraph(document, "Сводка по технике компании:", this.boldFont, 12, TextAlignment.LEFT).setMarginTop(6);

                        Table technicsStatsTable = new Table(UnitValue.createPercentArray(new float[]{3f, 2f, 1f, 1f, 1f})).useAllAvailableWidth().setMarginTop(5);
                        addHeaderCellToTable(technicsStatsTable, "Техника (Бренд Модель / S/N)", this.boldFont);
                        addHeaderCellToTable(technicsStatsTable, "Статус", this.boldFont);
                        addHeaderCellToTable(technicsStatsTable, "Сенсоров", this.boldFont);
                        addHeaderCellToTable(technicsStatsTable, "Предупр.", this.boldFont);
                        addHeaderCellToTable(technicsStatsTable, "Критич.", this.boldFont);

                        for (TechnicStatsDto technicStat : companySummary.getTechnicsStats()) {
                            addCellToTable(technicsStatsTable, strVal(technicStat.getTechnicBrandModel()) + "\n(SN: " + strVal(technicStat.getTechnicSerialNumber()) + ")", this.regularFont);
                            addCellToTable(technicsStatsTable, technicStat.isActive() ? "Активна" : "Неактивна", this.regularFont, TextAlignment.CENTER);
                            addCellToTable(technicsStatsTable, String.valueOf(technicStat.getNumberOfSensors()), this.regularFont, TextAlignment.CENTER);
                            addCellToTable(technicsStatsTable, String.valueOf(technicStat.getWarningAlerts()), this.regularFont, TextAlignment.CENTER);
                            addCellToTable(technicsStatsTable, String.valueOf(technicStat.getCriticalAlerts()), this.regularFont, TextAlignment.CENTER);
                        }
                        document.add(technicsStatsTable);
                    } else {
                        addParagraph(document, "Нет данных по технике для этой компании.", this.regularFont, 10).setItalic();
                    }
                    addParagraph(document, " ", this.regularFont, 10);
                }
            } else {
                addParagraph(document, "Нет данных по компаниям для отображения.", this.regularFont, 12, TextAlignment.CENTER).setItalic();
            }

            // === 4. Заключение (если нужно) ===
            addParagraph(document, " ", this.regularFont, 20);
            addParagraph(document, "Конец отчета.", this.regularFont, 10, TextAlignment.CENTER).setItalic();

            document.close();
            log.info("Admin system PDF report generated successfully at {}", LocalDateTime.now());
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("IOException during PDF generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report due to IO error", e);
        } catch (Exception e) {
            log.error("Unexpected exception during PDF report generation: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while generating the PDF report", e);
        }
    }

    // --- Вспомогательные методы ---

    private Paragraph addParagraph(Document document, String text, PdfFont font, float fontSize, TextAlignment alignment) throws IOException {
        // Проверка на null для font перед использованием this.regularFont как fallback
        PdfFont effectiveFont = font != null ? font : (this.regularFont != null ? this.regularFont : PdfFontFactory.createFont(StandardFonts.HELVETICA));
        Paragraph p = new Paragraph(text)
                .setFont(effectiveFont)
                .setFontSize(fontSize)
                .setTextAlignment(alignment);
        document.add(p);
        return p;
    }

    private Paragraph addParagraph(Document document, String text, PdfFont font, float fontSize) throws IOException {
        return addParagraph(document, text, font, fontSize, TextAlignment.LEFT);
    }

    private Cell createHeaderInfoCell(String text, PdfFont fontToUse) throws IOException {
        PdfFont effectiveFont = fontToUse != null ? fontToUse : (this.boldFont != null ? this.boldFont : PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        return new Cell().add(new Paragraph(text).setFont(effectiveFont).setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingRight(5);
    }

    private Cell createInfoCell(String text, PdfFont fontToUse) throws IOException {
        return createInfoCell(text, fontToUse, TextAlignment.LEFT);
    }

    private Cell createInfoCell(String text, PdfFont fontToUse, TextAlignment alignment) throws IOException {
        PdfFont effectiveFont = fontToUse != null ? fontToUse : (this.regularFont != null ? this.regularFont : PdfFontFactory.createFont(StandardFonts.HELVETICA));
        return new Cell().add(new Paragraph(text).setFont(effectiveFont).setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingLeft(5);
    }

    private void addCellToTable(Table table, String text, PdfFont fontToUse) throws IOException {
        addCellToTable(table, text, fontToUse, TextAlignment.LEFT);
    }

    private void addCellToTable(Table table, String text, PdfFont fontToUse, TextAlignment alignment) throws IOException {
        PdfFont effectiveFont = fontToUse != null ? fontToUse : (this.regularFont != null ? this.regularFont : PdfFontFactory.createFont(StandardFonts.HELVETICA));
        Cell cell = new Cell().add(new Paragraph(text).setFont(effectiveFont).setFontSize(8))
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addCell(cell);
    }

    private void addHeaderCellToTable(Table table, String text, PdfFont fontToUse) throws IOException {
        PdfFont effectiveFont = fontToUse != null ? fontToUse : (this.boldFont != null ? this.boldFont : PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        Cell cell = new Cell().add(new Paragraph(text).setFont(effectiveFont).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBackgroundColor(new DeviceGray(0.9f))
                .setPadding(4);
        table.addCell(cell);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : NOT_AVAILABLE_TEXT;
    }

    private String formatDate(LocalDate date) { // На случай, если понадобится
        return date != null ? date.format(DATE_FORMATTER) : NOT_AVAILABLE_TEXT;
    }

    private String strVal(Object obj) {
        if (obj == null) return NOT_AVAILABLE_TEXT;
        String s = obj.toString();
        return s.isBlank() ? NOT_AVAILABLE_TEXT : s;
    }

    private String uuidVal(UUID obj) {
        return obj != null ? obj.toString() : NOT_AVAILABLE_TEXT;
    }

}