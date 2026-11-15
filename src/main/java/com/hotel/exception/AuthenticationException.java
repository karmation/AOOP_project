package com.hotel.exception;

/**
 * Exception thrown when user authentication fails due to invalid credentials,
 * non-existent accounts, or disabled status.
 */
public class AuthenticationException extends HotelException {
    public enum Reason {
        USER_NOT_FOUND,
        INVALID_PASSWORD,
        ACCOUNT_DISABLED
    }

    private final Reason reason;

    public AuthenticationException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public AuthenticationException(String message, Reason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}

