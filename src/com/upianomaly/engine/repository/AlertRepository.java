package com.upianomaly.engine.repository;

import com.upianomaly.engine.api.ApiDtos.AlertView;
import com.upianomaly.engine.api.ApiExceptions;
import com.upianomaly.engine.model.Alert;
import com.upianomaly.engine.model.Transaction;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AlertRepository {

    private static final String SELECT_FIELDS = "SELECT id, transaction_id, user_id, amount, "
            + "anomaly_type, reason, detected_at FROM alerts";

    private final DatabaseConnection databaseConnection;

    public AlertRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public AlertView save(Alert alert) {
        Transaction transaction = alert.getTransaction();
        String sql = "INSERT INTO alerts "
                + "(transaction_id, user_id, amount, anomaly_type, reason, detected_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, transaction.getTransactionId());
            statement.setString(2, transaction.getUserId());
            statement.setDouble(3, transaction.getAmount());
            statement.setString(4, alert.getType().name());
            statement.setString(5, alert.getReason());
            statement.setTimestamp(6, Timestamp.from(alert.getDetectedAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return AlertView.from(keys.getLong(1), alert);
                }
            }
            throw new SQLException("MySQL did not return an alert ID.");
        } catch (SQLException e) {
            throw new ApiExceptions.DatabaseException("Could not save alert.", e);
        }
    }

    public List<AlertView> findAll() {
        return query(SELECT_FIELDS + " ORDER BY detected_at DESC", null);
    }

    public List<AlertView> findRecent(int limit) {
        return query(SELECT_FIELDS + " ORDER BY detected_at DESC LIMIT ?", limit);
    }

    public Optional<AlertView> findById(long id) {
        return query(SELECT_FIELDS + " WHERE id = ?", id).stream().findFirst();
    }

    public long count() {
        return count("SELECT COUNT(*) FROM alerts", null);
    }

    public long countByType(String anomalyType) {
        return count("SELECT COUNT(*) FROM alerts WHERE anomaly_type = ?", anomalyType);
    }

    private long count(String sql, String anomalyType) {
        try (Connection connection = databaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (anomalyType != null) {
                statement.setString(1, anomalyType);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException e) {
            throw new ApiExceptions.DatabaseException("Could not count alerts.", e);
        }
    }

    private List<AlertView> query(String sql, Object parameter) {
        List<AlertView> alerts = new ArrayList<>();
        try (Connection connection = databaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter instanceof Integer limit) {
                statement.setInt(1, limit);
            } else if (parameter instanceof Long id) {
                statement.setLong(1, id);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    alerts.add(new AlertView(
                            resultSet.getLong("id"),
                            resultSet.getString("transaction_id"),
                            resultSet.getString("user_id"),
                            resultSet.getDouble("amount"),
                            resultSet.getString("anomaly_type"),
                            resultSet.getString("reason"),
                            resultSet.getTimestamp("detected_at").toInstant()));
                }
            }
            return alerts;
        } catch (SQLException e) {
            throw new ApiExceptions.DatabaseException("Could not read alerts.", e);
        }
    }
}