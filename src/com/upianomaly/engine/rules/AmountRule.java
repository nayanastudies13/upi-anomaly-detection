package com.upianomaly.engine.rules;

import com.upianomaly.engine.model.Alert;
import com.upianomaly.engine.model.AnomalyType;
import com.upianomaly.engine.model.Transaction;

import java.util.Deque;
import java.util.Optional;
import java.time.Duration;
import java.time.Instant;

/**
 * Flags transactions based on money, not speed. Covers two separate cases:
 *
 *  1. HIGH_VALUE       - a single transaction is unusually large on its own.
 *  2. HIGH_VOLUME_BURST - no single transaction looks alarming, but the user
 *                         has moved a large total amount within the short
 *                         monitoring window (e.g. several medium payments
 *                         back to back that add up to a lot of money fast).
 *
 * Only one Alert is returned per call - if both conditions are true, the
 * single high-value case takes priority since it is the more specific,
 * more actionable finding.
 */
public class AmountRule implements AnomalyRule {

    private final double singleTransactionThreshold;
    private final double cumulativeWindowThreshold;
    private final Duration monitoringWindow;

    public AmountRule(double singleTransactionThreshold, double cumulativeWindowThreshold) {
        this(singleTransactionThreshold, cumulativeWindowThreshold, Duration.ofSeconds(10));
    }

    public AmountRule(double singleTransactionThreshold, double cumulativeWindowThreshold,
                      Duration monitoringWindow) {
        this.singleTransactionThreshold = singleTransactionThreshold;
        this.cumulativeWindowThreshold = cumulativeWindowThreshold;
        this.monitoringWindow = monitoringWindow;
    }

    @Override
    public Optional<Alert> evaluate(Transaction transaction, Deque<Transaction> recentByUser) {
        if (transaction.getAmount() > singleTransactionThreshold) {
            String reason = String.format(
                    "single transaction amount %.2f exceeds high-value threshold %.2f",
                    transaction.getAmount(), singleTransactionThreshold);
            return Optional.of(new Alert(AnomalyType.HIGH_VALUE, transaction, reason));
        }

        Instant cutoff = transaction.getTimestamp().minus(monitoringWindow);
        double totalInWindow = recentByUser.stream()
            .filter(recent -> !recent.getTimestamp().isBefore(cutoff))
            .filter(recent -> !recent.getTimestamp().isAfter(transaction.getTimestamp()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        if (totalInWindow > cumulativeWindowThreshold) {
            String reason = String.format(
                    "user %s moved a total of %.2f across %d transactions within the monitoring window (limit is %.2f)",
                    transaction.getUserId(), totalInWindow, recentByUser.size(), cumulativeWindowThreshold);
            return Optional.of(new Alert(AnomalyType.HIGH_VOLUME_BURST, transaction, reason));
        }

        return Optional.empty();
    }
}
