package com.upianomaly.engine.model;

/**
 * The kinds of anomalies this engine can detect.
 * Kept as an enum so new rule types stay type-safe and easy to extend.
 */
public enum AnomalyType {
    VELOCITY_ALERT,      // too many transactions from the same user in a short window
    HIGH_VELOCITY,       // too many transactions from the same user in a short window
    HIGH_VALUE,          // a single transaction above the high-value threshold
    HIGH_VOLUME_BURST,   // sum of a user's transactions in a short window is too large
    ABOVE_MEAN_AMOUNT    // current amount is above the user's recent mean
}
