package com.upianomaly.engine.rules;

import com.upianomaly.engine.model.AnomalyType;
import com.upianomaly.engine.model.Transaction;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeanAmountRuleTest {

    private static final Instant START = Instant.parse("2026-10-05T10:00:00Z");
    private final MeanAmountRule rule = new MeanAmountRule(Duration.ofSeconds(20));

    @Test
    void doesNotAlertWithoutPreviousTransactions() {
        Deque<Transaction> history = history(txn("CURRENT", 10, 5000));

        assertTrue(rule.evaluate(history.getLast(), history).isEmpty());
    }

    @Test
    void alertsWhenCurrentAmountIsAbovePreviousMean() {
        Deque<Transaction> history = history(
                txn("A", 1, 1000), txn("B", 2, 1500), txn("C", 3, 2000), txn("CURRENT", 4, 5000));

        var alert = rule.evaluate(history.getLast(), history).orElseThrow();

        assertEquals(AnomalyType.ABOVE_MEAN_AMOUNT, alert.getType());
        assertEquals("Transaction amount is greater than the user's recent mean transaction amount.",
                alert.getReason());
    }

    @Test
    void doesNotAlertWhenAmountEqualsOrIsBelowMean() {
        Deque<Transaction> equalHistory = history(txn("PREVIOUS", 1, 1000), txn("CURRENT", 2, 1000));
        Deque<Transaction> lowerHistory = history(txn("PREVIOUS", 1, 1500), txn("CURRENT", 2, 1000));

        assertTrue(rule.evaluate(equalHistory.getLast(), equalHistory).isEmpty());
        assertTrue(rule.evaluate(lowerHistory.getLast(), lowerHistory).isEmpty());
    }

    @Test
    void excludesTransactionsAtOrBeforeTwentySecondCutoff() {
        Deque<Transaction> history = history(txn("OLD", 0, 1000), txn("CURRENT", 20, 5000));

        assertTrue(rule.evaluate(history.getLast(), history).isEmpty());
    }

    @Test
    void includesPreviousTransactionsInsideTwentySecondWindow() {
        Deque<Transaction> history = history(txn("PREVIOUS", 1, 1000), txn("CURRENT", 20, 5000));

        assertEquals(AnomalyType.ABOVE_MEAN_AMOUNT,
                rule.evaluate(history.getLast(), history).orElseThrow().getType());
    }

    private Deque<Transaction> history(Transaction... transactions) {
        Deque<Transaction> history = new ArrayDeque<>();
        for (Transaction transaction : transactions) history.add(transaction);
        return history;
    }

    private Transaction txn(String id, int second, double amount) {
        return new Transaction(id, "user1", "merchant1", amount, START.plusSeconds(second));
    }
}