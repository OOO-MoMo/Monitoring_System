package ru.momo.monitoring.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.ExcelExportService;
import ru.momo.monitoring.store.dto.request.SensorDataHistoryDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ExcelExportServiceImpl implements ExcelExportService {

    @Override
    public ByteArrayInputStream generateSensorHistoryExcel(
            UUID sensorId,
            String sensorInfo,
            String periodFromString,
            String periodToString,
            String reportGeneratedAt,
            List<SensorDataHistoryDto> dataList) {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            String sheetName = "История_" + sensorInfo.replaceAll("[^a-zA-Z0-9_\\-]", "_").substring(0, Math.min(sensorInfo.length(), 20));
            Sheet sheet = workbook.createSheet(sheetName);

            // --- Стили ---
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

            CellStyle titleCellStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleCellStyle.setFont(titleFont);
            titleCellStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerInfoCellStyle = workbook.createCellStyle();
            Font headerInfoFont = workbook.createFont();
            headerInfoFont.setBold(true);
            headerInfoFont.setFontHeightInPoints((short) 10);
            headerInfoCellStyle.setFont(headerInfoFont);

            CellStyle dataCellStyle = workbook.createCellStyle();
            Font dataFont = workbook.createFont();
            dataFont.setFontHeightInPoints((short) 10);
            dataCellStyle.setFont(dataFont);
            dataCellStyle.setDataFormat(workbook.createDataFormat().getFormat("dd.MM.yyyy HH:mm:ss"));

            CellStyle numericDataCellStyle = workbook.createCellStyle();
            numericDataCellStyle.setFont(dataFont);
            numericDataCellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

            // --- Шапка отчета ---
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("История показаний сенсора");
            titleCell.setCellStyle(titleCellStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            int currentRowNum = 2;
            Row infoRow1 = sheet.createRow(currentRowNum++);
            infoRow1.createCell(0).setCellValue("Сенсор:");
            infoRow1.getCell(0).setCellStyle(headerInfoCellStyle);
            infoRow1.createCell(1).setCellValue(sensorInfo);

            Row infoRow2 = sheet.createRow(currentRowNum++);
            infoRow2.createCell(0).setCellValue("Период:");
            infoRow2.getCell(0).setCellStyle(headerInfoCellStyle);
            infoRow2.createCell(1).setCellValue(periodFromString + " - " + periodToString);

            Row infoRow3 = sheet.createRow(currentRowNum++);
            infoRow3.createCell(0).setCellValue("Отчет сформирован:");
            infoRow3.getCell(0).setCellStyle(headerInfoCellStyle);
            infoRow3.createCell(1).setCellValue(reportGeneratedAt);

            currentRowNum++;

            // --- Заголовки таблицы данных ---
            Row dataHeaderRow = sheet.createRow(currentRowNum++);
            String[] columns = {"Время показания", "Значение", "Статус"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = dataHeaderRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerInfoCellStyle);
            }

            // --- Заполнение данными ---
            for (SensorDataHistoryDto record : dataList) {
                Row row = sheet.createRow(currentRowNum++);

                Cell timeCell = row.createCell(0);
                if (record.getTimestamp() != null) {
                    timeCell.setCellValue(record.getTimestamp());
                    timeCell.setCellStyle(dataCellStyle);
                } else {
                    timeCell.setCellValue("Н/Д");
                }

                Cell valueCell = row.createCell(1);
                if (record.getValue() != null) {
                    valueCell.setCellValue(record.getValue());
                    valueCell.setCellStyle(numericDataCellStyle);
                } else {
                    valueCell.setCellValue("Н/Д");
                }

                row.createCell(2).setCellValue(record.getStatus() != null ? record.getStatus().name() : "Н/Д");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 15 * 256);
            sheet.setColumnWidth(2, 15 * 256);


            workbook.write(out);
            log.info("Excel report for sensor history {} ({} records) generated successfully.", sensorId, dataList.size());
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Error generating Excel for sensor {}: {}", sensorId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during Excel generation for sensor {}: {}", sensorId, e.getMessage(), e);
            throw new RuntimeException("Unexpected error generating Excel report: " + e.getMessage());
        }
    }
}