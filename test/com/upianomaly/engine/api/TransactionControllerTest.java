package com.upianomaly.engine.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upianomaly.engine.api.ApiDtos.TransactionRequest;
import com.upianomaly.engine.api.ApiDtos.TransactionView;
import com.upianomaly.engine.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest {

    private final TransactionService transactionService = mock(TransactionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(transactionService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsAcceptedWhenTransactionIsSubmitted() throws Exception {
        when(transactionService.submit(any(TransactionRequest.class))).thenAnswer(invocation -> {
            TransactionRequest request = invocation.getArgument(0);
            var transaction = request.toTransaction();
            return TransactionView.from(transaction, "PROCESSING");
        });

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"transactionId":"TXN001","userId":"user001","beneficiaryId":"merchant001",
                                 "amount":500,"timestamp":"2026-10-05T10:30:00Z"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.transactionId").value("TXN001"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void returnsBadRequestForInvalidAmount() throws Exception {
        when(transactionService.submit(any(TransactionRequest.class))).thenAnswer(invocation -> {
            TransactionRequest request = invocation.getArgument(0);
            request.toTransaction();
            return null;
        });

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"transactionId":"TXN001","userId":"user001","beneficiaryId":"merchant001",
                                 "amount":0,"timestamp":"2026-10-05T10:30:00Z"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

}