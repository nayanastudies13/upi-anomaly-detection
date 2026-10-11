package com.upianomaly.engine.api;

import com.upianomaly.engine.api.ApiDtos.HealthView;
import com.upianomaly.engine.repository.DatabaseConnection;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class HealthController {

    private final DatabaseConnection databaseConnection;

    public HealthController(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @GetMapping
    public HealthView health() {
        databaseConnection.verifyConnection();
        return new HealthView("UP", "UP");
    }
}