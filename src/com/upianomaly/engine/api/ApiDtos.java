package com.upianomaly.engine.api;

import com.upianomaly.engine.model.Alert;
import com.upianomaly.engine.model.Transaction;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record TransactionRequest(
            String transactionId,
            String userId,
            String beneficiaryId,
            double amount,
            String timestamp) {

        public Transaction toTransaction() {
            String cleanTransactionId = required(transactionId, "Transaction ID");
            String cleanUserId = required(userId, "User ID");
            String cleanBeneficiaryId = required(beneficiaryId, "Beneficiary ID");
            if (!Double.isFinite(amount) || amount <= 0) {
                throw new ApiExceptions.InvalidTransactionException("Transaction amount must be greater than 0.");
            }
            return new Transaction(cleanTransactionId, cleanUserId, cleanBeneficiaryId,
                    amount, parseTimestamp(timestamp));
        }

        private static String required(String value, String label) {
            if (value == null || value.isBlank()) {
                throw new ApiExceptions.InvalidTransactionException(label + " cannot be empty.");
            }
            return value.trim();
        }

        private static Instant parseTimestamp(String value) {
            if (value == null || value.isBlank()) {
                throw new ApiExceptions.InvalidTransactionException("Timestamp cannot be empty.");
            }
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException ignored) {
                try {
                    return OffsetDateTime.parse(value).toInstant();
                } catch (DateTimeParseException alsoIgnored) {
                    try {
                        return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
                    } catch (DateTimeParseException invalid) {
                        throw new ApiExceptions.InvalidTransactionException(
                                "Timestamp must be ISO-8601, for example 2026-10-05T10:30:00.");
                    }
                }
            }
        }
    }

    public record TransactionView(
            String transactionId,
            String userId,
            String beneficiaryId,
            double amount,
            Instant timestamp,
            String status) {

        public static TransactionView from(Transaction transaction, String status) {
            return new TransactionView(transaction.getTransactionId(), transaction.getUserId(),
                    transaction.getBeneficiaryId(), transaction.getAmount(), transaction.getTimestamp(), status);
        }
    }

    public record AlertView(
            long id,
            String transactionId,
            String userId,
            double amount,
            String anomalyType,
            String reason,
            Instant detectedAt) {

        public static AlertView from(long id, Alert alert) {
            Transaction transaction = alert.getTransaction();
            return new AlertView(id, transaction.getTransactionId(), transaction.getUserId(),
                    transaction.getAmount(), alert.getType().name(), alert.getReason(), alert.getDetectedAt());
        }
    }

    public record DashboardView(
            long totalTransactions,
            long totalAlerts,
            long velocityAlerts,
            long aboveMeanAmountAlerts,
            List<TransactionView> recentTransactions,
            List<AlertView> recentAlerts) {
    }

    public record ApiError(String error, int status) {
    }

    public record HealthView(String status, String database) {
    }
}