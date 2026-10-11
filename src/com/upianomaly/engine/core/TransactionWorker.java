package com.upianomaly.engine.core;

import com.upianomaly.engine.alert.AlertService;
import com.upianomaly.engine.model.Alert;
import com.upianomaly.engine.model.Transaction;
import com.upianomaly.engine.rules.AnomalyRule;
import com.upianomaly.engine.repository.TransactionRepository;

import java.time.Duration;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One worker thread's job: repeatedly take a transaction off the shared
 * queue, run every configured rule against it, and hand any resulting
 * alerts to the AlertService.
 *
 * Several TransactionWorker instances run in parallel (see
 * AnomalyDetectionEngine), which is what "worker threads" / "queue-based
 * processing" means in this project: transactions are produced faster than
 * one thread could check them one by one, so a pool of workers drains the
 * queue concurrently instead of processing everything sequentially.
 */
public class TransactionWorker implements Runnable {

    private final BlockingQueue<Transaction> queue;
    private final UserActivityStore activityStore;
    private final ConcurrentHashMap<String, Long> sequenceByTransactionId;
    private final List<AnomalyRule> rules;
    private final AlertService alertService;
    private final Duration monitoringWindow;
    private final String workerName;
    private final TransactionRepository transactionRepository;

    public TransactionWorker(BlockingQueue<Transaction> queue,
                              UserActivityStore activityStore,
                              ConcurrentHashMap<String, Long> sequenceByTransactionId,
                              List<AnomalyRule> rules,
                              AlertService alertService,
                              Duration monitoringWindow,
                              String workerName,
                              TransactionRepository transactionRepository) {
        this.queue = queue;
        this.activityStore = activityStore;
        this.sequenceByTransactionId = sequenceByTransactionId;
        this.rules = rules;
        this.alertService = alertService;
        this.monitoringWindow = monitoringWindow;
        this.workerName = workerName;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run() {
        while (true) {
            Transaction txn;
            try {
                txn = queue.take(); // blocks until a transaction is available
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (txn == PoisonPill.INSTANCE) {
                // Sentinel value telling this worker to stop. See
                // AnomalyDetectionEngine.shutdown() for how it is used.
                return;
            }

            try {
                processTransaction(txn);
            } catch (RuntimeException e) {
                System.err.printf("Failed to process transaction %s: %s%n",
                        txn.getTransactionId(), e.getMessage());
            }
        }
    }

    private void processTransaction(Transaction txn) {
        UserActivity activity = activityStore.getOrCreate(txn.getUserId());
        Long sequence = sequenceByTransactionId.remove(txn.getTransactionId());
        if (sequence == null) {
            throw new IllegalStateException("No processing sequence found for transaction " + txn.getTransactionId());
        }

        activity.awaitProcessingTurn(sequence);
        try {
            Deque<Transaction> windowForUser = activity.recordAndGetWindow(txn, monitoringWindow);
            for (AnomalyRule rule : rules) {
                Optional<Alert> alert = rule.evaluate(txn, windowForUser);
                alert.ifPresent(alertService::raise);
            }
            transactionRepository.markProcessed(txn.getTransactionId());
        } finally {
            activity.completeProcessingTurn(sequence);
        }
    }

    public String getWorkerName() {
        return workerName;
    }
}
