package com.hotel.exception;

/** Base checked exception for hotel application business errors. */
public class HotelException extends Exception {
    public HotelException(String message) { super(message); }
    public HotelException(String message, Throwable cause) { super(message, cause); }
}
