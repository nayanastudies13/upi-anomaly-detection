package com.upianomaly.engine.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserActivityOrderingTest {

    @Test
    void waitsForEarlierTransactionBeforeAllowingNextSequence() throws InterruptedException {
        UserActivity activity = new UserActivity();
        long firstSequence = activity.assignSubmissionSequence();
        long secondSequence = activity.assignSubmissionSequence();
        CountDownLatch laterTurnStarted = new CountDownLatch(1);
        CountDownLatch laterTurnCompleted = new CountDownLatch(1);

        Thread laterTransaction = new Thread(() -> {
            laterTurnStarted.countDown();
            activity.awaitProcessingTurn(secondSequence);
            laterTurnCompleted.countDown();
            activity.completeProcessingTurn(secondSequence);
        });
        laterTransaction.start();

        assertTrue(laterTurnStarted.await(1, TimeUnit.SECONDS));
        assertFalse(laterTurnCompleted.await(100, TimeUnit.MILLISECONDS));

        activity.awaitProcessingTurn(firstSequence);
        activity.completeProcessingTurn(firstSequence);

        assertTrue(laterTurnCompleted.await(1, TimeUnit.SECONDS));
        laterTransaction.join();
    }
}