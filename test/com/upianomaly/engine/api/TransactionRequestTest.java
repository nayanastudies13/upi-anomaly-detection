package com.upianomaly.engine.api;

import com.upianomaly.engine.api.ApiDtos.TransactionRequest;
import com.upianomaly.engine.api.ApiExceptions.InvalidTransactionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionRequestTest {

    @Test
    void acceptsAValidLocalTimestampAsUtc() {
        var transaction = new TransactionRequest("TXN001", "user001", "merchant001", 500,
                "2026-10-05T10:30:00").toTransaction();

        assertEquals(Instant.parse("2026-10-05T10:30:00Z"), transaction.getTimestamp());
    }

    @Test
    void rejectsMissingTransactionId() {
        var request = new TransactionRequest(" ", "user001", "merchant001", 500,
                "2026-10-05T10:30:00Z");

        assertThrows(InvalidTransactionException.class, request::toTransaction);
    }

    @Test
    void rejectsZeroOrInvalidAmounts() {
        var zero = new TransactionRequest("TXN001", "user001", "merchant001", 0,
                "2026-10-05T10:30:00Z");
        var notANumber = new TransactionRequest("TXN002", "user001", "merchant001", Double.NaN,
                "2026-10-05T10:30:00Z");

        assertThrows(InvalidTransactionException.class, zero::toTransaction);
        assertThrows(InvalidTransactionException.class, notANumber::toTransaction);
    }

    @Test
    void rejectsInvalidTimestamp() {
        var request = new TransactionRequest("TXN001", "user001", "merchant001", 500, "today");

        assertThrows(InvalidTransactionException.class, request::toTransaction);
    }
}