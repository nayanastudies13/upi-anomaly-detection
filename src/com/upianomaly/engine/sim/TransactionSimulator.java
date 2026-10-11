package com.upianomaly.engine.sim;

import com.upianomaly.engine.model.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a fixed, readable list of test transactions covering every
 * scenario the engine is supposed to catch, plus normal traffic that
 * should NOT raise any alert. This stands in for a real feed of incoming
 * UPI transactions (e.g. from a message queue or a bank's payment gateway)
 * for demo purposes.
 */
public class TransactionSimulator {

    private int counter = 0;

    private String nextId() {
        counter++;
        return "TXN" + String.format("%03d", counter);
    }

    public List<Transaction> buildScenarios() {
        List<Transaction> transactions = new ArrayList<>();
        Instant now = Instant.now();

        // --- Scenario 1: normal, everyday transactions from different users ---
        transactions.add(new Transaction(nextId(), "priya@upi", "merchant1@upi", 450.00, now));
        transactions.add(new Transaction(nextId(), "arjun@upi", "merchant2@upi", 1200.00, now));
        transactions.add(new Transaction(nextId(), "priya@upi", "merchant3@upi", 899.50, now));

        // --- Scenario 2: high velocity - "rahul@upi" fires off many payments fast ---
        for (int i = 0; i < 7; i++) {
            transactions.add(new Transaction(nextId(), "rahul@upi", "merchant" + i + "@upi", 300.00, now));
        }

        // --- Scenario 3: a single high-value transaction ---
        transactions.add(new Transaction(nextId(), "sneha@upi", "merchant9@upi", 75000.00, now));

        // --- Scenario 4: volume burst - "vikram@upi" sends several medium
        //     payments that individually look fine but add up quickly ---
        for (int i = 0; i < 5; i++) {
            transactions.add(new Transaction(nextId(), "vikram@upi", "merchant" + i + "@upi", 15000.00, now));
        }

        // --- Scenario 5: more normal traffic mixed in at the end ---
        transactions.add(new Transaction(nextId(), "arjun@upi", "merchant4@upi", 220.00, now));

        return transactions;
    }
}
