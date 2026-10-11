package com.upianomaly.engine.core;

import com.upianomaly.engine.model.Transaction;

import java.time.Instant;

/**
 * A "poison pill" is a well-known concurrency pattern for shutting down
 * queue-consuming worker threads cleanly: instead of forcibly killing
 * threads, you put a special marker object on the queue for each worker.
 * When a worker pulls the marker off the queue, it knows there is no more
 * real work coming and exits its loop on its own.
 *
 * INSTANCE is a single, unique Transaction object. Workers check for it
 * using reference equality (==), not equals(), so it can never be confused
 * with a real transaction.
 */
final class PoisonPill {
    static final Transaction INSTANCE =
            new Transaction("__POISON_PILL__", "__SYSTEM__", "__SYSTEM__", 0.0, Instant.EPOCH);

    private PoisonPill() {
    }
}
