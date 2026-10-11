package com.upianomaly.engine.core;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds one UserActivity object per user, shared by all worker threads.
 *
 * ConcurrentHashMap is used instead of a plain HashMap because several
 * worker threads read/write this map concurrently while processing
 * different transactions from the queue.
 */
public class UserActivityStore {

    private final ConcurrentHashMap<String, UserActivity> activityByUser = new ConcurrentHashMap<>();

    public UserActivity getOrCreate(String userId) {
        return activityByUser.computeIfAbsent(userId, id -> new UserActivity());
    }
}
