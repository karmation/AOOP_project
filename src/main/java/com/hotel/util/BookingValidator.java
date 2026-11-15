package com.hotel.util;

import com.hotel.exception.BookingConflictException;
import java.sql.Date;

/** Central validation for booking dates and guest counts. */
public final class BookingValidator {
    private BookingValidator() { }

    public static void validate(Date checkIn, Date checkOut, int adults, int children)
            throws BookingConflictException {
        if (checkIn == null || checkOut == null) {
            throw new BookingConflictException("Please enter both check-in and check-out dates.");
        }
        if (!checkOut.after(checkIn)) {
            throw new BookingConflictException("Check-out date must be after check-in date.");
        }
        if (adults < 1 || children < 0) {
            throw new BookingConflictException("At least one adult is required; children cannot be negative.");
        }
    }
}
