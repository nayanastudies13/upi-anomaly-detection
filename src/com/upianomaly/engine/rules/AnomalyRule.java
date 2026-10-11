package com.upianomaly.engine.rules;

import com.upianomaly.engine.model.Alert;
import com.upianomaly.engine.model.Transaction;

import java.util.Deque;
import java.util.Optional;

/**
 * A single anomaly-detection check.
 *
 * Implementations look only at the current transaction plus the user's
 * recent transaction window, and decide whether that is suspicious enough
 * to raise an Alert. Adding a new type of check later just means adding a
 * new class that implements this interface - nothing else has to change.
 */
public interface AnomalyRule {

    /**
     * @param transaction     the transaction currently being processed
     * @param recentByUser    that user's transactions within the configured time window,
     *                        oldest first, including the current transaction
     * @return an Alert if this rule considers the transaction anomalous, otherwise empty
     */
    Optional<Alert> evaluate(Transaction transaction, Deque<Transaction> recentByUser);
}
