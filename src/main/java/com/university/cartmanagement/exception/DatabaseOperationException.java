package com.university.cartmanagement.exception;

/**
 * Exception thrown when a traditional or Spring JDBC database operation encounters an error.
 */
public class DatabaseOperationException extends CartException {
    private static final long serialVersionUID = 1L;

    private final String sqlQuery;

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
        this.sqlQuery = null;
    }

    public DatabaseOperationException(String message, String sqlQuery, Throwable cause) {
        super(message + (sqlQuery != null ? " [SQL: " + sqlQuery + "]" : ""), cause);
        this.sqlQuery = sqlQuery;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }
}
