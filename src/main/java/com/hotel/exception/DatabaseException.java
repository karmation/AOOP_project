package com.hotel.exception;

/** Wraps a database operation failure with a user-readable context. */
public class DatabaseException extends HotelException {
    public DatabaseException(String message, Throwable cause) { super(message, cause); }
}
