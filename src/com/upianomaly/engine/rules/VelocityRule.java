package com.upianomaly.engine.rules;

import com.upianomaly.engine.model.Alert;
import com.upianomaly.engine.model.AnomalyType;
import com.upianomaly.engine.model.Transaction;

import java.util.Deque;
import java.util.Optional;
import java.time.Duration;
import java.time.Instant;

/**
 * Flags a user who makes too many transactions in a short window of time.
 *
 * This is "velocity-based" detection: it does not care about the amount at
 * all, only about HOW FAST transactions are arriving for a given user.
 * Real-world example: a compromised UPI account suddenly firing off many
 * payments in quick succession.
 */
public class VelocityRule implements AnomalyRule {

    private final int maxTransactionsInWindow;
    private final Duration window;

    /**
     * @param maxTransactionsInWindow if the user has MORE than this many
     *                                transactions inside the shared time window
     *                                (see AnomalyDetectionEngine), it is flagged.
     */
    public VelocityRule(int maxTransactionsInWindow) {
        this(maxTransactionsInWindow, Duration.ofSeconds(10));
    }

    public VelocityRule(int maxTransactionsInWindow, Duration window) {
        this.maxTransactionsInWindow = maxTransactionsInWindow;
        this.window = window;
    }

    @Override
    public Optional<Alert> evaluate(Transaction transaction, Deque<Transaction> recentByUser) {
        Instant cutoff = transaction.getTimestamp().minus(window);
        int countInWindow = (int) recentByUser.stream()
            .filter(recent -> !recent.getTimestamp().isBefore(cutoff))
            .filter(recent -> !recent.getTimestamp().isAfter(transaction.getTimestamp()))
            .count();

        if (countInWindow > maxTransactionsInWindow) {
            String reason = String.format("More than %d transactions detected within %d seconds.",
                maxTransactionsInWindow, window.toSeconds());
            return Optional.of(new Alert(AnomalyType.VELOCITY_ALERT, transaction, reason));
        }
        return Optional.empty();
    }
}
