package com.upianomaly.engine.core;

import com.upianomaly.engine.model.Transaction;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * Keeps a short rolling history of one user's recent transactions.
 *
 * Multiple worker threads can be processing transactions for the SAME user
 * at (almost) the same time, so every method here is synchronized. The
 * history itself is small (only transactions within the sliding window are
 * kept), so a simple synchronized ArrayDeque is enough - no need for
 * anything fancier for a project of this size.
 */
public class UserActivity {

    private final Deque<Transaction> recentTransactions = new ArrayDeque<>();
    private long nextSubmissionSequence;
    private long nextProcessingSequence;

    public synchronized long assignSubmissionSequence() {
        return nextSubmissionSequence++;
    }

    public synchronized void awaitProcessingTurn(long sequence) {
        boolean interrupted = false;
        while (sequence != nextProcessingSequence) {
            try {
                wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void completeProcessingTurn(long sequence) {
        if (sequence != nextProcessingSequence) {
            throw new IllegalStateException("User transaction processing order was violated.");
        }
        nextProcessingSequence++;
        notifyAll();
    }

    /**
     * Records a new transaction and forgets anything older than maxWindow.
     * Returns the list of transactions still inside that window (including
     * the one just added), which callers use to evaluate rules.
     */
    public synchronized Deque<Transaction> recordAndGetWindow(Transaction txn, Duration maxWindow) {
        List<Transaction> orderedTransactions = new ArrayList<>(recentTransactions);
        orderedTransactions.add(txn);
        orderedTransactions.sort(Comparator.comparing(Transaction::getTimestamp));
        recentTransactions.clear();
        recentTransactions.addAll(orderedTransactions);
        Instant cutoff = txn.getTimestamp().minus(maxWindow);

        while (!recentTransactions.isEmpty() && recentTransactions.peekFirst().getTimestamp().isBefore(cutoff)) {
            recentTransactions.pollFirst();
        }
        return new ArrayDeque<>(recentTransactions); // defensive copy
    }
}
