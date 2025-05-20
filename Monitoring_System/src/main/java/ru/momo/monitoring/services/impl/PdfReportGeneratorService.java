package ru.momo.monitoring.services.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
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
import ru.momo.monitoring.store.dto.report.ReportHeaderForAdminDto;
import ru.momo.monitoring.store.dto.report.ReportHeaderForManagerDto;
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

    public PdfReportGeneratorService() {
        log.info("PdfReportGeneratorService initialized.");
    }

    public byte[] generateDriverActivityPdf(DriverActivityReportDto reportData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfFont currentRegularFont;
        PdfFont currentBoldFont;

        try {
            try {
                currentRegularFont = PdfFontFactory.createFont(REGULAR_FONT_RESOURCE_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                currentBoldFont = PdfFontFactory.createFont(BOLD_FONT_RESOURCE_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                log.debug("Custom PDF fonts loaded for DriverActivityReport.");
            } catch (Exception e) {
                log.warn("Failed to load custom fonts for DriverActivityReport (paths: '{}', '{}'). Falling back to Helvetica. Error: {}",
                        REGULAR_FONT_RESOURCE_PATH, BOLD_FONT_RESOURCE_PATH, e.getMessage());
                currentRegularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                currentBoldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            }

            try (PdfWriter writer = new PdfWriter(baos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc, PageSize.A4)) {

                document.setMargins(36, 36, 36, 36);

                // === 0. Заголовок и Введение ===
                Paragraph mainTitle = new Paragraph("Отчет по Активности Водителя")
                        .setFont(currentBoldFont).setFontSize(20).setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10);
                document.add(mainTitle);

                Paragraph introText = new Paragraph(
                        "Данный документ представляет собой сводный отчет об активности водителя, " +
                                "включая информацию о назначенной ему технике и статистику по показаниям сенсоров " +
                                "за указанный отчетный период. Отчет сформирован для анализа и контроля работы водителя.")
                        .setFont(currentRegularFont).setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED)
                        .setMarginBottom(18);
                document.add(introText);


                // === 1. Общая информация об отчете и водителе ===
                ReportHeaderForManagerDto header = reportData.getHeader(); // Используем ReportHeaderForManagerDto
                if (header != null) {
                    addSectionTitle(document, "1. Общая информация", currentBoldFont);

                    Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1.8f, 3f})).useAllAvailableWidth().setMarginBottom(12);
                    headerTable.addCell(createHeaderInfoCell("ФИО Водителя:", currentBoldFont));
                    headerTable.addCell(createInfoCell(strVal(header.getDriverFullName()), currentRegularFont));
                    headerTable.addCell(createHeaderInfoCell("Email Водителя:", currentBoldFont));
                    headerTable.addCell(createInfoCell(strVal(header.getDriverEmail()), currentRegularFont));
                    headerTable.addCell(createHeaderInfoCell("Компания:", currentBoldFont));
                    headerTable.addCell(createInfoCell(strVal(header.getCompanyName()), currentRegularFont));
                    headerTable.addCell(createHeaderInfoCell("Отчетный период:", currentBoldFont));
                    headerTable.addCell(createInfoCell(formatDateTime(header.getPeriodFrom()) + " — " + formatDateTime(header.getPeriodTo()), currentRegularFont));
                    headerTable.addCell(createHeaderInfoCell("Дата формирования:", currentBoldFont));
                    headerTable.addCell(createInfoCell(formatDateTime(header.getReportGeneratedAt()), currentRegularFont));
                    headerTable.addCell(createHeaderInfoCell("Отчет сформировал:", currentBoldFont));
                    headerTable.addCell(createInfoCell(strVal(header.getGeneratedByManagerInfo()), currentRegularFont));
                    document.add(headerTable);
                }

                // === 2. Сводка по назначенной технике ===
                DriverSummaryDto driverSummary = reportData.getDriverSummary();
                if (driverSummary != null) {
                    addSectionTitle(document, "2. Сводка по назначенной технике", currentBoldFont);
                    addParagraph(document, "Общее количество техники, закрепленной за водителем, и ее текущий статус активности.", currentRegularFont, 9, TextAlignment.LEFT).setItalic().setMarginBottom(5);

                    Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f})).useAllAvailableWidth().setMarginBottom(12);
                    summaryTable.addCell(createInfoCell("Общее количество назначенных единиц техники:", currentRegularFont));
                    summaryTable.addCell(createInfoCell(String.valueOf(driverSummary.getTotalAssignedTechnics()), currentRegularFont, TextAlignment.RIGHT));
                    summaryTable.addCell(createInfoCell("Из них активно в системе на данный момент:", currentRegularFont));
                    summaryTable.addCell(createInfoCell(String.valueOf(driverSummary.getActiveAssignedTechnics()), currentRegularFont, TextAlignment.RIGHT));
                    document.add(summaryTable);
                }

                // === 3. Детализация по технике и сенсорам ===
                if (reportData.getTechnicsDetails() != null && !reportData.getTechnicsDetails().isEmpty()) {
                    addSectionTitle(document, "3. Детализация по технике и сенсорам", currentBoldFont);

                    for (TechnicReportDto technic : reportData.getTechnicsDetails()) {
                        addParagraph(document, " ", currentRegularFont, 6);
                        String technicTitle = String.format("Техника: %s %s (Серийный №: %s, VIN: %s, Год: %s)",
                                strVal(technic.getBrand()), strVal(technic.getModel()),
                                strVal(technic.getSerialNumber()), strVal(technic.getVin()),
                                technic.getYear() != null ? technic.getYear().toString() : NOT_AVAILABLE_TEXT);
                        Paragraph technicParagraph = new Paragraph(technicTitle)
                                .setFont(currentBoldFont).setFontSize(14).setMarginBottom(2);
                        document.add(technicParagraph);

                        Table technicInfoTable = new Table(UnitValue.createPercentArray(new float[]{1.2f, 2.3f})).useAllAvailableWidth().setMarginTop(4).setMarginBottom(6);
                        technicInfoTable.addCell(createHeaderInfoCell("ID Техники:", currentRegularFont));
                        technicInfoTable.addCell(createInfoCell(uuidVal(technic.getTechnicId()), currentRegularFont));
                        technicInfoTable.addCell(createHeaderInfoCell("Статус в системе:", currentRegularFont));
                        technicInfoTable.addCell(createInfoCell(technic.getIsActive() != null && technic.getIsActive() ? "Активна" : "Неактивна", currentRegularFont));
                        technicInfoTable.addCell(createHeaderInfoCell("Последнее ТО:", currentRegularFont));
                        technicInfoTable.addCell(createInfoCell(formatDateTime(technic.getLastServiceDate()), currentRegularFont));
                        technicInfoTable.addCell(createHeaderInfoCell("Следующее ТО:", currentRegularFont));
                        technicInfoTable.addCell(createInfoCell(formatDateTime(technic.getNextServiceDate()), currentRegularFont));
                        if (technic.getDescription() != null && !technic.getDescription().isBlank()) {
                            technicInfoTable.addCell(createHeaderInfoCell("Описание:", currentRegularFont));
                            technicInfoTable.addCell(createInfoCell(strVal(technic.getDescription()), currentRegularFont));
                        }
                        document.add(technicInfoTable);

                        if (technic.getSensorsSummary() != null && !technic.getSensorsSummary().isEmpty()) {
                            Paragraph sensorsSubtitle = new Paragraph("Показатели сенсоров на данной технике за период:")
                                    .setFont(currentBoldFont).setFontSize(11).setMarginTop(8).setMarginBottom(2);
                            document.add(sensorsSubtitle);

                            Table sensorsTable = new Table(UnitValue.createPercentArray(new float[]{2.5f, 1.3f, 1f, 1f, 1f, 1f, 1f, 1f})).useAllAvailableWidth().setMarginTop(5);

                            addHeaderCellToTable(sensorsTable, "Сенсор (Тип / S/N)", currentBoldFont);
                            addHeaderCellToTable(sensorsTable, "Ед. изм.", currentBoldFont);
                            addHeaderCellToTable(sensorsTable, "Мин.", currentBoldFont);
                            addHeaderCellToTable(sensorsTable, "Макс.", currentBoldFont);
                            addHeaderCellToTable(sensorsTable, "Сред.", currentBoldFont);
                            addHeaderCellToTable(sensorsTable, "Посл.", currentBoldFont);
                            addHeaderCellToTable(sensorsTable, "Предупр.", currentBoldFont);
                            addHeaderCellToTable(sensorsTable, "Критич.", currentBoldFont);

                            for (SensorReportDto sensor : technic.getSensorsSummary()) {
                                addCellToTable(sensorsTable, strVal(sensor.getSensorType()) + "\n(SN: " + strVal(sensor.getSensorSerialNumber()) + ")", currentRegularFont);
                                addCellToTable(sensorsTable, strVal(sensor.getUnitOfMeasurement()), currentRegularFont, TextAlignment.CENTER);

                                SensorValueStatsDto vs = sensor.getValueStats();
                                addCellToTable(sensorsTable, vs != null && vs.getMinValue() != null ? String.format(Locale.US, "%.2f", vs.getMinValue()) : NOT_AVAILABLE_TEXT, currentRegularFont, TextAlignment.CENTER);
                                addCellToTable(sensorsTable, vs != null && vs.getMaxValue() != null ? String.format(Locale.US, "%.2f", vs.getMaxValue()) : NOT_AVAILABLE_TEXT, currentRegularFont, TextAlignment.CENTER);
                                addCellToTable(sensorsTable, vs != null && vs.getAvgValue() != null ? String.format(Locale.US, "%.2f", vs.getAvgValue()) : NOT_AVAILABLE_TEXT, currentRegularFont, TextAlignment.CENTER);
                                addCellToTable(sensorsTable, vs != null && vs.getLastValue() != null ? String.format(Locale.US, "%.2f", vs.getLastValue()) : NOT_AVAILABLE_TEXT, currentRegularFont, TextAlignment.CENTER);

                                SensorStatusSummaryDto ss = sensor.getStatusSummary();
                                addCellToTable(sensorsTable, ss != null ? String.valueOf(ss.getWarningCount()) : "0", currentRegularFont, TextAlignment.CENTER);
                                addCellToTable(sensorsTable, ss != null ? String.valueOf(ss.getCriticalCount()) : "0", currentRegularFont, TextAlignment.CENTER);
                            }
                            document.add(sensorsTable);
                        } else {
                            addParagraph(document, "На данной единице техники не найдено активных сенсоров с данными за указанный период.", currentRegularFont, 10).setItalic().setMarginTop(4);
                        }
                        addParagraph(document, " ", currentRegularFont, 8); // Отступ после каждой единицы техники
                    }
                } else {
                    addParagraph(document, "За водителем не закреплена техника, либо отсутствуют данные по технике за указанный период.", currentRegularFont, 12, TextAlignment.CENTER).setItalic().setMarginTop(10);
                }

                // === 4. Заключение/Примечания ===
                addParagraph(document, " ", currentRegularFont, 24);
                addSectionTitle(document, "4. Дополнительная информация", currentBoldFont);
                Paragraph notes = new Paragraph()
                        .setFont(currentRegularFont).setFontSize(9).setTextAlignment(TextAlignment.JUSTIFIED)
                        .add(new Text("• Все временные метки в отчете указаны в часовом поясе UTC (Coordinated Universal Time).\n").setBold())
                        .add("• 'Н/Д' (Нет Данных) используется в случаях, когда информация отсутствует или неприменима.\n")
                        .add("• Статистика по сенсорам (Мин./Макс./Сред./Посл. значения, Предупр./Критич. события) рассчитывается за указанный отчетный период.\n")
                        .add("• 'Предупр.' и 'Критич.' означают количество зафиксированных событий со статусами WARNING и CRITICAL соответственно.\n")
                        .add("• Данный отчет является автоматически сгенерированным и предназначен для информационных целей.");
                document.add(notes);

                addParagraph(document, " ", currentRegularFont, 12);
                addParagraph(document, "Конец отчета", currentRegularFont, 10, TextAlignment.CENTER).setItalic();


                document.close();
                log.info("PDF report generated successfully for driver: {}", reportData.getHeader() != null ? strVal(reportData.getHeader().getDriverFullName()) : "UNKNOWN_DRIVER");
                return baos.toByteArray();
            }
        } catch (IOException e) {
            log.error("IOException (font loading or PDF writing) in generateDriverActivityPdf: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report due to IO error", e);
        } catch (Exception e) {
            log.error("Unexpected exception in generateDriverActivityPdf: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while generating the PDF report", e);
        }
    }


    // Код метода generateAdminSystemReportPdf остается таким же, как в предыдущем ответе
    public byte[] generateAdminSystemReportPdf(AdminReportDto reportData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfFont currentRegularFont;
        PdfFont currentBoldFont;

        try {
            try {
                currentRegularFont = PdfFontFactory.createFont(REGULAR_FONT_RESOURCE_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                currentBoldFont = PdfFontFactory.createFont(BOLD_FONT_RESOURCE_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                log.debug("Custom PDF fonts loaded for AdminSystemReport.");
            } catch (Exception e) {
                log.warn("Failed to load custom fonts for AdminSystemReport (paths: '{}', '{}'). Falling back to Helvetica. Error: {}",
                        REGULAR_FONT_RESOURCE_PATH, BOLD_FONT_RESOURCE_PATH, e.getMessage());
                currentRegularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                currentBoldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            }

            try (PdfWriter writer = new PdfWriter(baos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc, PageSize.A4)) {

                document.setMargins(36, 36, 36, 36);

                // === 0. Заголовок отчета ===
                addParagraph(document, "Сводный Отчет по Системе Мониторинга", currentBoldFont, 20, TextAlignment.CENTER);
                addParagraph(document, " ", currentRegularFont, 10);
                addParagraph(document,
                        "Настоящий документ содержит общую статистику по работе системы мониторинга, " +
                                "а также детализированную информацию по каждой зарегистрированной компании " +
                                "за указанный отчетный период.",
                        currentRegularFont, 10, TextAlignment.JUSTIFIED);
                addParagraph(document, " ", currentRegularFont, 12);

                // === 1. Общая информация об отчете (из ReportHeaderDto) ===
                // Предполагаем, что ReportHeaderDto используется для AdminReportDto
                ReportHeaderForAdminDto header = reportData.getHeader(); // Убедись, что тип хедера совпадает
                if (header != null) {
                    addSectionTitle(document, "1. Информация об отчете", currentBoldFont);

                    Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1.8f, 3f})).useAllAvailableWidth().setMarginBottom(12);
                    headerTable.addCell(createHeaderInfoCell("Наименование отчета:", currentBoldFont));
                    headerTable.addCell(createInfoCell(strVal(header.getReportName()), currentRegularFont));
                    headerTable.addCell(createHeaderInfoCell("Отчетный период:", currentBoldFont));
                    headerTable.addCell(createInfoCell(formatDateTime(header.getPeriodFrom()) + " — " + formatDateTime(header.getPeriodTo()), currentRegularFont));
                    headerTable.addCell(createHeaderInfoCell("Дата формирования:", currentBoldFont));
                    headerTable.addCell(createInfoCell(formatDateTime(header.getReportGeneratedAt()), currentRegularFont));
                    headerTable.addCell(createHeaderInfoCell("Отчет сформировал:", currentBoldFont));
                    headerTable.addCell(createInfoCell(strVal(header.getReportGeneratedBy()), currentRegularFont)); // или reportGeneratedBy
                    document.add(headerTable);
                }

                // === 2. Глобальная статистика по системе ===
                GlobalSystemStatsDto globalStats = reportData.getGlobalStats();
                if (globalStats != null) {
                    addSectionTitle(document, "2. Общая статистика по системе", currentBoldFont);
                    addParagraph(document, "Данный раздел отражает ключевые количественные показатели по всем активам и событиям в системе.", currentRegularFont, 9, TextAlignment.LEFT).setItalic().setMarginBottom(5);

                    Table statsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f})).useAllAvailableWidth().setMarginBottom(12);
                    statsTable.addCell(createInfoCell("Всего компаний в системе:", currentRegularFont));
                    statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalCompanies()), currentRegularFont, TextAlignment.RIGHT));
                    statsTable.addCell(createInfoCell("Всего единиц техники:", currentRegularFont));
                    statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalTechnics()), currentRegularFont, TextAlignment.RIGHT));
                    statsTable.addCell(createInfoCell("Из них активно:", currentRegularFont));
                    statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalActiveTechnics()), currentRegularFont, TextAlignment.RIGHT));
                    statsTable.addCell(createInfoCell("Всего сенсоров зарегистрировано:", currentRegularFont));
                    statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalSensors()), currentRegularFont, TextAlignment.RIGHT));
                    statsTable.addCell(createInfoCell("Из них активно передают данные:", currentRegularFont));
                    statsTable.addCell(createInfoCell(String.valueOf(globalStats.getTotalActiveSensors()), currentRegularFont, TextAlignment.RIGHT));
                    statsTable.addCell(createInfoCell("Среднее кол-во регистрируемых нарушений в час (по системе):", currentRegularFont));
                    statsTable.addCell(createInfoCell(String.format(Locale.US, "%.2f", globalStats.getAverageViolationsPerHour()), currentRegularFont, TextAlignment.RIGHT));
                    document.add(statsTable);
                }

                // === 3. Детализация по компаниям ===
                if (reportData.getCompaniesSummary() != null && !reportData.getCompaniesSummary().isEmpty()) {
                    addSectionTitle(document, "3. Статистика по компаниям", currentBoldFont);

                    for (CompanySummaryReportDto companySummary : reportData.getCompaniesSummary()) {
                        addParagraph(document, " ", currentRegularFont, 6);
                        String companyTitle = String.format("Компания: «%s» (ИНН: %s)",
                                strVal(companySummary.getCompanyName()), strVal(companySummary.getCompanyInn()));
                        Paragraph companyParagraph = new Paragraph(companyTitle)
                                .setFont(currentBoldFont).setFontSize(14).setMarginBottom(2);
                        document.add(companyParagraph);

                        Table companyInfoTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f})).useAllAvailableWidth().setMarginTop(4).setMarginBottom(6);
                        companyInfoTable.addCell(createInfoCell("Всего техники в компании:", currentRegularFont));
                        companyInfoTable.addCell(createInfoCell(String.valueOf(companySummary.getTotalTechnicsInCompany()), currentRegularFont, TextAlignment.RIGHT));
                        companyInfoTable.addCell(createInfoCell("Активной техники:", currentRegularFont));
                        companyInfoTable.addCell(createInfoCell(String.valueOf(companySummary.getActiveTechnicsInCompany()), currentRegularFont, TextAlignment.RIGHT));
                        companyInfoTable.addCell(createInfoCell("Всего сенсоров в компании:", currentRegularFont));
                        companyInfoTable.addCell(createInfoCell(String.valueOf(companySummary.getTotalSensorsInCompany()), currentRegularFont, TextAlignment.RIGHT));
                        companyInfoTable.addCell(createInfoCell("Активных сенсоров:", currentRegularFont));
                        companyInfoTable.addCell(createInfoCell(String.valueOf(companySummary.getActiveSensorsInCompany()), currentRegularFont, TextAlignment.RIGHT));
                        companyInfoTable.addCell(createInfoCell("Среднее кол-во нарушений в час (по компании):", currentRegularFont));
                        companyInfoTable.addCell(createInfoCell(String.format(Locale.US, "%.2f", companySummary.getCompanyViolationsPerHour()), currentRegularFont, TextAlignment.RIGHT));
                        document.add(companyInfoTable);

                        if (companySummary.getTechnicsStats() != null && !companySummary.getTechnicsStats().isEmpty()) {
                            Paragraph technicsSubtitle = new Paragraph("Сводка по технике данной компании:")
                                    .setFont(currentBoldFont).setFontSize(11).setMarginTop(6).setMarginBottom(2);
                            document.add(technicsSubtitle);

                            Table technicsStatsTable = new Table(UnitValue.createPercentArray(new float[]{2.8f, 1.2f, 1f, 1f, 1f})).useAllAvailableWidth().setMarginTop(5);
                            addHeaderCellToTable(technicsStatsTable, "Техника (Бренд Модель / S/N)", currentBoldFont);
                            addHeaderCellToTable(technicsStatsTable, "Статус", currentBoldFont);
                            addHeaderCellToTable(technicsStatsTable, "Сенсоров (акт.)", currentBoldFont);
                            addHeaderCellToTable(technicsStatsTable, "Предупр.", currentBoldFont);
                            addHeaderCellToTable(technicsStatsTable, "Критич.", currentBoldFont);

                            for (TechnicStatsDto technicStat : companySummary.getTechnicsStats()) {
                                addCellToTable(technicsStatsTable, strVal(technicStat.getTechnicBrandModel()) + "\n(SN: " + strVal(technicStat.getTechnicSerialNumber()) + ")", currentRegularFont);
                                addCellToTable(technicsStatsTable, technicStat.isActive() ? "Активна" : "Неактивна", currentRegularFont, TextAlignment.CENTER);
                                addCellToTable(technicsStatsTable, String.valueOf(technicStat.getNumberOfSensors()), currentRegularFont, TextAlignment.CENTER);
                                addCellToTable(technicsStatsTable, String.valueOf(technicStat.getWarningAlerts()), currentRegularFont, TextAlignment.CENTER);
                                addCellToTable(technicsStatsTable, String.valueOf(technicStat.getCriticalAlerts()), currentRegularFont, TextAlignment.CENTER);
                            }
                            document.add(technicsStatsTable);
                        } else {
                            addParagraph(document, "Нет данных по технике для этой компании за указанный период.", currentRegularFont, 10).setItalic().setMarginTop(4);
                        }
                        addParagraph(document, " ", currentRegularFont, 8);
                    }
                } else {
                    addParagraph(document, "В системе не зарегистрировано компаний, или отсутствуют данные по компаниям.", currentRegularFont, 12, TextAlignment.CENTER).setItalic().setMarginTop(10);
                }

                // === 4. Заключение и Условные обозначения ===
                addParagraph(document, " ", currentRegularFont, 24);
                addSectionTitle(document, "4. Дополнительная информация", currentBoldFont);

                addParagraph(document, "Условные обозначения и примечания:", currentBoldFont, 11, TextAlignment.LEFT).setMarginTop(4).setMarginBottom(4);
                Paragraph notes = new Paragraph()
                        .setFont(currentRegularFont).setFontSize(9).setTextAlignment(TextAlignment.JUSTIFIED)
                        .add(new Text("• Все временные метки в отчете указаны в часовом поясе UTC (Coordinated Universal Time).\n").setBold())
                        .add("• 'Н/Д' (Нет Данных) используется в случаях, когда информация отсутствует или неприменима.\n")
                        .add("• 'Предупр.' и 'Критич.' в таблицах техники означают количество зафиксированных событий со статусами WARNING и CRITICAL соответственно для данной единицы техники за отчетный период.\n")
                        .add("• 'Среднее кол-во нарушений в час' рассчитывается как общее число событий WARNING и CRITICAL, деленное на общее количество часов, в которые была зафиксирована активность техники (упрощенная оценка).\n")
                        .add("• Отчет является автоматически сгенерированным на основе данных системы мониторинга и предназначен для внутреннего использования.");
                document.add(notes);

                addParagraph(document, " ", currentRegularFont, 12);
                addParagraph(document, "Конец отчета", currentRegularFont, 10, TextAlignment.CENTER).setItalic();

                document.close();
                log.info("Admin system PDF report generated successfully at {}", LocalDateTime.now());
                return baos.toByteArray();

            }
        } catch (IOException e) {
            log.error("IOException (PDF writing) in generateAdminSystemReportPdf: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report due to IO error", e);
        } catch (Exception e) {
            log.error("Unexpected exception in generateAdminSystemReportPdf: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while generating the PDF report", e);
        }
    }


    // --- Вспомогательные методы ---
    private void addSectionTitle(Document document, String title, PdfFont font) {
        PdfFont effectiveFont = font;
        if (font == null) {
            try {
                effectiveFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            } catch (IOException e) {
                log.error("Fallback font HELVETICA_BOLD failed to load for section title", e);
            }
        }
        document.add(new Paragraph(title)
                .setFont(effectiveFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(6)
                .setMarginTop(12)
                .setBorderBottom(new SolidBorder(ColorConstants.GRAY, 0.5f))
                .setPaddingBottom(2));
    }

    private Paragraph addParagraph(Document document, String text, PdfFont font, float fontSize, TextAlignment alignment) {
        PdfFont effectiveFont = font;
        if (font == null) {
            try {
                effectiveFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (IOException e) {
                log.error("Fallback font HELVETICA failed to load for paragraph", e);
            }
        }
        Paragraph p = new Paragraph(text)
                .setFont(effectiveFont)
                .setFontSize(fontSize)
                .setTextAlignment(alignment);
        document.add(p);
        return p;
    }

    private Paragraph addParagraph(Document document, String text, PdfFont font, float fontSize) {
        return addParagraph(document, text, font, fontSize, TextAlignment.LEFT);
    }

    private Cell createHeaderInfoCell(String text, PdfFont fontToUse) {
        PdfFont effectiveFont = fontToUse;
        if (fontToUse == null) {
            try {
                effectiveFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            } catch (IOException e) {
                log.error("Fallback font HELVETICA_BOLD failed to load for HeaderInfoCell", e);
            }
        }
        return new Cell().add(new Paragraph(text).setFont(effectiveFont).setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .setPaddingRight(8).setPaddingBottom(2).setPaddingTop(2);
    }

    private Cell createInfoCell(String text, PdfFont fontToUse) {
        return createInfoCell(text, fontToUse, TextAlignment.LEFT);
    }

    private Cell createInfoCell(String text, PdfFont fontToUse, TextAlignment alignment) {
        PdfFont effectiveFont = fontToUse;
        if (fontToUse == null) {
            try {
                effectiveFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (IOException e) {
                log.error("Fallback font HELVETICA failed to load for InfoCell", e);
            }
        }
        return new Cell().add(new Paragraph(text).setFont(effectiveFont).setFontSize(9))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.TOP)
                .setPaddingLeft(5).setPaddingBottom(2).setPaddingTop(2);
    }

    private void addCellToTable(Table table, String text, PdfFont fontToUse) {
        addCellToTable(table, text, fontToUse, TextAlignment.LEFT);
    }

    private void addCellToTable(Table table, String text, PdfFont fontToUse, TextAlignment alignment) {
        PdfFont effectiveFont = fontToUse;
        if (fontToUse == null) {
            try {
                effectiveFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (IOException e) {
                log.error("Fallback font HELVETICA failed to load for TableCell", e);
            }
        }
        Cell cell = new Cell().add(new Paragraph(text).setFont(effectiveFont).setFontSize(8))
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(4);
        table.addCell(cell);
    }

    private void addHeaderCellToTable(Table table, String text, PdfFont fontToUse) {
        PdfFont effectiveFont = fontToUse;
        if (fontToUse == null) {
            try {
                effectiveFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            } catch (IOException e) {
                log.error("Fallback font HELVETICA_BOLD failed to load for TableHeaderCell", e);
            }
        }
        Cell cell = new Cell().add(new Paragraph(text).setFont(effectiveFont).setFontSize(9).setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBackgroundColor(new DeviceGray(0.85f))
                .setPadding(5);
        table.addCell(cell);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : NOT_AVAILABLE_TEXT;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : NOT_AVAILABLE_TEXT;
    }

    private String strVal(Object obj) {
        if (obj == null) return NOT_AVAILABLE_TEXT;
        String s = obj.toString();
        if (obj instanceof Number && s.endsWith(".0")) { // Убираем .0 для целых чисел
            s = s.substring(0, s.length() - 2);
        }
        return s.isBlank() ? NOT_AVAILABLE_TEXT : s;
    }

    private String uuidVal(UUID obj) {
        return obj != null ? obj.toString() : NOT_AVAILABLE_TEXT;
    }
}