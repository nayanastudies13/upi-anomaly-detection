package com.upianomaly.engine.service;

import com.upianomaly.engine.alert.AlertService;
import com.upianomaly.engine.api.ApiExceptions;
import com.upianomaly.engine.api.ApiDtos.AlertView;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class AlertExportService {

    private final AlertService alertService;

    public AlertExportService(AlertService alertService) {
        this.alertService = alertService;
    }

    public byte[] exportAlerts() {
        List<AlertView> alerts = alertService.findAll();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Alerts");
            Row header = sheet.createRow(0);
            String[] columns = {"Alert ID", "Transaction ID", "User ID", "Amount", "Anomaly Type", "Reason", "Detected At"};
            for (int column = 0; column < columns.length; column++) {
                header.createCell(column).setCellValue(columns[column]);
            }
            for (int index = 0; index < alerts.size(); index++) {
                AlertView alert = alerts.get(index);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(alert.id());
                row.createCell(1).setCellValue(alert.transactionId());
                row.createCell(2).setCellValue(alert.userId());
                row.createCell(3).setCellValue(alert.amount());
                row.createCell(4).setCellValue(alert.anomalyType());
                row.createCell(5).setCellValue(alert.reason());
                row.createCell(6).setCellValue(alert.detectedAt().toString());
            }
            for (int column = 0; column < columns.length; column++) {
                sheet.autoSizeColumn(column);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new ApiExceptions.ExportException("Could not create the Excel alert export.", e);
        }
    }
}