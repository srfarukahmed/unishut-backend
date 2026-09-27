package com.unishut.exception;

/**
 * Exception thrown when a requested uniShut resource
 * cannot be found in the database.
 *
 * Examples:
 *
 * User not found
 * Bus not found
 * Route not found
 * Trip not found
 * Reservation not found
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates an exception with a custom message.
     *
     * @param message descriptive error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }


    /**
     * Creates an exception with a custom message and
     * the original cause.
     *
     * @param message descriptive error message
     * @param cause original exception
     */
    public ResourceNotFoundException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}