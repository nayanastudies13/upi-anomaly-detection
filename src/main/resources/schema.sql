CREATE DATABASE IF NOT EXISTS upi_anomaly_detection;
USE upi_anomaly_detection;

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(80) PRIMARY KEY,
    user_id VARCHAR(120) NOT NULL,
    beneficiary_id VARCHAR(120) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_time DATETIME(6) NOT NULL,
    processing_status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transactions_user_time (user_id, transaction_time)
);

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(80) NOT NULL,
    user_id VARCHAR(120) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    anomaly_type VARCHAR(40) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    detected_at DATETIME(6) NOT NULL,
    INDEX idx_alerts_detected_at (detected_at),
    CONSTRAINT fk_alerts_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id)
);