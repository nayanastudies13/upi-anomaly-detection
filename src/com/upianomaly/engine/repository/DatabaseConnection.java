package com.upianomaly.engine.repository;

import com.upianomaly.engine.api.ApiExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection {

    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnection(
            @Value("${DB_URL:jdbc:mysql://localhost:3306/upi_anomaly_detection?serverTimezone=UTC}") String url,
            @Value("${DB_USERNAME:root}") String username,
            @Value("${DB_PASSWORD:}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection open() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new ApiExceptions.DatabaseException("Could not connect to MySQL. Check DB_URL and credentials.", e);
        }
    }

    public void verifyConnection() {
        try (Connection ignored = open()) {
            // Opening and closing the connection is the health check.
        } catch (SQLException e) {
            throw new ApiExceptions.DatabaseException("Could not verify the MySQL connection.", e);
        }
    }
}