package com.upianomaly.engine.api;

import com.upianomaly.engine.api.ApiDtos.AlertView;
import com.upianomaly.engine.alert.AlertService;
import com.upianomaly.engine.service.AlertExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AlertController {

    private final AlertService alertService;
    private final AlertExportService alertExportService;

    public AlertController(AlertService alertService, AlertExportService alertExportService) {
        this.alertService = alertService;
        this.alertExportService = alertExportService;
    }

    @GetMapping
    public List<AlertView> findAll() {
        return alertService.findAll();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=alerts.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(alertExportService.exportAlerts());
    }

    @GetMapping("/{id}")
    public AlertView findById(@PathVariable("id") long id) {
        return alertService.findById(id);
    }
}