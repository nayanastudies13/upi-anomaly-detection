package com.upianomaly.engine.repository;

import com.upianomaly.engine.api.ApiDtos.TransactionView;
import com.upianomaly.engine.api.ApiExceptions;
import com.upianomaly.engine.model.Transaction;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository {

    private static final String SELECT_FIELDS = """
            SELECT t.transaction_id, t.user_id, t.beneficiary_id, t.amount, t.transaction_time,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM alerts a WHERE a.transaction_id = t.transaction_id
                   ) THEN 'ALERT' ELSE t.processing_status END AS status
            FROM transactions t
            """;

    private final DatabaseConnection databaseConnection;

    public TransactionRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void save(Transaction transaction) {
        String sql = "INSERT INTO transactions "
                + "(transaction_id, user_id, beneficiary_id, amount, transaction_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getTransactionId());
            statement.setString(2, transaction.getUserId());
            statement.setString(3, transaction.getBeneficiaryId());
            statement.setDouble(4, transaction.getAmount());
            statement.setTimestamp(5, Timestamp.from(transaction.getTimestamp()));
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new ApiExceptions.DuplicateTransactionException(transaction.getTransactionId());
            }
            throw databaseFailure("Could not save transaction.", e);
        }
    }

    public void markProcessed(String transactionId) {
        String sql = "UPDATE transactions SET processing_status = 'NORMAL' WHERE transaction_id = ?";
        try (Connection connection = databaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transactionId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseFailure("Could not update transaction processing status.", e);
        }
    }

    public List<TransactionView> findAll() {
        return query(SELECT_FIELDS + " ORDER BY t.transaction_time DESC", null);
    }

    public List<TransactionView> findRecent(int limit) {
        return query(SELECT_FIELDS + " ORDER BY t.transaction_time DESC LIMIT ?", limit);
    }

    public Optional<TransactionView> findById(String transactionId) {
        List<TransactionView> matches = query(SELECT_FIELDS + " WHERE t.transaction_id = ?", transactionId);
        return matches.stream().findFirst();
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Connection connection = databaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw databaseFailure("Could not count transactions.", e);
        }
    }

    private List<TransactionView> query(String sql, Object parameter) {
        List<TransactionView> transactions = new ArrayList<>();
        try (Connection connection = databaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter instanceof Integer limit) {
                statement.setInt(1, limit);
            } else if (parameter instanceof String transactionId) {
                statement.setString(1, transactionId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Instant timestamp = resultSet.getTimestamp("transaction_time").toInstant();
                    transactions.add(new TransactionView(
                            resultSet.getString("transaction_id"),
                            resultSet.getString("user_id"),
                            resultSet.getString("beneficiary_id"),
                            resultSet.getDouble("amount"),
                            timestamp,
                            resultSet.getString("status")));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw databaseFailure("Could not read transactions.", e);
        }
    }

    private ApiExceptions.DatabaseException databaseFailure(String message, SQLException cause) {
        return new ApiExceptions.DatabaseException(message, cause);
    }
}