package com.upianomaly.engine.rules;

import com.upianomaly.engine.model.Alert;
import com.upianomaly.engine.model.AnomalyType;
import com.upianomaly.engine.model.Transaction;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Optional;

public class MeanAmountRule implements AnomalyRule {

    private final Duration window;

    public MeanAmountRule(Duration window) {
        this.window = window;
    }

    @Override
    public Optional<Alert> evaluate(Transaction transaction, Deque<Transaction> recentByUser) {
        Instant cutoff = transaction.getTimestamp().minus(window);
        double[] previousAmounts = recentByUser.stream()
                .filter(recent -> recent != transaction)
                .filter(recent -> recent.getTimestamp().isAfter(cutoff))
                .filter(recent -> !recent.getTimestamp().isAfter(transaction.getTimestamp()))
                .mapToDouble(Transaction::getAmount)
                .toArray();

        if (previousAmounts.length == 0) {
            return Optional.empty();
        }

        double meanAmount = 0.0;
        for (double amount : previousAmounts) {
            meanAmount += amount;
        }
        meanAmount /= previousAmounts.length;

        if (transaction.getAmount() > meanAmount) {
            String reason = "Transaction amount is greater than the user's recent mean transaction amount.";
            return Optional.of(new Alert(AnomalyType.ABOVE_MEAN_AMOUNT, transaction, reason));
        }
        return Optional.empty();
    }
}