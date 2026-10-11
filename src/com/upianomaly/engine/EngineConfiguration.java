package com.upianomaly.engine;

import com.upianomaly.engine.alert.AlertService;
import com.upianomaly.engine.core.AnomalyDetectionEngine;
import com.upianomaly.engine.rules.AmountRule;
import com.upianomaly.engine.rules.AnomalyRule;
import com.upianomaly.engine.rules.MeanAmountRule;
import com.upianomaly.engine.rules.VelocityRule;
import com.upianomaly.engine.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class EngineConfiguration {

    @Bean
    public VelocityRule velocityRule(
            @Value("${anomaly.velocity.max-transactions:5}") int maxTransactions,
            @Value("${anomaly.velocity.window-seconds:10}") long windowSeconds) {
        return new VelocityRule(maxTransactions, Duration.ofSeconds(windowSeconds));
    }

    @Bean
    public AmountRule amountRule(
            @Value("${anomaly.amount.single-threshold:50000}") double singleThreshold,
            @Value("${anomaly.amount.cumulative-threshold:70000}") double cumulativeThreshold,
            @Value("${anomaly.amount.window-seconds:10}") long windowSeconds) {
        return new AmountRule(singleThreshold, cumulativeThreshold, Duration.ofSeconds(windowSeconds));
    }

    @Bean
    public MeanAmountRule meanAmountRule(
            @Value("${anomaly.mean.window-seconds:20}") long windowSeconds) {
        return new MeanAmountRule(Duration.ofSeconds(windowSeconds));
    }

    @Bean
    public AnomalyDetectionEngine anomalyDetectionEngine(
            List<AnomalyRule> rules,
            AlertService alertService,
            TransactionRepository transactionRepository,
            @Value("${anomaly.activity-retention-seconds:20}") long retentionSeconds,
            @Value("${anomaly.worker-count:3}") int workerCount) {
        return new AnomalyDetectionEngine(
            rules, alertService, Duration.ofSeconds(retentionSeconds), workerCount, transactionRepository);
    }
}