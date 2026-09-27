package com.unishut.exception;

/**
 * Exception thrown when a client request is invalid
 * or violates a uniShut business rule.
 *
 * Examples:
 *
 * - Duplicate email
 * - Duplicate bus number
 * - Invalid reservation
 * - Invalid seat selection
 * - Invalid driver assignment
 * - Attempt to reserve a cancelled trip
 */
public class BadRequestException extends RuntimeException {

    /**
     * Creates an exception with a descriptive message.
     *
     * @param message description of the problem
     */
    public BadRequestException(String message) {
        super(message);
    }


    /**
     * Creates an exception with a descriptive message
     * and the original cause.
     *
     * @param message description of the problem
     * @param cause original exception
     */
    public BadRequestException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}