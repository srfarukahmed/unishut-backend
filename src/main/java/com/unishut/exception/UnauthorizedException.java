package com.unishut.exception;

/**
 * Exception thrown when a user is not authorized to perform
 * a requested operation.
 *
 * Examples:
 *
 * - Invalid login credentials
 * - Inactive user account
 * - Student attempting a driver-only operation
 * - Driver attempting an admin-only operation
 * - User attempting to access another user's protected data
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Creates an unauthorized exception with a descriptive message.
     *
     * @param message description of the authorization failure
     */
    public UnauthorizedException(String message) {
        super(message);
    }


    /**
     * Creates an unauthorized exception with a descriptive message
     * and the original cause.
     *
     * @param message description of the authorization failure
     * @param cause original exception
     */
    public UnauthorizedException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}