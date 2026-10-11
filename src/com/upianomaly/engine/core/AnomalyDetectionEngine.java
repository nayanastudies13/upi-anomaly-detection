package com.upianomaly.engine.core;

import com.upianomaly.engine.alert.AlertService;
import com.upianomaly.engine.model.Transaction;
import com.upianomaly.engine.rules.AnomalyRule;
import com.upianomaly.engine.repository.TransactionRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The engine ties everything together:
 *
 *   incoming transactions -> BlockingQueue -> pool of TransactionWorker threads -> AlertService
 *
 * Why a queue + worker pool instead of just checking each transaction as it
 * arrives? In a real UPI system, transactions can arrive faster than a
 * single thread could evaluate them one by one, and evaluation (rule
 * checks, history lookups) takes some time. Putting incoming transactions
 * onto a queue lets the "producer" (whatever is receiving transactions)
 * carry on immediately, while a fixed pool of "consumer" worker threads
 * drains the queue and does the actual detection work in parallel. This is
 * the standard producer-consumer pattern.
 */
public class AnomalyDetectionEngine {

    private final BlockingQueue<Transaction> queue = new LinkedBlockingQueue<>();
    private final UserActivityStore activityStore = new UserActivityStore();
    private final ConcurrentHashMap<String, Long> sequenceByTransactionId = new ConcurrentHashMap<>();
    private final List<AnomalyRule> rules;
    private final AlertService alertService;
    private final Duration monitoringWindow;
    private final int workerCount;
    private final TransactionRepository transactionRepository;

    private final List<Thread> workerThreads = new java.util.ArrayList<>();
    private volatile boolean started;

    public AnomalyDetectionEngine(List<AnomalyRule> rules,
                                   AlertService alertService,
                                   Duration monitoringWindow,
                                   int workerCount,
                                   TransactionRepository transactionRepository) {
        this.rules = rules;
        this.alertService = alertService;
        this.monitoringWindow = monitoringWindow;
        this.workerCount = workerCount;
        this.transactionRepository = transactionRepository;
    }

    /** Starts the worker threads. Call once before submitting transactions. */
    @PostConstruct
    public void start() {
        if (started) {
            return;
        }
        for (int i = 1; i <= workerCount; i++) {
            String name = "worker-" + i;
                TransactionWorker worker = new TransactionWorker(
                    queue, activityStore, sequenceByTransactionId, rules, alertService,
                    monitoringWindow, name, transactionRepository);
            Thread thread = new Thread(worker, name);
            thread.start();
            workerThreads.add(thread);
        }
        started = true;
    }

    /** Adds a transaction to the queue for processing. Never blocks the caller for long. */
    public synchronized void submit(Transaction transaction) {
        if (!started) {
            throw new IllegalStateException("The anomaly detection engine is not running.");
        }
        UserActivity activity = activityStore.getOrCreate(transaction.getUserId());
        long sequence = activity.assignSubmissionSequence();
        sequenceByTransactionId.put(transaction.getTransactionId(), sequence);
        queue.add(transaction);
    }

    /**
     * Signals every worker to stop once it has drained the transactions
     * already in the queue, then waits for them to finish. Uses the poison
     * pill pattern: one sentinel per worker.
     */
    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (!started) {
            return;
        }
        for (int i = 0; i < workerCount; i++) {
            queue.add(PoisonPill.INSTANCE);
        }
        for (Thread thread : workerThreads) {
            thread.join();
        }
        started = false;
    }
}
