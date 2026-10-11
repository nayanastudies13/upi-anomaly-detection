package com.upianomaly.engine.model;

import java.time.Instant;

/**
 * Represents one anomaly finding, produced by a rule when it flags a transaction.
 */
public class Alert {

    private final AnomalyType type;
    private final Transaction transaction;
    private final String reason;
    private final Instant detectedAt;

    public Alert(AnomalyType type, Transaction transaction, String reason) {
        this.type = type;
        this.transaction = transaction;
        this.reason = reason;
        this.detectedAt = Instant.now();
    }

    public AnomalyType getType() {
        return type;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String getReason() {
        return reason;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    @Override
    public String toString() {
        return String.format("[ALERT][%s] user=%s txn=%s amount=%.2f -> %s",
                type, transaction.getUserId(), transaction.getTransactionId(),
                transaction.getAmount(), reason);
    }
}
