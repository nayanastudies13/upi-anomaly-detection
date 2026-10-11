package com.upianomaly.engine.service;

import com.upianomaly.engine.api.ApiDtos.TransactionRequest;
import com.upianomaly.engine.api.ApiDtos.TransactionView;
import com.upianomaly.engine.api.ApiExceptions;
import com.upianomaly.engine.core.AnomalyDetectionEngine;
import com.upianomaly.engine.model.Transaction;
import com.upianomaly.engine.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AnomalyDetectionEngine anomalyDetectionEngine;

    public TransactionService(TransactionRepository transactionRepository,
                              AnomalyDetectionEngine anomalyDetectionEngine) {
        this.transactionRepository = transactionRepository;
        this.anomalyDetectionEngine = anomalyDetectionEngine;
    }

    public TransactionView submit(TransactionRequest request) {
        if (request == null) {
            throw new ApiExceptions.InvalidTransactionException("A transaction request body is required.");
        }
        Transaction transaction = request.toTransaction();
        transactionRepository.save(transaction);
        anomalyDetectionEngine.submit(transaction);
        return TransactionView.from(transaction, "PROCESSING");
    }

    public List<TransactionView> findAll() {
        return transactionRepository.findAll();
    }

    public TransactionView findById(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Transaction"));
    }

    public long count() {
        return transactionRepository.count();
    }

    public List<TransactionView> findRecent(int limit) {
        return transactionRepository.findRecent(limit);
    }
}