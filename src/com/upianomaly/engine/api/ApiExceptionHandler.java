package com.upianomaly.engine.api;

import com.upianomaly.engine.api.ApiDtos.ApiError;
import com.upianomaly.engine.api.ApiExceptions.DatabaseException;
import com.upianomaly.engine.api.ApiExceptions.DuplicateTransactionException;
import com.upianomaly.engine.api.ApiExceptions.ExportException;
import com.upianomaly.engine.api.ApiExceptions.InvalidTransactionException;
import com.upianomaly.engine.api.ApiExceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ApiError> invalidTransaction(InvalidTransactionException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> invalidJson() {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON.");
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ApiError> duplicateTransaction(DuplicateTransactionException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ApiError> databaseUnavailable(DatabaseException e) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(ExportException.class)
    public ResponseEntity<ApiError> exportFailed(ExportException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> engineUnavailable(IllegalStateException e) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(message, status.value()));
    }
}