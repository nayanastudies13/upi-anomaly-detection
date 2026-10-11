package com.upianomaly.engine.rules;

import com.upianomaly.engine.model.AnomalyType;
import com.upianomaly.engine.model.Transaction;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelocityRuleTest {

    private static final Instant START = Instant.parse("2026-10-05T10:00:00Z");

    @Test
    void doesNotAlertAtExactlyFiveTransactions() {
        Deque<Transaction> history = new ArrayDeque<>();
        for (int second = 0; second < 5; second++) {
            history.add(transaction("TXN" + second, second, 100));
        }

        Optional<?> alert = new VelocityRule(5).evaluate(history.getLast(), history);

        assertFalse(alert.isPresent());
    }

    @Test
    void alertsAtSixTransactionsWithinTenSeconds() {
        Deque<Transaction> history = new ArrayDeque<>();
        for (int second : new int[]{0, 1, 2, 3, 5, 9}) {
            history.add(transaction("TXN" + second, second, 100));
        }

        var alert = new VelocityRule(5).evaluate(history.getLast(), history).orElseThrow();

        assertEquals(AnomalyType.VELOCITY_ALERT, alert.getType());
        assertEquals("More than 5 transactions detected within 10 seconds.", alert.getReason());
    }

    @Test
    void ignoresTransactionsOutsideTenSecondWindow() {
        Deque<Transaction> history = new ArrayDeque<>();
        for (int second = 0; second < 5; second++) {
            history.add(transaction("OLD" + second, second, 100));
        }
        history.add(transaction("CURRENT", 11, 100));

        assertTrue(new VelocityRule(5).evaluate(history.getLast(), history).isEmpty());
    }

    private Transaction transaction(String id, int second, double amount) {
        return new Transaction(id, "user1", "merchant1", amount, START.plusSeconds(second));
    }
}