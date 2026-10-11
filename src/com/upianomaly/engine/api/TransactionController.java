package com.upianomaly.engine.api;

import com.upianomaly.engine.api.ApiDtos.TransactionRequest;
import com.upianomaly.engine.api.ApiDtos.TransactionView;
import com.upianomaly.engine.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionView> submit(@RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(transactionService.submit(request));
    }

    @GetMapping
    public List<TransactionView> findAll() {
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public TransactionView findById(@PathVariable("id") String transactionId) {
        return transactionService.findById(transactionId);
    }
}