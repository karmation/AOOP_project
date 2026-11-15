package com.hotel.exception;

/** Thrown when a room cannot be booked. */
public class BookingConflictException extends HotelException {
    public BookingConflictException(String message) { super(message); }
}
