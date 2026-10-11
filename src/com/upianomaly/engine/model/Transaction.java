package com.upianomaly.engine.model;

import java.time.Instant;

/**
 * Represents a single UPI transaction entering the system.
 *
 * This is a plain immutable data holder (POJO). It carries no logic -
 * just the facts about one transaction that the rest of the engine needs.
 */
public class Transaction {

    private final String transactionId;
    private final String userId;        // the UPI id / account making the payment
    private final String beneficiaryId; // the UPI id / account receiving the payment
    private final double amount;
    private final Instant timestamp;

    public Transaction(String transactionId, String userId, String beneficiaryId,
                        double amount, Instant timestamp) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.beneficiaryId = beneficiaryId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public double getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("Txn[id=%s, user=%s, to=%s, amount=%.2f, time=%s]",
                transactionId, userId, beneficiaryId, amount, timestamp);
    }
}
