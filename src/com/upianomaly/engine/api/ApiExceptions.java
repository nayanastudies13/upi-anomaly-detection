package com.upianomaly.engine.api;

public final class ApiExceptions {

    private ApiExceptions() {
    }

    public static class InvalidTransactionException extends RuntimeException {
        public InvalidTransactionException(String message) {
            super(message);
        }
    }

    public static class DuplicateTransactionException extends RuntimeException {
        public DuplicateTransactionException(String transactionId) {
            super("Transaction ID already exists: " + transactionId);
        }
    }

    public static class DatabaseException extends RuntimeException {
        public DatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String resource) {
            super(resource + " was not found.");
        }
    }

    public static class ExportException extends RuntimeException {
        public ExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}